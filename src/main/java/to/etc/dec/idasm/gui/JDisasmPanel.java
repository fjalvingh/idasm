package to.etc.dec.idasm.gui;

import to.etc.dec.idasm.disassembler.DisContext;
import to.etc.dec.idasm.disassembler.DisassemblerMain;
import to.etc.dec.idasm.disassembler.IDisassembler;
import to.etc.dec.idasm.disassembler.pdp11.IByteSource;

import javax.swing.*;
import java.awt.*;

public class JDisasmPanel extends JPanel {
	private final IByteSource m_source;

	private final IDisassembler m_disassembler;

	/**
	 * The addresses for each line of the disassembly.
	 */
	private int[] m_lineAddresses = new int[8192];

	private int m_lineCount;

	private DisContext m_context;

	private final Font m_font = new Font("Arial", Font.PLAIN, 14);


	private boolean m_initialized;

	public JDisasmPanel(IByteSource source, IDisassembler disassembler) {
		m_source = source;
		m_disassembler = disassembler;
		setBorder(BorderFactory.createLineBorder(Color.BLACK));
	}

	@Override public Dimension getPreferredSize() {
		return new Dimension(1024, 1024);
	}

	@Override public void paintComponent(Graphics g) {
		super.paintComponent(g);
		try {
			Graphics2D g2d= (Graphics2D) g;
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
			g2d.setFont(m_font);

			if(!m_initialized) {
				initialize(g);
				g.setColor(Color.WHITE);
				//g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
				m_initialized = true;
			}

			//-- Calculate the region
			m_yPos = 0;
			int line = 0;

			int addr = m_lineAddresses[line];
			m_context.setCurrentAddress(addr);
			while(m_yPos < g.getClip().getBounds().height) {
				renderLine(g, line++);
			}
		} catch(Exception x) {
			x.printStackTrace();
		}
	}

	private void renderLine(Graphics g, int line) throws Exception {
		m_context.start();
		m_disassembler.disassemble(m_context);

		//-- Start rendering
		g.drawString(m_context.getAddressString(), 10, m_yPos);

		m_yPos += m_fontHeight;
	}

	private int m_yPos;

	private int m_fontHeight;

	private int m_panelHeight;

	private void initialize(Graphics g) throws Exception {
		FontMetrics fontMetrics = g.getFontMetrics();

		m_fontHeight = fontMetrics.getHeight();
		m_panelHeight = 0;
		m_yPos = 0;
		m_context = DisassemblerMain.disassemble(m_disassembler, m_source, 036352, m_source.getEndAddress(), a -> {
			addLine(a.getStartAddress());

		});
		m_panelHeight = m_yPos;
		setSize(1024, m_panelHeight);
	}

	private void addLine(int address) {
		if(m_lineCount >= m_lineAddresses.length) {
			int[] li = new int[m_lineAddresses.length * 2];
			System.arraycopy(m_lineAddresses, 0, li, 0, m_lineAddresses.length);
			m_lineAddresses = li;
		}
		m_lineAddresses[m_lineCount++] = address;
	}
}
