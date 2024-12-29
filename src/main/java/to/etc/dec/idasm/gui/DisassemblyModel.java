package to.etc.dec.idasm.gui;

import to.etc.dec.idasm.deidioting.WrappedException;
import to.etc.dec.idasm.disassembler.DisContext;
import to.etc.dec.idasm.disassembler.IDisassembler;
import to.etc.dec.idasm.disassembler.model.InfoModel;
import to.etc.dec.idasm.disassembler.pdp11.IByteSource;
import to.etc.dec.idasm.disassembler.pdp11.PdpDisassembler;

import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

/**
 * A model for handling interactive disassembly. This
 * provides a TableModel which shows the disassembly
 * and which changes depending on commands.
 */
public class DisassemblyModel {
	/** The source */
	private final IByteSource m_source;

	private final InfoModel m_infoModel;

	/** The addresses for each line of the disassembly. */
	private int[] m_lineAddresses = new int[8192];

	private int m_lineCount;

	private DisContext m_context;

	private IDisassembler m_disassembler = new PdpDisassembler();

	private int m_lastDisassembledLine = -1;

	static private final String[] COLUMNS = {
		"address",
		"bytes",
		"ascii",
		"label",
		"mnemonic",
		"comment"
	};

	private final TableModel 	m_tableModel = new TableModel() {
		@Override public int getRowCount() {
			return m_lineCount;
		}

		@Override public int getColumnCount() {
			return COLUMNS.length;
		}

		@Override public String getColumnName(int columnIndex) {
			return COLUMNS[columnIndex];
		}

		@Override public Class<?> getColumnClass(int columnIndex) {
			return String.class;
		}

		@Override public boolean isCellEditable(int rowIndex, int columnIndex) {
			return false;
		}

		@Override public Object getValueAt(int rowIndex, int columnIndex) {
			try {
				DisContext ctx = disassembleLine(rowIndex);
				switch(columnIndex) {
					default:
						throw new IllegalStateException(columnIndex + "?");

					case 0:
						return ctx.getAddressString();

					case 1:
						return ctx.getInstBytes();

					case 2:
						return ctx.getAsciiBytes();

					case 3:
						return ctx.getLabelsAsString();

					case 4:
						String operandString = ctx.getOperandString();
						if(operandString == null || operandString.isBlank())
							return ctx.getOpcodeString();
						return ctx.getOpcodeString() + " " + operandString;

					case 5:
						return "";
				}
			} catch(Exception x) {
				// Idiots
				throw WrappedException.wrap(x);
			}
		}

		@Override public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			throw new IllegalStateException();
		}

		@Override public void addTableModelListener(TableModelListener l) {

		}

		@Override public void removeTableModelListener(TableModelListener l) {

		}
	};

	public DisassemblyModel(IByteSource source, InfoModel infoModel) {
		m_source = source;
		m_infoModel = infoModel;
	}

	/**
	 * Handles the initial disassembly to be able to create
	 * a base model for the table.
	 */
	public void initialize() throws Exception {
		DisContext ctx = m_context = new DisContext(m_source, m_infoModel);

		ctx.disassembleBlock(m_disassembler, 036352, m_source.getEndAddress(), a -> {
			addLine(a.getStartAddress());
		});
		m_lastDisassembledLine = -1;
	}

	public DisContext disassembleLine(int line) throws Exception {
		if(m_lastDisassembledLine != line) {
			if(line >= m_lineCount)
				throw new IllegalArgumentException("line " + line + " is greater than max line count=" + m_lineCount);

			m_lastDisassembledLine = line;
			int addr = m_lineAddresses[line];
			m_context.setCurrentAddress(addr);
			m_context.start();
			m_disassembler.disassemble(m_context);
		}
		return m_context;
	}

	private void resetLines() {
		m_lineCount = 0;
		m_lastDisassembledLine = -1;
	}

	private void addLine(int address) {
		if(m_lineCount >= m_lineAddresses.length) {
			int[] li = new int[m_lineAddresses.length * 2];
			System.arraycopy(m_lineAddresses, 0, li, 0, m_lineAddresses.length);
			m_lineAddresses = li;
		}
		m_lineAddresses[m_lineCount++] = address;
	}

	public TableModel getTableModel() {
		return m_tableModel;
	}
}
