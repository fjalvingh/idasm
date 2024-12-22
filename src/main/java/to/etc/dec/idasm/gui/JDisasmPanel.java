package to.etc.dec.idasm.gui;

import to.etc.dec.idasm.disassembler.DisContext;
import to.etc.dec.idasm.disassembler.DisassemblerMain;
import to.etc.dec.idasm.disassembler.IDisassembler;
import to.etc.dec.idasm.disassembler.pdp11.IByteSource;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

public class JDisasmPanel extends JPanel implements Scrollable {
	private final IByteSource m_source;

	private final IDisassembler m_disassembler;

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

	private int m_leftMargin = 10;

	private int m_spacing = 20;

	private int m_bytesSize;

	private int m_charsSize;

	private int m_addrSize;

	private int m_mnemSize;

	public JDisasmPanel(IByteSource source, IDisassembler disassembler) throws Exception {
		m_source = source;
		m_disassembler = disassembler;
		setBorder(BorderFactory.createLineBorder(Color.BLACK));
	}

	@Override public Dimension getPreferredSize() {
		try {
			initialize();
		} catch(Exception x) {
			x.printStackTrace();
		}
		return new Dimension(1024, m_panelHeight);
	}

	@Override public void paintComponent(Graphics g) {
		super.paintComponent(g);
		try {
			Graphics2D g2d= (Graphics2D) g;
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
			g2d.setFont(m_font);

			int fromY = g.getClipBounds().y;
			System.out.println(
				"repaint: "
					+ g.getClipBounds().width
					+ " x " + g.getClipBounds().height
					+ " @ " + g.getClipBounds().x + ", " + g.getClipBounds().y
			);

			//-- Calculate the closest Y line
			int index = Arrays.binarySearch(m_posMap, 0, m_lineCount, fromY);
			if(index < 0) {
				index = -(index + 1);
			}
			if(index > 0)
				index--;						// Get one line before
			m_yPos = m_posMap[index];			// That will render here,
			int endY = fromY + g.getClipBounds().height;
			int addr = m_lineAddresses[index];

			System.out.println("- index=" + index + ", ypos=" + m_yPos + ", addr=" + Integer.toOctalString(addr));
			m_context.setCurrentAddress(addr);
			while(m_yPos < endY) {
				System.out.println("-- renderLine @" + m_yPos + ", addr=" + Integer.toOctalString(m_context.getCurrentAddress()));
				renderLine(g, index);
				index++;
			}
		} catch(Exception x) {
			x.printStackTrace();
		}
	}

	private void renderLine(Graphics g, int line) throws Exception {
		m_context.start();
		m_disassembler.disassemble(m_context);

		//-- Start rendering
		int y = m_yPos + m_maxAscent;
		int x = m_leftMargin;
		g.drawString(m_context.getAddressString(), x, y);
		x	+= m_addrSize + m_spacing;
		g.drawString(m_context.getInstBytes(), x, y);
		x	+= m_bytesSize + m_spacing;
		g.drawString(m_context.getAsciiBytes(), x, y);
		x	+= m_charsSize + m_spacing;
		g.drawString(m_context.getOpcodeString(), x, y);
		x	+= m_mnemSize + m_spacing;
		g.drawString(m_context.getOperandString(), x, y);
		m_yPos += m_fontHeight;
	}

	private void initialize() throws Exception {
		if(!m_initialized) {
			Graphics g = getGraphics();
			Graphics2D g2d= (Graphics2D) g;
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
			g2d.setFont(m_font);

			FontMetrics fontMetrics = g.getFontMetrics();

			m_fontHeight = fontMetrics.getHeight();
			m_maxAscent = fontMetrics.getMaxAscent();
			m_panelHeight = 0;
			m_yPos = 0;
			m_context = DisassemblerMain.disassemble(m_disassembler, m_source, 036352, m_source.getEndAddress(), a -> {
				addLine(a.getStartAddress(), m_yPos);
				m_yPos += m_fontHeight;
			});
			m_panelHeight = m_yPos;
			//setSize(1024, m_panelHeight);

			//-- Calculate sizes
			int addrSize = m_context.getCharsInBase(m_disassembler.getAddressSizeInBits());
			m_addrSize	= fontMetrics.stringWidth(calculateMeasureString(addrSize));

			int chars = m_disassembler.getMaxInstructionSizeInChars(m_context.getBase());
			m_bytesSize = fontMetrics.stringWidth(calculateMeasureString(chars));
			m_charsSize	= fontMetrics.stringWidth(calculateMeasureString(8));

			m_mnemSize	= fontMetrics.stringWidth(calculateMeasureString(m_disassembler.getMaxMnemonicSize(), 'm'));











			m_initialized = true;
		}
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
}
