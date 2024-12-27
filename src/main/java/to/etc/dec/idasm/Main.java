package to.etc.dec.idasm;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import to.etc.dec.idasm.cmdline.AttributedLine;
import to.etc.dec.idasm.cmdline.Renderer;
import to.etc.dec.idasm.disassembler.DisContext;
import to.etc.dec.idasm.disassembler.DisassemblerMain;
import to.etc.dec.idasm.disassembler.Label;
import to.etc.dec.idasm.disassembler.model.InfoModel;
import to.etc.dec.idasm.disassembler.pdp11.FileByteSource;
import to.etc.dec.idasm.disassembler.pdp11.IByteSource;
import to.etc.dec.idasm.disassembler.pdp11.PdpDisassembler;
import to.etc.dec.idasm.gui.MainWindow;

import java.io.File;
import java.util.List;
import java.util.TreeSet;

public class Main {
	@Option(name = "-i", required = true, usage = "The input file")
	private String m_input;

	@Option(name = "-g", required = false, usage = "Use the GUI")
	private boolean m_gui;

	static public void main(String[] args) throws Exception {
		new Main().run(args);
	}

	private void run(String[] args) throws Exception {
		CmdLineParser p = new CmdLineParser(this);
		try {
			//-- Decode the tasks's arguments
			p.parseArgument(args);
		} catch(CmdLineException x) {
			System.err.println("Invalid arguments: " + x.getMessage());
			p.printUsage(System.err);
			System.exit(10);
		}

		IByteSource data = loadFile();
		InfoModel infoModel = loadInfoModel();

		if(m_gui) {
			runGUI(data, infoModel);
		} else {
			PdpDisassembler das = new PdpDisassembler();
			DisassemblerMain.disassemble(das, data, infoModel, 036352, data.getEndAddress(), disContext -> display(disContext));
		}
	}

	private InfoModel loadInfoModel() throws Exception {
		String fn = m_input + ".mdl";
		File f = new File(fn);
		InfoModel m = new InfoModel(f);
		m.load();
		return m;
	}

	private void runGUI(IByteSource data, InfoModel infoModel) throws Exception {
		new MainWindow(data, infoModel);
	}

	private final int TAB_BYTES = 8;

	private final int TAB_CHARS = TAB_BYTES + 24;

	private final int TAB_LABEL = TAB_CHARS + 8;

	private final int TAB_MNEMONIC = TAB_LABEL + 8;

	private final int TAB_OPERAND = TAB_MNEMONIC + 8;

	private final int TAB_COMMENT = TAB_OPERAND + 20;

	private AttributedLine m_line = new AttributedLine();

	private void display(DisContext ctx) {
		m_line.reset();
		String as = ctx.getAddressString();

		//-- Do we have labels here?
		List<Label> labels = ctx.getLabels(ctx.getStartAddress());
		TreeSet<Integer> aset = new TreeSet<>();
		if(null != labels) {
			//-- Add all labels on a separate line
			appendAddress(ctx);
			m_line.tabTo(TAB_LABEL);
			for(Label label : labels) {
				m_line.append(label.getName(), Renderer.F_LABEL);
				m_line.append(": ", 0);
				int[] xrefs = label.getXrefs();
				for(int xref : xrefs) {
					aset.add(xref);
				}
			}

			//-- Add xrefs
			m_line.append(";- xrefs: ", Renderer.F_COMMENT);
			for(Integer i : aset) {
				m_line.append(ctx.valueInBase(i) + " ", Renderer.F_COMMENT);
			}
			renderLine();
			m_line.reset();
		}
		appendAddress(ctx);

		String bytes = ctx.getInstBytes();
		m_line.append(bytes, Renderer.F_BYTES);
		m_line.tabTo(TAB_CHARS);
		appendCharacters(ctx);
		m_line.tabTo(TAB_MNEMONIC);

		m_line.append(ctx.getOpcodeString(), Renderer.F_MNEMONIC);
		m_line.tabTo(TAB_OPERAND);
		m_line.append(ctx.getOperandString(), Renderer.F_OPERAND);
		//, TAB_COMMENT);
		renderLine();
	}

	private void appendAddress(DisContext ctx) {
		String as = ctx.getAddressString();
		m_line.append(ctx.getAddressString(), Renderer.F_ADDR);
		m_line.tabTo(TAB_BYTES);
	}

	private Renderer m_renderer = new Renderer();

	public void renderLine() {
		m_line.render(m_renderer);
		StringBuilder sb = m_renderer.getSb();
		System.out.println(sb.toString());
		m_renderer.reset();
		System.out.print(Renderer.RESET);
	}

	private void appendCharacters(DisContext ctx) {
		int i = ctx.getStartAddress();
		while(i < ctx.getCurrentAddress()) {
			int v = ctx.byteAt(i++);
			if(v < 32) {
				m_line.append('.', Renderer.F_CHARS);
			} else if(v < 128) {
				m_line.append((char) v, Renderer.F_CHARS);
			} else if(v < 128 + 32) {
				m_line.append('.', Renderer.F_INVERSE);
			} else {
				m_line.append((char) (v - 128), Renderer.F_INVERSE);
			}
		}
	}

	/**
	 * Load the specified file into the memory array.
	 */
	private IByteSource loadFile() throws Exception {
		File inf = new File(m_input);
		return new FileByteSource(inf);
	}


}
