package to.etc.dec.idasm.disassembler.display;

final public class DisplayItem {
	private String m_text;

	private ItemType m_type;

	private int m_bx, m_by, m_ex, m_ey;

	public String getText() {
		return m_text;
	}

	public void setText(String text) {
		m_text = text;
	}

	public ItemType getType() {
		return m_type;
	}

	public void setType(ItemType type) {
		m_type = type;
	}

	public void setLocation(int bx, int by, int ex, int ey) {
		m_bx = bx;
		m_by = by;
		m_ex = ex;
		m_ey = ey;
	}
}
