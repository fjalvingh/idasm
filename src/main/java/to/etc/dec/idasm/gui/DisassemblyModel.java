package to.etc.dec.idasm.gui;

/**
 * A model for handling interactive disassembly. This
 * provides a TableModel which shows the disassembly
 * and which changes depending on commands.
 */
public class DisassemblyModel {
	/** The source */
	private final byte[] m_memory;

	public DisassemblyModel(byte[] memory) {
		m_memory = memory;
	}

	/**
	 * Handles the initial disassembly to be able to create
	 * a base model for the table.
	 */
	public void initialize() {




	}


}
