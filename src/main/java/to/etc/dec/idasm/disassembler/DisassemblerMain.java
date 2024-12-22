package to.etc.dec.idasm.disassembler;

import to.etc.dec.idasm.deidioting.ConsumerEx;
import to.etc.dec.idasm.disassembler.pdp11.IByteSource;

public class DisassemblerMain {
	/**
	 * Do a multipass disassembly to resolve all labels.
	 */
	static public DisContext disassemble(IDisassembler das, IByteSource data, int from, int to, ConsumerEx<DisContext> listener) throws Exception {
		DisContext ctx = new DisContext(data);
		//IDisassembler das = new PdpDisassembler();
		das.configureDefaults(ctx);

		//-- Pass 1: detect labels
		ctx.setCurrentAddress(from);
		while(ctx.getCurrentAddress() < to) {
			ctx.start();
			das.disassemble(ctx);
		}

		//-- Pass 2: output
		ctx.setCurrentAddress(from);
		while(ctx.getCurrentAddress() < to) {
			ctx.start();
			das.disassemble(ctx);
			listener.accept(ctx);
		}
		return ctx;
	}

}
