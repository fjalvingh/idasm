package to.etc.dec.idasm.gui;

import org.eclipse.jdt.annotation.Nullable;
import to.etc.dec.idasm.disassembler.disassembler.DisContext;
import to.etc.dec.idasm.disassembler.disassembler.IByteSource;
import to.etc.dec.idasm.disassembler.disassembler.IDisassembler;
import to.etc.dec.idasm.disassembler.disassembler.Label;
import to.etc.dec.idasm.disassembler.display.DisplayItem;
import to.etc.dec.idasm.disassembler.display.DisplayLine;
import to.etc.dec.idasm.disassembler.model.InfoModel;
import to.etc.dec.idasm.disassembler.model.RegionType;
import to.etc.dec.idasm.disassembler.util.Util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JDisasmPanel extends JPanel implements Scrollable {
	private final IByteSource m_source;

	private final InfoModel m_infoModel;

	private final IDisassembler m_disassembler;

	private final int m_startAddress = 036352;

	private final Color m_selectionColor = new Color(0, 220, 220);

	/**
	 * The addresses for each line of the disassembly.
	 */
	private int[] m_lineAddresses = new int[8192];

	/**
	 * Y positions per line, as calculated.
	 */
	private int[] m_posMap = new int[8192];

	private int m_lineCount;

	private DisContext m_context;

	private final Font m_font = new Font("Arial", Font.PLAIN, 14);

	private int m_yPos;

	private int m_fontHeight;

	private int m_panelHeight;


	private boolean m_initialized;

	private int m_maxAscent;

	private FontMetrics m_fontMetrics;

	private int m_leftMargin = 10;

	private int m_spacing = 20;

	private int m_bytesSize;

	private int m_charsSize;

	private int m_addrSize;

	private int m_mnemSize;

	private int m_labelStartX;

	/**
	 * Selection start address
	 */
	private int m_selectionStart;		// = 036500;

	/**
	 * Selection end address (exclusive)
	 */
	private int m_selectionEnd;			// = 036530;

	final private List<DisplayLine> m_displayLines = new ArrayList<>();


	public JDisasmPanel(IByteSource source, InfoModel infoModel, IDisassembler disassembler) throws Exception {
		m_source = source;
		m_infoModel = infoModel;
		m_disassembler = disassembler;
		setBorder(BorderFactory.createLineBorder(Color.BLACK));
		addMouseListener(m_mouseListener);
		addMouseMotionListener(m_mouseListener);
	}

	@Override public Dimension getPreferredSize() {
		try {
			initialize();
		} catch(Exception x) {
			x.printStackTrace();
		}
		return new Dimension(1024, m_panelHeight);
	}

	static public final boolean PAINTDBG = false;

	@Override public void paintComponent(Graphics g) {
		super.paintComponent(g);

		try {
			clearOutUnusedLines();
			Graphics2D g2d = (Graphics2D) g;
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
			g2d.setFont(m_font);

			int fromY = g.getClipBounds().y;
			if(PAINTDBG) {
				System.out.println(
					"repaint: "
						+ g.getClipBounds().width
						+ " x " + g.getClipBounds().height
						+ " @ " + g.getClipBounds().x + ", " + g.getClipBounds().y
				);
			}

			//-- Calculate the closest Y line
			int index = Arrays.binarySearch(m_posMap, 0, m_lineCount, fromY);
			if(index < 0) {
				index = -(index + 1);
			}
			if(index > 0)
				index--;                        // Get one line before
			m_yPos = m_posMap[index];            // That will render here,
			int endY = fromY + g.getClipBounds().height;
			int addr = m_lineAddresses[index];

			if(PAINTDBG)
				System.out.println("- index=" + index + ", ypos=" + m_yPos + ", addr=" + Integer.toOctalString(addr));
			m_context.setCurrentAddress(addr);
			m_context.setRender(true);

			int displayLineIndex = calculateDisplayLineIndex(addr);

			while(m_yPos < endY) {
				if(PAINTDBG)
					System.out.println("-- renderLine ix=" + index + " @" + m_yPos + ", addr=" + Integer.toOctalString(m_context.getCurrentAddress()));

				if(inSelection(m_context)) {
					//-- Render a background rectangle with the selection color.
					int selEndY = m_posMap[index + 1];
					g.setColor(m_selectionColor);
					g.fillRect(0, m_yPos, getSize().width, selEndY - m_yPos);
					g.setColor(Color.BLACK);
				}
				m_context.disassembleLine(m_disassembler, a -> {});
				int height = renderLine(g, m_context, m_yPos, false);
				DisplayLine line = m_context.ownLine();
				m_displayLines.add(displayLineIndex++, line);

				m_yPos += height;
				index++;

				while(displayLineIndex < m_displayLines.size()) {
					DisplayLine odl = m_displayLines.get(displayLineIndex);
					if(odl.getBy() < m_yPos) {
						m_displayLines.remove(displayLineIndex);
					} else {
						break;
					}
				}
			}
		} catch(Exception x) {
			x.printStackTrace();
		}
	}

	private int calculateDisplayLineIndex(int addr) {
		int index = Util.binarySearch(m_displayLines, addr, a -> a.getAddress(), Integer::compare);
		if(index < 0)
			index = -(index + 1);
		//if(index > 0 && m_displayLines.get(index).getAddress() == addr)
		//	index--;
		return index;
	}

	private void clearOutUnusedLines() {
		JViewport vp = (JViewport) getParent();
		//System.out.println("Pos = " + vp.getViewPosition());
		Point pos = vp.getViewPosition();
		while(m_displayLines.size() > 0) {
			DisplayLine line = m_displayLines.get(0);
			if(line.getEy() <= pos.y) {
				m_displayLines.remove(0);
				System.out.println("dl: remove line " + Integer.toOctalString(line.getAddress()));
			} else {
				break;
			}
		}

		int ey = pos.y + vp.getSize().height;
		while(m_displayLines.size() > 0) {
			DisplayLine line = m_displayLines.get(m_displayLines.size() - 1);
			if(line.getBy() >= ey) {
				m_displayLines.remove(m_displayLines.size() - 1);
				System.out.println("dl: remove line " + Integer.toOctalString(line.getAddress()));
			} else {
				break;
			}
		}
	}

	private boolean inSelection(DisContext dc) {
		return dc.getCurrentAddress() >= m_selectionStart && dc.getCurrentAddress() < m_selectionEnd;
	}

	/**
	 * Render a line, or just calculate its height.
	 *
	 * @return The height of the rendered area
	 */
	private int renderLine(Graphics g, DisContext context, int atY, boolean calculateHeightOnly) throws Exception {
		int baselineY = atY + m_maxAscent;                            // The baseline to draw at
		int y = atY;

		//-- Do we have label(s)?
		List<Label> labels = context.getLabels(context.getStartAddress());
		if(null != labels && !labels.isEmpty()) {
			g.setColor(Color.BLUE);
			int x = m_labelStartX;
			for(Label label : labels) {
				int drawX = x;
				String s = label.getName() + ": ";
				int width = m_fontMetrics.stringWidth(s);
				if(x + width > getSize().width) {
					x = m_labelStartX;
					baselineY += m_fontHeight;
				} else {
					x += width;
				}
				if(!calculateHeightOnly)
					g.drawString(s, drawX, baselineY);
			}
			baselineY += m_fontHeight;
			y += m_fontHeight;
		}

		//-- Start rendering the instruction
		if(!calculateHeightOnly) {
			int x = m_leftMargin;
			g.setColor(Color.GRAY);
			g.drawString(context.getAddressString(), x, baselineY);
			x += m_addrSize + m_spacing;
			g.drawString(context.getInstBytes(), x, baselineY);
			x += m_bytesSize + m_spacing;
			g.drawString(context.getAsciiBytes(), x, baselineY);
			x += m_charsSize + m_spacing;
			g.setColor(Color.BLACK);
			renderDisplayItems(g, context.getMnemonic(), x, y);
			//g.drawString(itemsToString(context.getMnemonic()), x, baselineY);
			x += m_mnemSize + m_spacing;
			renderDisplayItems(g, context.getOperands(), x, y);
			//g.drawString(itemsToString(context.getOperands()), x, baselineY);
		}
		baselineY += m_fontHeight;
		y += m_fontHeight;
		context.line().setLocation(0, atY, getSize().width, y);
		return y - atY;
	}

	private void renderDisplayItems(Graphics g, List<DisplayItem> list, int x, int y) {
		int baselineY = y + m_maxAscent;
		for(DisplayItem displayItem : list) {
			g.drawString(displayItem.getText(), x, baselineY);
			int width = m_fontMetrics.stringWidth(displayItem.getText());
			displayItem.setLocation(x, y, x + width, y + m_fontHeight);
			x += width;
		}
	}

	private final StringBuilder m_itemSb = new StringBuilder();

	private String itemsToString(List<DisplayItem> list) {
		m_itemSb.setLength(0);
		for(DisplayItem displayItem : list) {
			m_itemSb.append(displayItem.getText());
		}
		return m_itemSb.toString();
	}

	private void initialize() throws Exception {
		if(!m_initialized) {
			DisContext ctx = m_context = new DisContext(m_source, m_infoModel);
			m_disassembler.configureDefaults(ctx);

			Graphics g = getGraphics();
			Graphics2D g2d = (Graphics2D) g;
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
			g2d.setFont(m_font);

			FontMetrics fontMetrics = m_fontMetrics = g.getFontMetrics();

			m_fontHeight = fontMetrics.getHeight();
			m_maxAscent = fontMetrics.getMaxAscent();
			m_panelHeight = 0;
			m_yPos = 0;
			//setSize(1024, m_panelHeight);

			//-- Calculate sizes
			int addrSize = m_context.getCharsInBase(m_disassembler.getAddressSizeInBits());
			m_addrSize = fontMetrics.stringWidth(calculateMeasureString(addrSize));

			int chars = m_disassembler.getMaxInstructionSizeInChars(m_context.getBase());
			m_bytesSize = fontMetrics.stringWidth(calculateMeasureString(chars));
			m_charsSize = fontMetrics.stringWidth(calculateMeasureString(8));

			m_mnemSize = fontMetrics.stringWidth(calculateMeasureString(m_disassembler.getMaxMnemonicSize(), 'm'));

			m_labelStartX = m_leftMargin + m_addrSize
				+ m_spacing + m_bytesSize
				+ m_spacing + m_charsSize;

			//-- Do pass 1
			ctx.predisassembleBlock(m_disassembler, m_startAddress, m_source.getEndAddress());

			ctx.disassembleAndRenderBlock(m_disassembler, m_startAddress, m_source.getEndAddress(), a -> {
				addLine(a.getStartAddress(), m_yPos);
				int height = renderLine(g, a, m_yPos, true);
				m_yPos += height;
			});

			m_panelHeight = m_yPos;
			m_initialized = true;
		}
	}

	private void redoFrom(int address) throws Exception {
		//-- We need to fully re-render from here.
		int index = calculateIndexByAddr(address);
		if(m_lineAddresses[index] != address) {
			throw new IllegalStateException("Bad index");
		}

		//-- Recalculate all lines
		m_context.setCurrentAddress(address);
		int yPos = m_posMap[index];
		m_lineCount = index + 1;
		Graphics g = getGraphics();
		m_context.setRender(true);
		while(m_context.getCurrentAddress() < m_source.getEndAddress()) {
			m_context.disassembleLine(m_disassembler, a -> {});
			int height = renderLine(g, m_context, yPos, true);
			yPos += height;
			addLine(m_context.getCurrentAddress(), yPos);
		}
		m_panelHeight = yPos;

		//-- Now: rerender
		repaint(0L, 0, m_posMap[index], getSize().width, yPos);
	}

	/**
	 * Calculate the index in the line arrays for the specified Y position.
	 */
	public int calculateIndexByY(int y) {
		//-- Calculate the closest Y line
		int index = Arrays.binarySearch(m_posMap, 0, m_lineCount, y);
		if(index < 0) {
			index = -(index + 1);
		}
		return index - 1;                        // As y is always >= the location found the index is always AFTER that location, we need it AT the location
	}

	/**
	 * Calculate the line index by address.
	 */
	private int calculateIndexByAddr(int address) {
		//-- Calculate the closest address
		int index = Arrays.binarySearch(m_lineAddresses, 0, m_lineCount, address);
		if(index < 0) {
			index = -(index + 1);
		}
		if(m_lineAddresses[index] == address) {
			return index;
		}
		return index - 1;                        // As y is always >= the location found the index is always AFTER that location, we need it AT the location
	}

	private String calculateMeasureString(int chars) {
		return calculateMeasureString(chars, '0');
	}


	private String calculateMeasureString(int chars, char c) {
		StringBuilder sb = new StringBuilder();
		while(sb.length() < chars) {
			sb.append(c);
		}
		return sb.toString();
	}

	private void addLine(int address, int yPos) {
		if(m_lineCount >= m_lineAddresses.length) {
			int[] li = new int[m_lineAddresses.length * 2];
			System.arraycopy(m_lineAddresses, 0, li, 0, m_lineAddresses.length);
			m_lineAddresses = li;

			li = new int[m_posMap.length * 2];
			System.arraycopy(m_posMap, 0, li, 0, m_posMap.length);
			m_posMap = li;
		}
		m_lineAddresses[m_lineCount] = address;
		m_posMap[m_lineCount] = yPos;
		m_lineCount++;
	}

	@Nullable
	private DisplayLine findLineByCoords(Point pos) {
		System.out.println("findLineByCoords: " + pos);
		if(m_displayLines.isEmpty())
			return null;
		if(pos.y < m_displayLines.get(0).getBy())
			return null;
		if(pos.y > m_displayLines.get(m_displayLines.size() - 1).getEy())
			return null;

		int index = Util.binarySearch(m_displayLines, pos.y, a -> a.getBy(), Integer::compareTo);
		if(index < 0) {
			//-- Not found; index will be after the line - so we need to decrement it one more
			index = -(index + 1);
			if(index > 0)
				index--;
		}
		DisplayLine line = m_displayLines.get(index);
		if(pos.y >= line.getBy() && pos.y < line.getEy()) {
			System.out.println("Found Line: " + line);
			return line;
		}
		return null;
	}

	@Nullable
	DisplayItem findItemByCoords(Point pos) {
		DisplayLine line = findLineByCoords(pos);
		if(null == line)
			return null;

		//-- Now: walk all line items
		for(DisplayItem item : line.getItemList()) {
			if(item.contains(pos))
				return item;
		}
		return null;
	}

	/*----------------------------------------------------------------------*/
	/*	CODING:	Scrollable interface										*/
	/*----------------------------------------------------------------------*/

	@Override public Dimension getPreferredScrollableViewportSize() {
		return new Dimension(1024, 1024);
	}

	@Override public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
		return m_fontHeight;
	}

	@Override public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
		return m_fontHeight * 20;
	}

	@Override public boolean getScrollableTracksViewportWidth() {
		return false;
	}

	@Override public boolean getScrollableTracksViewportHeight() {
		return false;
	}

	/*----------------------------------------------------------------------*/
	/*	CODING:	Mouse listener												*/
	/*----------------------------------------------------------------------*/
	private final MouseAdapter m_mouseListener = new DisasmPanelMouseAdapter(this);

	/**
	 * Remove the previous selection by asking for a repaint.
	 */
	public void clearSelection() {
		repaintSelection();
		m_selectionStart = 0;
		m_selectionEnd = 0;
	}

	public void repaintSelection() {
		if(m_selectionStart >= m_selectionEnd)                    // Nothing selected?
			return;
		int startIndex = calculateIndexByAddr(m_selectionStart);
		int endIndex = calculateIndexByAddr(m_selectionEnd);
		if(startIndex == -1 || endIndex == -1) {
			return;
		}
		int startY = m_posMap[startIndex];
		int endY = m_posMap[endIndex];
		if(startY >= endY)
			return;

		repaint(0L, 0, startY, getSize().width, endY);
	}


	/*----------------------------------------------------------------------*/
	/*	CODING:	Popup Menu													*/
	/*----------------------------------------------------------------------*/

	void createPopupMenu(int x, int y, int address) {
		//-- Bla bla bla

		//-- Create the actual menu
		JPopupMenu pm = new JPopupMenu();

		JMenuItem miSave = new JMenuItem("Save", KeyEvent.VK_S);
		pm.add(miSave);

		//JMenuItem miData = new JMenuItem("Mark as data", KeyEvent.VK_D);
		//pm.add(miData);

		JMenu menu = new JMenu("Mark as data");
		pm.add(menu);

		//-- All data types as a submenu
		for(RegionType value : RegionType.values()) {
			JMenuItem miItem = new JMenuItem(value.name());
			menu.add(miItem);
			miItem.addActionListener(e -> {
				try {
					actionMarkRegionAs(value);
				} catch(Exception ex) {
					ex.printStackTrace();
				}
			});
		}

		//-- Show popup
		pm.show(this, x, y);
	}

	/**
	 * Mark a region as a different data type.
	 */
	private void actionMarkRegionAs(RegionType value) throws Exception {
		int cs = m_selectionStart;
		int ce = m_selectionEnd;
		if(cs >= ce)
			return;

		m_infoModel.addRegion(value, cs, ce);
		m_infoModel.save();

		m_selectionStart = 0;
		m_selectionEnd = 0;
		redoFrom(cs);
	}

	public int getSelectionStart() {
		return m_selectionStart;
	}

	public void setSelectionStart(int selectionStart) {
		m_selectionStart = selectionStart;
	}

	public int getSelectionEnd() {
		return m_selectionEnd;
	}

	public void setSelectionEnd(int selectionEnd) {
		m_selectionEnd = selectionEnd;
	}

	public void setSelection(int from, int to) {
		if(from > to)
			throw new IllegalArgumentException("from > to");
		m_selectionStart = from;
		m_selectionEnd = to;
	}

	int getLineAddress(int index) {
		if(index < 0 || index > m_lineCount)
			throw new IllegalStateException("Line address index " + index + " out of bounds (max=" + m_lineCount + ")");
		return m_lineAddresses[index];
	}

	int getPosMap(int index) {
		if(index < 0 || index > m_lineCount)
			throw new IllegalStateException("Posmap address index " + index + " out of bounds (max=" + m_lineCount + ")");
		return m_posMap[index];
	}
}
