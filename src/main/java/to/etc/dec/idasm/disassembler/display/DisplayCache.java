package to.etc.dec.idasm.disassembler.display;

import java.util.ArrayList;
import java.util.List;

final public class DisplayCache {
	private final List<DisplayLine> m_availableLines = new ArrayList<DisplayLine>();

	private final List<DisplayLine> m_usedLines = new ArrayList<DisplayLine>();

	private final List<DisplayItem> m_availableItems = new ArrayList<>();

	private final List<DisplayItem> m_usedItems = new ArrayList<>();

	public DisplayLine newLine() {
		if(m_usedLines.size() > 1000)
			throw new IllegalStateException("Lines are not being released");
		if(!m_availableLines.isEmpty()) {
			DisplayLine line = m_availableLines.remove(m_availableLines.size() - 1);
			m_usedLines.add(line);
			return line;
		}

		DisplayLine line = new DisplayLine(this);
		m_usedLines.add(line);
		return line;
	}

	void lineFree(DisplayLine line) {
		if(!m_usedLines.remove(line)) {
			return;
		}
		m_availableLines.add(line);
	}

	DisplayItem newItem() {
		if(!m_availableItems.isEmpty()) {
			DisplayItem item = m_availableItems.remove(m_availableItems.size() - 1);
			m_usedItems.add(item);
			return item;
		}
		DisplayItem item = new DisplayItem();
		m_usedItems.add(item);
		return item;
	}

	void freeItem(DisplayItem item) {
		if(!m_usedItems.remove(item)) {
			return;
		}
		m_availableItems.add(item);
	}


}
