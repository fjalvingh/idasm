package to.etc.dec.idasm;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.io.File;
import java.io.FileInputStream;

public class Main {
	@Option(name = "-i", required = true, usage = "The input file")
	private String m_input;

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

		byte[] data = loadFile();

		disassemble(data);



	}

	private void disassemble(byte[] data) throws Exception {
		DisContext ctx = new DisContext(data.length, data, NumericBase.Oct);

		IDisassembler das = new PdpDisassembler();
		ctx.setCurrentAddress(036352);
		while(ctx.getCurrentAddress() < ctx.getDataSize()) {
			ctx.start();
			das.disassemble(ctx);

			display(ctx);
		}




	}

	private void display(DisContext ctx) {
		StringBuilder sb = new StringBuilder();
		String as = ctx.getAddressString();
		sb.append(as);
		sb.append(" ");

		String bytes = ctx.getInstBytes();
		sb.append(bytes);
		while(sb.length() < 32)
			sb.append(' ');
		sb.append(ctx.getOpcodeString());
		while(sb.length() < 40)
			sb.append(' ');
		sb.append(ctx.getOperandString());
		System.out.println(sb.toString());
	}

	/**
	 * Load the specified file into the memory array.
	 */
	private byte[] loadFile() throws Exception {
		File inf = new File(m_input);
		int len = (int) inf.length();
		if(len > 65535)
			throw new Exception("File is > 64K");
		byte[] addr = new byte[len];
		try(FileInputStream fis = new FileInputStream(inf)) {
			return fis.readAllBytes();
			//if(read != len)
			//	throw new Exception("Cannot read file fully: only " + read + " of " + len + " bytes");
		}
		//return addr;
	}


}
