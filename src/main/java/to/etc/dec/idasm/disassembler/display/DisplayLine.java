package to.etc.dec.idasm.disassembler.display;

import org.eclipse.jdt.annotation.Nullable;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * A single disassembled line with all its embellishments.
 */
final public class DisplayLine {
	private final DisplayCache m_cache;

	private final List<DisplayItem> m_itemList = new ArrayList<DisplayItem>();

	/** The address for the line, if filled */
	private int m_address;

	private int m_bx, m_by, m_ex, m_ey;

	public DisplayLine(DisplayCache cache) {
		m_cache = cache;
	}

	public DisplayItem newItem() {
		DisplayItem item = m_cache.newItem();
		m_itemList.add(item);
		return item;
	}

	public DisplayItem newItem(ItemType type, String what) {
		DisplayItem item = m_cache.newItem();
		m_itemList.add(item);
		item.setText(what);
		item.setType(type);
		return item;
	}

	public void free() {
		releaseItems();
		m_cache.lineFree(this);
	}

	/**
	 * Remove all items from the line and prepare for a new display.
	 */
	public void prepare(int address) {
		m_address = address;
		releaseItems();
	}

	private void releaseItems() {
		for(DisplayItem di : m_itemList) {
			m_cache.freeItem(di);
		}
		m_itemList.clear();
	}

	@Nullable
	public DisplayItem findItemByCoords(Point pos) {
		for(DisplayItem item : getItemList()) {
			if(item.contains(pos))
				return item;
		}
		return null;
	}

	public void setLocation(int bx, int by, int ex, int ey) {
		m_bx = bx;
		m_by = by;
		m_ex = ex;
		m_ey = ey;
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

	public int getAddress() {
		return m_address;
	}

	public List<DisplayItem> getItemList() {
		return m_itemList;
	}

	@Override public String toString() {
		return "@" + Integer.toOctalString(m_address) + " " + m_by + ".." + m_ey;
	}
}
