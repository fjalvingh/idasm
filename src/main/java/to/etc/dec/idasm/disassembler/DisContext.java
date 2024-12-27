package to.etc.dec.idasm.disassembler;

import to.etc.dec.idasm.disassembler.model.InfoModel;
import to.etc.dec.idasm.disassembler.pdp11.IByteSource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Disassembler context.
 */
public class DisContext {
	private final IByteSource m_byteSource;

	private final InfoModel m_infoModel;

	private int m_currentAddress;

	/**
	 * Start address of the current instruction
	 */
	private int m_startAddress;

	private NumericBase m_base = NumericBase.Hex;

	private final StringBuilder m_instBytes = new StringBuilder();

	private String m_addressString;

	private String m_opcodeString;

	private StringBuilder m_operandString = new StringBuilder();

	private final Map<Integer, List<Label>> m_labelMap = new HashMap<>();

	public DisContext(IByteSource data, InfoModel infoModel) {
		m_byteSource = data;
		m_infoModel = infoModel;
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

	public void start() {
		m_startAddress = m_currentAddress;
		m_instBytes.setLength(0);
		m_addressString = getBaseValue(m_startAddress, m_byteSource.getEndAddress() <= 65536 ? 2 : 4);
		m_operandString.setLength(0);
		m_opcodeString = "";
	}

	private int nextByte() {
		return byteAt(m_currentAddress++);
	}

	public int byteAt(int addr) {
		if(addr < m_byteSource.getStartAddress())
			throw new IllegalStateException("Current address 0x" + Integer.toHexString(m_currentAddress) + " is below the start address 0x" + Long.toHexString(m_byteSource.getStartAddress()));

		if(addr >= m_byteSource.getEndAddress()) {
			throw new IllegalStateException("Current address 0x" + Integer.toHexString(m_currentAddress) + " is above the end address 0x" + Long.toHexString(m_byteSource.getEndAddress()));
		}
		return m_byteSource.getByte(addr);
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

	private void appendReadVal(int value, int bytes) {
		if(m_instBytes.length() > 0)
			m_instBytes.append(' ');
		switch(bytes) {
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

	public String valueInBase(int value) {
		switch(m_base){
			default:
				throw new IllegalStateException("Unsupported base: " + m_base);

			case Dec:
				return Integer.toString(value);

			case Hex:
				return Integer.toHexString(value);

			case Oct:
				return Integer.toOctalString(value);
		}
	}

	public void mnemonic(String mov) {
		m_opcodeString = mov;
	}

	public void mnemonicB(String mov, boolean byteMode) {
		m_opcodeString = mov + (byteMode ? "b" : "");
	}


	public void appendOperand(String dest) {
		m_operandString.append(dest);
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

	public String getOpcodeString() {
		return m_opcodeString;
	}

	public String getOperandString() {
		return m_operandString.toString();
	}

	public Label addLabel(int address, String label, AddrTarget type) {
		List<Label> list = m_labelMap.computeIfAbsent(address, k -> new ArrayList<>());
		Label alt = list.stream()
			.filter(a -> a.getAddress() == address && a.getName().equals(label))
			.findFirst()
			.orElse(null);
		if(null != alt) {
			alt.from(m_startAddress);
			return alt;
		}
		alt = new Label(address, label, type).from(m_startAddress);
		list.add(alt);
		return alt;
	}

	public Label addAutoLabel(int address, AddrTarget type) {
		String name = "L" + valueInBase(address);
		return addLabel(address, name, type);
	}

	public List<Label> getLabels(int address) {
		return m_labelMap.get(address);
	}


	public String getLabelsAsString() {
		List<Label> lm = m_labelMap.get(m_startAddress);
		if(null == lm) {
			return null;
		}
		return lm.stream().map(a -> a.getName() + ": ").collect(Collectors.joining());
	}

	/**
	 * Return the #of characters that is required to
	 * render a number of the specified #of bits in
	 * the current base.
	 */
	public int getCharsInBase(int bitSize) {
		double maxValue = Math.pow(2, bitSize);		// The max value of such a number
		double chars = Math.log(maxValue) / Math.log(m_base.getBase());
		return (int) Math.ceil(chars);
	}

	public InfoModel getInfoModel() {
		return m_infoModel;
	}
}
