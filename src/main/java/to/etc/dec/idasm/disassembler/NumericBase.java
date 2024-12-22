package to.etc.dec.idasm.disassembler;

public enum NumericBase {
	Dec(10, 0, 3, 5, 7, 9),
	Oct(8, 0, 3, 6, 9, 12),
	Hex(16, 0, 2, 4, 6, 8);

	NumericBase(int base, int... cpb) {
		m_base = base;
		m_charsPerByte = cpb;
	}

	private int[] m_charsPerByte;

	private int m_base;

	public int getSizeForBytes(int bytes) {
		return m_charsPerByte[bytes];
	}

	public int getBase() {
		return m_base;
	}
}
