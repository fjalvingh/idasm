package to.etc.dec.idasm.gui;

import javax.swing.*;

/**
 * Dialog to ask for a new Label name.
 */
public class DialogUtil {
	/**
	 * Show a Dialog asking for a label name.
	 */
	static public void createLabelDialog(String labelName) {
		JOptionPane.showInputDialog("New label name", labelName);
	}



}
