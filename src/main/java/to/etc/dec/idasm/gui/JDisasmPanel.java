package to.etc.dec.idasm.gui;

import to.etc.dec.idasm.disassembler.DisContext;
import to.etc.dec.idasm.disassembler.DisassemblerMain;
import to.etc.dec.idasm.disassembler.IDisassembler;
import to.etc.dec.idasm.disassembler.Label;
import to.etc.dec.idasm.disassembler.pdp11.IByteSource;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.List;

public class JDisasmPanel extends JPanel implements Scrollable {
	private final IByteSource m_source;

	private final IDisassembler m_disassembler;

	private final int m_startAddress = 036352;

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
			Graphics2D g2d = (Graphics2D) g;
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
				index--;                        // Get one line before
			m_yPos = m_posMap[index];            // That will render here,
			int endY = fromY + g.getClipBounds().height;
			int addr = m_lineAddresses[index];

			System.out.println("- index=" + index + ", ypos=" + m_yPos + ", addr=" + Integer.toOctalString(addr));
			m_context.setCurrentAddress(addr);
			while(m_yPos < endY) {
				System.out.println("-- renderLine ix=" + index + " @" + m_yPos + ", addr=" + Integer.toOctalString(m_context.getCurrentAddress()));
				m_context.start();
				m_disassembler.disassemble(m_context);
				renderLine(g, m_context);
				index++;
			}
		} catch(Exception x) {
			x.printStackTrace();
		}
	}

	private void renderLine(Graphics g, DisContext context) throws Exception {
		//-- Do we have label(s)?
		List<Label> labels = context.getLabels(context.getStartAddress());
		if(null != labels && !labels.isEmpty()) {
			g.setColor(Color.BLUE);
			int x = m_labelStartX;
			for(Label label : labels) {
				String s = label.getName() + ": ";
				int width = m_fontMetrics.stringWidth(s);
				if(x + width > getSize().width) {
					x = m_labelStartX;
					m_yPos += m_fontHeight;
				}
				g.drawString(s, x, m_yPos + m_maxAscent);
			}
			m_yPos += m_maxAscent;
		}

		//-- Start rendering the instruction
		int y = m_yPos + m_maxAscent;
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
		m_yPos += m_fontHeight;
	}

	private void initialize() throws Exception {
		if(!m_initialized) {
			DisContext ctx = m_context = new DisContext(m_source);
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

			DisassemblerMain.disassemble(ctx, m_disassembler, m_startAddress, m_source.getEndAddress(), a -> {
				addLine(a.getStartAddress(), m_yPos);
				renderLine(g, a);
				//m_yPos += m_fontHeight;
			});

			m_panelHeight = m_yPos;
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
