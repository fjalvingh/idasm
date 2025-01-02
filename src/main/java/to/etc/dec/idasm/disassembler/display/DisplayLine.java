package to.etc.dec.idasm.disassembler.display;

import java.util.ArrayList;
import java.util.List;

/**
 * A single disassembled line with all its embellishments.
 */
final public class DisplayLine {
	private final DisplayCache m_cache;

	private final List<DisplayItem> m_itemList = new ArrayList<DisplayItem>();

	public DisplayLine(DisplayCache cache) {
		m_cache = cache;
	}

	public DisplayItem newItem() {
		DisplayItem item = m_cache.newItem();
		m_itemList.add(item);
		return item;
	}

	public void free() {
		for(DisplayItem di : m_itemList) {
			m_cache.freeItem(di);
		}
		m_itemList.clear();

		m_cache.lineFree(this);
	}



}
