package to.etc.dec.idasm;

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
}
