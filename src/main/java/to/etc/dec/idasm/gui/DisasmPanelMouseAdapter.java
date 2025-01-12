package to.etc.dec.idasm.gui;

import org.eclipse.jdt.annotation.Nullable;
import to.etc.dec.idasm.disassembler.display.DisplayItem;
import to.etc.dec.idasm.disassembler.display.DisplayLine;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

final class DisasmPanelMouseAdapter extends MouseAdapter {
	private final JDisasmPanel m_panel;

	@Nullable
	private DisplayItem m_selectedItem;

	private DisplayLine m_currentLine;

	public DisasmPanelMouseAdapter(JDisasmPanel panel) {
		m_panel = panel;
	}

	@Override public void mouseClicked(MouseEvent e) {
		//System.out.println("mouseClicked " + e.getX() + "," + e.getY());
	}

	@Override public void mousePressed(MouseEvent e) {
		if(e.getButton() == MouseEvent.BUTTON1) {
			m_panel.setSelectedItem(m_currentLine, m_selectedItem);

			//-- In which line have we clicked?
			int index = m_panel.calculateIndexByY(e.getY());
			if(index == -1)
				return;

			int addr = m_panel.getLineAddress(index);            // The address of the line
			if(JDisasmPanel.PAINTDBG)
				System.out.println("CLICK at index " + index + " address " + Integer.toOctalString(addr) + " ypos=" + e.getY());
			if(e.isShiftDown()) {
				//-- We want to select multiple lines.
				int newSelStart = m_panel.getSelectionStart();
				int newSelEnd = m_panel.getSelectionEnd();
				m_panel.clearSelection();                        // Remove the old selection
				if(addr < newSelStart) {
					newSelStart = addr;            // Extend from the front
				} else {
					//-- Inclusive selection -> we need the next address
					newSelEnd = m_panel.getLineAddress(index + 1);
				}
				m_panel.setSelection(newSelEnd, newSelEnd);

				//-- Calculate positions
				m_panel.repaintSelection();
			} else {
				//-- Clear the previous selection and select only this new line.
				m_panel.clearSelection();
				m_panel.setSelection(addr, m_panel.getLineAddress(index + 1));
				m_panel.repaint(0L, 0, m_panel.getPosMap(index), m_panel.getSize().width, m_panel.getPosMap(index + 1));
			}
		} else if(e.getButton() == MouseEvent.BUTTON3) {
			int index = m_panel.calculateIndexByY(e.getY());
			if(index == -1)
				return;

			int addr = m_panel.getLineAddress(index);            // The address of the line
			m_panel.createPopupMenu(e.getX(), e.getY(), addr);
		}
	}


	@Override public void mouseMoved(MouseEvent e) {
		DisplayLine line = m_panel.findLineByCoords(e.getPoint());
		if(null == line) {
			return;
		}
		m_currentLine = line;
		DisplayItem item = line.findItemByCoords(e.getPoint());
		if(null == item)
			return;

		Graphics g = m_panel.getGraphics();
		DisplayItem prevItem = m_selectedItem;
		if(null != prevItem) {
			g.setColor(Color.WHITE);
			g.drawRect(prevItem.getBx(), prevItem.getBy(), prevItem.getEx() - prevItem.getBx(), prevItem.getEy() - prevItem.getBy());
		}

		g.setColor(Color.GREEN);
		g.drawRect(item.getBx(), item.getBy(), item.getEx() - item.getBx(), item.getEy() - item.getBy());
		m_selectedItem = item;
	}
}
