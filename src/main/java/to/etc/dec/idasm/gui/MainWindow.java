package to.etc.dec.idasm.gui;

import to.etc.dec.idasm.disassembler.pdp11.IByteSource;
import to.etc.dec.idasm.disassembler.pdp11.PdpDisassembler;

import javax.swing.*;

final public class MainWindow extends JFrame {
	private final IByteSource m_source;

	private DisassemblyModel m_model;

	public MainWindow(IByteSource source) throws Exception {
		m_source = source;
		setTitle("IdASM");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		//JButton button = new JButton("Click me");
		//getContentPane().add(button);
		setSize(1024, 768);

		if(false) {
			JTable table = new JTable();
			m_model = new DisassemblyModel(source);
			m_model.initialize();

			table.setModel(m_model.getTableModel());
			getContentPane().add(new JScrollPane(table));
		} else {
			JDisasmPanel dp = new JDisasmPanel(m_source, new PdpDisassembler());
			dp.setSize(1024, 8192);
			JScrollPane sp = new JScrollPane(dp);
			getContentPane().add(sp);
		}


		pack();
		setVisible(true);
	}


}
