package to.etc.dec.idasm.disassembler.disassembler;

public enum NumericBase {
	Dec(10, 0, 3, 5, 7, 9),
	Oct(8, 0, 3, 6, 9, 12),
	Hex(16, 0, 2, 4, 6, 8);

	NumericBase(int radix, int... cpb) {
		m_radix = radix;
		m_charsPerByte = cpb;
	}

	private int[] m_charsPerByte;

	private int m_radix;

	public int getSizeForBytes(int bytes) {
		return m_charsPerByte[bytes];
	}

	public int getRadix() {
		return m_radix;
	}

	public String valueInBase(long value, int bits, boolean leadingzeroes) {
		String s = Long.toString(value, getRadix());
		if(!leadingzeroes) {
			return s;
		}

		//-- Get the #chars in that many bits
		double maxValue = Math.pow(2, bits);                    // Max value in that many bits
		double digits = Math.log10(maxValue) / Math.log10(getRadix());
		int count = (int) Math.ceil(digits) - s.length();
		StringBuilder sb = new StringBuilder();
		while(count-- > 0) {
			sb.append('0');
		}
		sb.append(s);
		return sb.toString();
	}

	public int valueLengthForBits(int bits) {
		double maxValue = Math.pow(2, bits);                    // Max value in that many bits
		double digits = Math.log10(maxValue) / Math.log10(getRadix());
		return (int) Math.ceil(digits);
	}

	public boolean isValidChar(char c) {
		switch(this){
			default:
				throw new IllegalStateException();
			case Dec:
				return Character.isDigit(c);

			case Oct:
				return c >= '0' && c <= '7';

			case Hex:
				return Character.isDigit(c) || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
		}
	}
}
