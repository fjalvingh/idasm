package to.etc.dec.idasm.disassembler;

public enum NumericBase {
	Dec(0, 3, 5, 7, 9),
	Oct(0, 3, 6, 9, 12),
	Hex(0, 2, 4, 6, 8);

	NumericBase(int... cpb) {
		m_charsPerByte = cpb;
	}

	private int[] m_charsPerByte;

	public int getSizeForBytes(int bytes) {
		return m_charsPerByte[bytes];
	}
}
