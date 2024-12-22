package to.etc.dec.idasm.gui;

import javax.swing.*;

final public class MainWindow extends JFrame {
	public MainWindow() {
		setTitle("IdASM");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		JButton button = new JButton("Click me");
		getContentPane().add(button);
		pack();
		setVisible(true);
		setSize(1024, 768);

		JTable table = new JTable();

	}

	//private TableModel createDisassemblyModel() {
	//
	//
	//
	//
	//}

}
