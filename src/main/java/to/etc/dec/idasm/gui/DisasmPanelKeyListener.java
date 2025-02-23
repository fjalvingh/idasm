package to.etc.dec.idasm.gui;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class DisasmPanelKeyListener implements KeyListener {
	private final JDisasmPanel m_panel;

	public DisasmPanelKeyListener(JDisasmPanel panel) {
		m_panel = panel;
	}

	@Override public void keyTyped(KeyEvent e) {
		try {
			int modifiers = e.getModifiersEx();
			if(modifiers == 0) {
				switch(e.getKeyChar()){
					default:
						return;

					case 'l':
						m_panel.editLabelAtCursor();
						return;

					case '/':
						m_panel.editBlockCommentAtCursor();
						return;

					case ';':
						m_panel.editLineCommentAtCursor();
						return;
				}
			}

		} catch(Exception ex) {
			//-- Checked exceptions are idiotic.
			ex.printStackTrace();
		}
	}

	@Override public void keyReleased(KeyEvent e) {
	}

	@Override public void keyPressed(KeyEvent e) {
		System.out.println("Pressed");
	}
}
