package to.etc.dec.idasm.disassembler.disassembler;

import org.eclipse.jdt.annotation.Nullable;
import to.etc.dec.idasm.deidioting.ConsumerEx;
import to.etc.dec.idasm.disassembler.display.DisplayCache;
import to.etc.dec.idasm.disassembler.display.DisplayItem;
import to.etc.dec.idasm.disassembler.display.DisplayLine;
import to.etc.dec.idasm.disassembler.display.ItemType;
import to.etc.dec.idasm.disassembler.model.InfoModel;
import to.etc.dec.idasm.disassembler.model.Label;
import to.etc.dec.idasm.disassembler.model.Region;
import to.etc.dec.idasm.disassembler.model.RegionModel;
import to.etc.dec.idasm.disassembler.model.RegionType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Disassembler context.
 */
public class DisContext {
	private final IByteSource m_byteSource;

	private final InfoModel m_infoModel;

	private final DisplayCache m_cache = new DisplayCache();

	private boolean m_hasMnemonic;

	private boolean m_hasOperand;

	enum Endianness {
		Little,
		Big
	}

	private Endianness m_endianness = Endianness.Little;

	private int m_addressBits = 16;

	private int m_currentAddress;

	/**
	 * Start address of the current instruction
	 */
	private int m_startAddress;

	private NumericBase m_base = NumericBase.Hex;

	private final StringBuilder m_instBytes = new StringBuilder();

	private String m_addressString;

	private List<DisplayItem> m_mnemonic = new ArrayList<>();

	private List<DisplayItem> m_operands = new ArrayList<>();

	/**
	 * The line we're rendering into, or null if we do not want to render (for label calculating passes).
	 */
	@Nullable
	private DisplayLine m_line;

	/**
	 * True when we actually need to do rendering, i.e. adding line items
	 * so that they can be displayed.
	 */
	private boolean m_render;

	public DisContext(IByteSource data, InfoModel infoModel) {
		m_byteSource = data;
		m_infoModel = infoModel;
	}

	/*----------------------------------------------------------------------*/
	/*	CODING:	Disassembly methods.										*/
	/*----------------------------------------------------------------------*/

	public void start() {
		m_startAddress = m_currentAddress;
		m_instBytes.setLength(0);
		m_addressString = getBaseValue(m_startAddress, m_byteSource.getEndAddress() <= 65536 ? 2 : 4);
		m_mnemonic.clear();
		m_operands.clear();
		m_hasMnemonic = false;
		m_hasOperand = false;

		//-- Initialize the Line if rendering
		DisplayLine line = m_line;
		if(m_render) {
			if(line == null) {
				line = m_line = m_cache.newLine();
			}
			line.prepare(m_currentAddress);
		} else {
			if(line != null) {
				m_line = null;
				line.free();
			}
		}
	}

	/**
	 * Does a non-rendering pass over the block, to calculate labels and offsets.
	 */
	public void predisassembleBlock(IDisassembler das, int from, int to) throws Exception {
		setCurrentAddress(from);
		setRender(false);
		while(getCurrentAddress() < to) {
			disassembleLine(das, ctx -> {
			});
		}
	}

	public void disassembleAndRenderBlock(IDisassembler das, int from, int to, ConsumerEx<DisContext> listener) throws Exception {
		//-- Pass 2: output
		setRender(true);
		setCurrentAddress(from);
		while(getCurrentAddress() < to) {
			disassembleLine(das, listener);
		}
	}

	public void disassembleLine(IDisassembler das, ConsumerEx<DisContext> listener) throws Exception {
		Region r = getRegionModel().updateAddress(getCurrentAddress());
		start();
		if(r.getType() == RegionType.Code) {
			das.disassemble(this);
		} else {
			//System.out.println("data @" + Integer.toOctalString(getStartAddress()));
			das.getDataDisassembler().disassemble(this, r);
		}
		listener.accept(this);
	}

	/*----------------------------------------------------------------------*/
	/*	CODING:	Getting data from the memory source							*/
	/*----------------------------------------------------------------------*/


	public int nextByte() {
		return byteAt(m_currentAddress++);
	}

	public int byteAt(int addr) {
		return byteAt(addr, false);
	}

	public int byteAt(int addr, boolean dumpByte) {
		if(addr < m_byteSource.getStartAddress())
			throw new IllegalStateException("Current address 0x" + Integer.toHexString(m_currentAddress) + " is below the start address 0x" + Long.toHexString(m_byteSource.getStartAddress()));

		if(addr >= m_byteSource.getEndAddress()) {
			throw new IllegalStateException("Current address 0x" + Integer.toHexString(m_currentAddress) + " is above the end address 0x" + Long.toHexString(m_byteSource.getEndAddress()));
		}
		int v = m_byteSource.getByte(addr);
		if(dumpByte) {
			appendBytes(valueInBase(v, DataType.Byte));
		}
		return v;
	}

	public int wordAt(int addr) {
		return wordAt(addr, false);
	}

	public int wordAt(int addr, boolean dumpBytes) {
		int lo = byteAt(addr);
		int hi = byteAt(addr + 1);
		int val = m_endianness == Endianness.Little ? lo | (hi << 8) : hi | (lo << 8);
		if(dumpBytes) {
			appendBytes(valueInBase(val, DataType.Word));
		}
		return val;
	}

	public int longAt(int addr) {
		return longAt(addr, false);
	}

	public int longAt(int addr, boolean dumpBytes) {
		int lo = wordAt(addr, dumpBytes);
		int hi = wordAt(addr + 2, dumpBytes);
		if(m_endianness == Endianness.Little) {
			return lo | (hi << 16);
		} else {
			return hi | (lo << 16);
		}
	}

	public long getValueAt(int addr, DataType type) {
		return getValueAt(addr, type, false);
	}

	public long getValueAt(int addr, DataType type, boolean addToBytes) {
		switch(type){
			default:
				throw new IllegalStateException(type + "??");

			case Byte:
				return byteAt(addr, addToBytes);                        // Byte, masked to 0xff

			case Word:
				return wordAt(addr, addToBytes);

			case Long:
				return longAt(addr, addToBytes) & 0xffffffff;            // Make unsigned

			case SignedByte:
				int val = byteAt(addr, addToBytes);
				if((val & 0x80) != 0)
					val |= 0xffffff00;
				return val;

			case SignedWord:
				val = wordAt(addr, addToBytes);
				if((val & 0x8000) != 0)
					val |= 0xffff0000;
				return val;

			case SignedLong:
				return longAt(addr, addToBytes);
		}
	}

	public int getByte() {
		int hi = nextByte();
		appendReadVal(hi, 1);
		return hi;
	}

	/**
	 * Get little endian word.
	 */
	public int getWordLE() {
		int lo = nextByte();
		int hi = nextByte();
		int val = (hi << 8) | (lo & 0xff);
		appendReadVal(val, 2);
		return val;
	}

	/**
	 * Get big endian word.
	 */
	public int getWordBE() {
		int hi = nextByte();
		int lo = nextByte();
		int val = (hi << 8) | (lo & 0xff);
		appendReadVal(val, 2);
		return val;
	}


	/*----------------------------------------------------------------------*/
	/*	CODING:	Rendering settings											*/
	/*----------------------------------------------------------------------*/
	public void setRender(boolean render) {
		m_render = render;
		if(!render) {
			DisplayLine line = m_line;
			if(null != line) {
				m_line = null;
				line.free();
			}
		}
	}

	public DisplayLine line() {
		return Objects.requireNonNull(m_line);
	}

	public DisplayLine ownLine() {
		DisplayLine line = m_line;
		m_line = null;
		return line;
	}

	/*----------------------------------------------------------------------*/
	/*	CODING:	Writing decoded data to the output buffers					*/
	/*----------------------------------------------------------------------*/

	public void appendDss(DataType size, int count, long value) {
		mnemonic("ds." + size.getSuffix());

		operandNumber(valueInBase((int) value));                // FIXME 32bit?
		operandPunctuation(",");
		operandNumber(valueInBase(count));
		m_instBytes.append(valueInBase((int) value)).append(" * ").append(valueInBase(count));
	}


	private void appendReadVal(int value, int bytes) {
		if(m_instBytes.length() > 0)
			m_instBytes.append(' ');
		switch(bytes){
			default:
				throw new IllegalStateException(bytes + "?");

			case 1:
				value &= 0xff;
				break;

			case 2:
				value &= 0xffff;
				break;

			case 4:
				break;
		}

		String v = valueInBase(value);
		int chars = m_base.getSizeForBytes(bytes);
		chars -= v.length();
		while(chars-- > 0)
			m_instBytes.append('0');
		m_instBytes.append(v);
	}

	private void appendBytes(String bytesVal) {
		if(m_instBytes.length() > 0)
			m_instBytes.append(' ');
		m_instBytes.append(bytesVal);
	}

	private String getBaseValue(int value, int bytes) {
		String v = valueInBase(value);
		int chars = m_base.getSizeForBytes(bytes);
		chars -= v.length();
		StringBuilder sb = new StringBuilder(16);
		while(chars-- > 0)
			sb.append('0');
		sb.append(v);
		return sb.toString();
	}

	public String valueInBase(long value) {
		switch(m_base){
			default:
				throw new IllegalStateException("Unsupported base: " + m_base);

			case Dec:
				return Long.toString(value);

			case Hex:
				return Long.toHexString(value);

			case Oct:
				return Long.toOctalString(value);
		}
	}

	public String valueInBase(long value, DataType type) {
		String res = Long.toString(value, m_base.getRadix());
		int chlen = m_base.getSizeForBytes(type.getLen());
		if(res.length() >= chlen)
			return res;
		StringBuilder sb = new StringBuilder(16);
		chlen -= res.length();
		while(chlen-- > 0) {
			sb.append('0');
		}
		sb.append(res);
		return sb.toString();
	}

	public void mnemonic(String mov) {
		m_hasMnemonic = true;
		if(m_render) {
			m_mnemonic.add(line().newItem(ItemType.Mnemonic, mov));
		}
	}

	public void mnemonicB(String mov, boolean byteMode) {
		mnemonic(mov + (byteMode ? "b" : ""));
	}

	public DisContext operandPunctuation(String s) {
		m_hasOperand = true;
		if(m_render) {
			m_operands.add(line().newItem(ItemType.Punctuation, s));
		}
		return this;
	}

	public DisContext operandRegister(String s) {
		m_hasOperand = true;
		if(m_render) {
			m_operands.add(line().newItem(ItemType.Register, s));
		}
		return this;
	}

	public DisContext operandLabel(Label label) {
		m_hasOperand = true;
		if(m_render) {
			m_operands.add(line().newItem(ItemType.Label, label.getName()).setAttachedObject(label));
		}
		return this;
	}

	public DisContext operandNumber(String s) {
		m_hasOperand = true;
		if(m_render) {
			m_operands.add(line().newItem(ItemType.Number, s));
		}
		return this;
	}

	/*----------------------------------------------------------------------*/
	/*	CODING:	Labels														*/
	/*----------------------------------------------------------------------*/



	//public Label addLabel(int address, String label, AddrTarget type) {
	//	List<Label> list = m_labelMap.computeIfAbsent(address, k -> new ArrayList<>());
	//	Label alt = list.stream()
	//		.filter(a -> a.getAddress() == address && a.getName().equals(label))
	//		.findFirst()
	//		.orElse(null);
	//	if(null != alt) {
	//		alt.from(m_startAddress);
	//		return alt;
	//	}
	//	if(m_render)
	//		throw new IllegalStateException("Label " + label + " being created after pass 1");
	//	alt = new Label(address, label, type, false).from(m_startAddress);
	//	list.add(alt);
	//	return alt;
	//}

	public Label addAutoLabel(int address, AddrTarget type) {
		String name = "L" + m_base.valueInBase(address, m_addressBits, true);
		return m_infoModel.addAutoLabel(address, type, name);
	}

	public Label setLabel(int address, String name, AddrTarget type) {
		return m_infoModel.setLabel(address, name, type);
	}

	public boolean isAutoLabelFormat(String s) {
		if(!s.startsWith("L"))
			return false;
		int chars = m_base.valueLengthForBits(m_addressBits);
		if(s.length() != chars + 1)
			return false;

		for(int i = 1; i < s.length(); i++) {
			char c = s.charAt(i);
			if(!m_base.isValidChar(c))
				return false;
		}
		return true;
	}

	public List<Label> getLabels(int address) {
		Label label = m_infoModel.getLabel(address);
		return null == label ? null : List.of(label);
	}

	/**
	 * Return the #of characters that is required to
	 * render a number of the specified #of bits in
	 * the current base.
	 */
	public int getCharsInBase(int bitSize) {
		double maxValue = Math.pow(2, bitSize);        // The max value of such a number
		double chars = Math.log(maxValue) / Math.log(m_base.getRadix());
		return (int) Math.ceil(chars);
	}

	public InfoModel getInfoModel() {
		return m_infoModel;
	}

	public RegionModel getRegionModel() {
		return m_infoModel.getRegionModel();
	}

	public DisContext.Endianness getEndianness() {
		return m_endianness;
	}

	public void setEndianness(Endianness endianness) {
		m_endianness = endianness;
	}

	public int getAddressBits() {
		return m_addressBits;
	}

	public void setAddressBits(int addressBits) {
		m_addressBits = addressBits;
	}

	public void setBase(NumericBase base) {
		m_base = base;
	}

	public NumericBase getBase() {
		return m_base;
	}

	public void setCurrentAddress(int currentAddress) {
		m_currentAddress = currentAddress;
	}

	public int getCurrentAddress() {
		return m_currentAddress;
	}

	public int getStartAddress() {
		return m_startAddress;
	}

	public String getInstBytes() {
		return m_instBytes.toString();
	}

	public String getAsciiBytes() {
		StringBuilder sb = new StringBuilder();
		int pos = m_startAddress;
		while(pos < m_currentAddress) {
			int val = byteAt(pos++);
			if(val < 32) {
				sb.append('.');
			} else if(val < 128) {
				sb.append((char) val);
			} else if(val > 128) {
				sb.append('.');
			}
		}
		return sb.toString();
	}

	public String getAddressString() {
		return m_addressString;
	}

	public List<DisplayItem> getMnemonic() {
		return m_mnemonic;
	}

	public List<DisplayItem> getOperands() {
		return m_operands;
	}

	public boolean hasMnemonic() {
		return m_hasMnemonic;
	}

	public boolean hasOperand() {
		return m_hasOperand;
	}
}
