package to.etc.dec.idasm.disassembler.display;

final public class DisplayItem {
	private String m_text;

	private ItemType m_type;

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
}
