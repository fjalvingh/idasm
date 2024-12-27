package to.etc.dec.idasm.disassembler;

import to.etc.dec.idasm.deidioting.ConsumerEx;
import to.etc.dec.idasm.disassembler.model.InfoModel;
import to.etc.dec.idasm.disassembler.model.Region;
import to.etc.dec.idasm.disassembler.model.RegionType;
import to.etc.dec.idasm.disassembler.model.RegionWalker;
import to.etc.dec.idasm.disassembler.pdp11.IByteSource;

public class DisassemblerMain {
	/**
	 * Do a multipass disassembly to resolve all labels.
	 */
	static public DisContext disassemble(IDisassembler das, IByteSource data, InfoModel info, int from, int to, ConsumerEx<DisContext> listener) throws Exception {
		DisContext ctx = new DisContext(data, info);
		das.configureDefaults(ctx);
		disassemble(ctx, das, from, to, listener);
		return ctx;
	}

	public static void disassemble(DisContext ctx, IDisassembler das, int from, int to, ConsumerEx<DisContext> listener) throws Exception {

		//-- Pass 1: detect labels
		RegionWalker rw = new RegionWalker(ctx.getInfoModel().getRegionModel());
		ctx.setCurrentAddress(from);

		while(ctx.getCurrentAddress() < to) {
			Region r = rw.updateAddress(ctx.getCurrentAddress());
			ctx.start();

			if(r.getType() == RegionType.Code) {
				das.disassemble(ctx);
			} else {

			}
		}

		//-- Pass 2: output
		ctx.setCurrentAddress(from);
		while(ctx.getCurrentAddress() < to) {
			ctx.start();
			das.disassemble(ctx);
			listener.accept(ctx);
		}
	}

}
