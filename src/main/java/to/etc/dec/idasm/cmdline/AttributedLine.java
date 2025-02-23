package to.etc.dec.idasm.cmdline;

/**
 * Used to render syntax highlighting mixed with other
 * per-character characteristics, this contains a line
 * of characters where each character has a 16-bit attribute
 * value associated with it. The low part of that attribute
 * contains the syntax highlighting code, the high part
 * contains user-specific values.
 * <p>
 * The thing can then be used to create spans of characters
 * having the same attribute values, so that they can be
 * rendered with the same style.
 */
final public class AttributedLine {
	private char[] m_characters = new char[1024];

	private short[] m_attributes = new short[1024];

	/**
	 * The current offset/size of the string contained in the line.
	 */
	private int m_printOffset;

	/**
	 * Append the specified string at the current position. Each
	 * character in the string has the specified attribute.
	 */
	public AttributedLine append(CharSequence cs, int attribute) {
		int totalLen = cs.length() + m_printOffset;
		resize(totalLen);
		for(int i = 0; i < cs.length(); i++) {
			m_characters[m_printOffset] = cs.charAt(i);
			m_attributes[m_printOffset] = (short) attribute;
			m_printOffset++;
		}
		return this;
	}

	public AttributedLine append(char c, int attribute) {
		resize(m_printOffset + 20);
		m_characters[m_printOffset] = c;
		m_attributes[m_printOffset] = (short) attribute;
		m_printOffset++;
		return this;
	}

	public AttributedLine tabTo(int pos) {
		return tabTo(pos, 0);
	}

	public AttributedLine tabTo(int pos, int attr) {
		resize(pos + 10);
		while(m_printOffset < pos) {
			m_characters[m_printOffset] = ' ';
			m_attributes[m_printOffset++] = (short) attr;
		}
		return this;
	}



	/**
	 * Set the specified range of attributes. The range must fall
	 * within the available characters. The attribute value is
	 * first masked by mask, after that the value is orred in.
	 */
	public void mark(int start, int end, short mask, short value) {
		if(end > m_printOffset)
			throw new IllegalStateException("End (" + end + ") must be inside the available string size (" + m_printOffset + ")");

		//-- The mask must not change the lower 8 bits
		mask |= 0xff;										// Make sure lower bits remain as-is
		for(int i = start; i < end; i++) {
			m_attributes[i] = (short) ((m_attributes[i] & mask) | value);
		}
	}

	public void setMark(int start, int end, short mark) {
		if(end > m_printOffset)
			throw new IllegalStateException("End (" + end + ") must be inside the available string size (" + m_printOffset + ")");
		for(int i = start; i < end; i++) {
			m_attributes[i] = (short) ((m_attributes[i] & 0xff) | mark);
		}
	}

	/**
	 * Resize the arrays if the required size is too small.
	 */
	private void resize(int totalLen) {
		if(totalLen <= m_characters.length)
			return;

		char[] newChars = new char[m_characters.length + 2048];
		System.arraycopy(m_characters, 0, newChars, 0, m_characters.length);
		m_characters = newChars;

		short[] newAttributes = new short[m_attributes.length + 2048];
		System.arraycopy(m_attributes, 0, newAttributes, 0, m_attributes.length);
		m_attributes = newAttributes;
	}

	/**
	 * Clear all.
	 */
	public void reset() {
		m_printOffset = 0;
	}

	public interface IRunListener {
		void renderRun(String text, short attribute);
	}

	/**
	 * Scan the data, and collect ranges of text and attributes where
	 * the attributes are the same.
	 */
	public void render(IRunListener listener) {
		if(m_printOffset == 0)
			return;
		short currentAttr = m_attributes[0];
		int index = 0;
		while(index < m_printOffset) {
			//-- Collect a range
			int startIndex = index;
			while(m_attributes[index] == currentAttr && index < m_printOffset)
				index++;

			listener.renderRun(new String(m_characters, startIndex, index - startIndex), currentAttr);
			currentAttr = m_attributes[index];
		}
	}
}
