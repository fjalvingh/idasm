package to.etc.dec.idasm.disassembler.display;

import org.eclipse.jdt.annotation.Nullable;

import java.awt.*;

final public class DisplayItem {
	private String m_text;

	private ItemType m_type;

	private int m_bx, m_by, m_ex, m_ey;

	@Nullable
	private Object m_attachedObject;

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

	public boolean contains(Point pos) {
		return pos.y >= m_by && pos.y < m_ey && pos.x >= m_bx && pos.x < m_ex;
	}

	public int getBx() {
		return m_bx;
	}

	public int getBy() {
		return m_by;
	}

	public int getEx() {
		return m_ex;
	}

	public int getEy() {
		return m_ey;
	}

	@Nullable public Object getAttachedObject() {
		return m_attachedObject;
	}

	public DisplayItem setAttachedObject(@Nullable Object attachedObject) {
		m_attachedObject = attachedObject;
		return this;
	}
}
