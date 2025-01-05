package to.etc.dec.idasm.disassembler.display;

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
}
