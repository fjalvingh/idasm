package to.etc.dec.idasm.disassembler;

public enum DataType {
	Byte(1, false, "b"),
	SignedByte(1, true, "b"),
	Word(2, false, "w"),
	SignedWord(2, true, "w"),
	Long(4, false, "l"),
	SignedLong(4, true, "l"),
	;

	private final int m_len;

	private final boolean m_signed;

	private final String m_suffix;

	DataType(int len, boolean signed, String suffix) {
		m_len = len;
		m_signed = signed;
		m_suffix = suffix;
	}

	public int getLen() {
		return m_len;
	}

	public boolean isSigned() {
		return m_signed;
	}

	public String getSuffix() {
		return m_suffix;
	}
}
