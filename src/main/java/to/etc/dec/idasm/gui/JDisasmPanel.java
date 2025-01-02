package to.etc.dec.idasm.gui;

import to.etc.dec.idasm.disassembler.disassembler.DisContext;
import to.etc.dec.idasm.disassembler.disassembler.IByteSource;
import to.etc.dec.idasm.disassembler.disassembler.IDisassembler;
import to.etc.dec.idasm.disassembler.disassembler.Label;
import to.etc.dec.idasm.disassembler.model.InfoModel;
import to.etc.dec.idasm.disassembler.model.RegionType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
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
	private int m_selectionStart = 036500;

	/**
	 * Selection end address (exclusive)
	 */
	private int m_selectionEnd = 036530;


	public JDisasmPanel(IByteSource source, InfoModel infoModel, IDisassembler disassembler) throws Exception {
		m_source = source;
		m_infoModel = infoModel;
		m_disassembler = disassembler;
		setBorder(BorderFactory.createLineBorder(Color.BLACK));
		addMouseListener(m_mouseListener);
	}

	@Override public Dimension getPreferredSize() {
		try {
			initialize();
		} catch(Exception x) {
			x.printStackTrace();
		}
		return new Dimension(1024, m_panelHeight);
	}

	static private final boolean PAINTDBG = false;

	@Override public void paintComponent(Graphics g) {
		super.paintComponent(g);
		try {
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
				m_yPos += height;
				index++;
			}
		} catch(Exception x) {
			x.printStackTrace();
		}
	}

	private boolean inSelection(DisContext dc) {
		return dc.getCurrentAddress() >= m_selectionStart && dc.getCurrentAddress() < m_selectionEnd;
	}

	/**
	 * Return T if the address specified is inside a code area.
	 */
	private boolean inCodeArea(int addr) {
		return true;
	}

	/**
	 * Render a line, or just calculate its height.
	 *
	 * @return The height of the rendered area
	 */
	private int renderLine(Graphics g, DisContext context, int atY, boolean calculateHeightOnly) throws Exception {
		int y = atY + m_maxAscent;                            // The baseline to draw at

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
					y += m_fontHeight;
				} else {
					x += width;
				}
				if(!calculateHeightOnly)
					g.drawString(s, drawX, y);
			}
			y += m_fontHeight;
		}

		//-- Start rendering the instruction
		if(!calculateHeightOnly) {
			int x = m_leftMargin;
			g.setColor(Color.GRAY);
			g.drawString(context.getAddressString(), x, y);
			x += m_addrSize + m_spacing;
			g.drawString(context.getInstBytes(), x, y);
			x += m_bytesSize + m_spacing;
			g.drawString(context.getAsciiBytes(), x, y);
			x += m_charsSize + m_spacing;
			g.setColor(Color.BLACK);
			g.drawString(context.getOpcodeString(), x, y);
			x += m_mnemSize + m_spacing;
			g.drawString(context.getOperandString(), x, y);
		}
		y += m_fontHeight;
		return y - atY - m_maxAscent;
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

			ctx.disassembleBlock(m_disassembler, m_startAddress, m_source.getEndAddress(), a -> {
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
	private int calculateIndexByY(int y) {
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
	private final MouseListener m_mouseListener = new MouseAdapter() {
		@Override public void mouseClicked(MouseEvent e) {
			//System.out.println("mouseClicked " + e.getX() + "," + e.getY());
		}

		@Override public void mousePressed(MouseEvent e) {
			if(e.getButton() == MouseEvent.BUTTON1) {
				//-- In which line have we clicked?
				int index = calculateIndexByY(e.getY());
				if(index == -1)
					return;

				int addr = m_lineAddresses[index];            // The address of the line
				if(e.isShiftDown()) {
					//-- We want to select multiple lines..
					int newSelStart = m_selectionStart;
					int newSelEnd = m_selectionEnd;
					clearSelection();                        // Remove the old selection
					if(addr < newSelStart) {
						newSelStart = addr;            // Extend from the front
					} else {
						//-- Inclusive selection -> we need the next address
						newSelEnd = m_lineAddresses[index + 1];
					}
					m_selectionStart = newSelStart;
					m_selectionEnd = newSelEnd;

					//-- Calculate positions
					repaintSelection();
				} else {
					//-- Clear the previous selection and select only this new line.
					clearSelection();
					m_selectionStart = addr;
					m_selectionEnd = m_lineAddresses[index + 1];
					repaint(0L, 0, m_posMap[index], getSize().width, m_posMap[index + 1]);
				}
			} else if(e.getButton() == MouseEvent.BUTTON3) {
				int index = calculateIndexByY(e.getY());
				if(index == -1)
					return;

				int addr = m_lineAddresses[index];            // The address of the line
				createPopupMenu(e.getX(), e.getY(), addr);
			}
		}
	};

	/**
	 * Remove the previous selection by asking for a repaint.
	 */
	private void clearSelection() {
		repaintSelection();
		m_selectionStart = 0;
		m_selectionEnd = 0;
	}

	private void repaintSelection() {
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

	private void createPopupMenu(int x, int y, int address) {
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
}
