package to.etc.dec.idasm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Disassembler context.
 */
public class DisContext {
	private int m_currentAddress;

	/**
	 * Start address of the current instruction
	 */
	private int m_startAddress;

	private final int m_dataSize;

	private final byte[] m_memory;

	private final NumericBase m_base;

	private final StringBuilder m_instBytes = new StringBuilder();

	private String m_addressString;

	private String m_opcodeString;

	private StringBuilder m_operandString = new StringBuilder();

	private final Map<Integer, List<Label>> m_labelMap = new HashMap<>();

	public DisContext(int dataSize, byte[] memory, NumericBase base) {
		m_dataSize = dataSize;
		m_memory = memory;
		m_base = base;
	}

	public void setCurrentAddress(int currentAddress) {
		m_currentAddress = currentAddress;
	}

	public void start() {
		m_startAddress = m_currentAddress;
		m_instBytes.setLength(0);
		m_addressString = getBaseValue(m_startAddress, m_dataSize <= 65536 ? 2 : 4);
		m_operandString.setLength(0);
		m_opcodeString = "";
	}

	private int nextByte() {
		if(m_currentAddress >= m_dataSize) {
			throw new IllegalStateException("Address overflow");
		}
		return m_memory[m_currentAddress++] & 0xff;
	}

	public int byteAt(int addr) {
		if(addr >= m_dataSize) {
			throw new IllegalStateException("Address overflow");
		}
		return m_memory[addr] & 0xff;
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

	public int getDataSize() {
		return m_dataSize;
	}

	public int getStartAddress() {
		return m_startAddress;
	}

	public String getInstBytes() {
		return m_instBytes.toString();
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


}
