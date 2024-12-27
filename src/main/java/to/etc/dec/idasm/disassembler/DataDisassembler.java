package to.etc.dec.idasm.disassembler;

import to.etc.dec.idasm.disassembler.model.Region;

/**
 * Disassembler part which
 */
final public class DataDisassembler {
	public void disassemble(DisContext ctx, Region region) throws Exception {
		switch(region.getType()) {
			default:
				decodeBytes(ctx, region, 1);
				return;



		}









	}

	private void decodeBytes(DisContext ctx, Region region, int size) {
		int addr = ctx.getCurrentAddress();
		int maxlen = region.getEnd() - ctx.getCurrentAddress();

		//-- Do we have a span of the same bytes?
		int val = ctx.byteAt(addr);
		int peekAddr = addr + 1;
		while(val == ctx.byteAt(peekAddr) && peekAddr < region.getEnd() ) {
			peekAddr++;
		}
		int rll = peekAddr - addr;
		if(rll >= 4) {
			//-- render as ds.b value, count
			ctx.appendDss(1, rll, val);
			ctx.setCurrentAddress(addr + rll);
			return;
		}

		if(maxlen > 10) {
			maxlen = 10;
		}

		//-- Just dump bytes
		StringBuilder sb = new StringBuilder();
		while(maxlen-- > 0) {
			val = ctx.getByte();
			if(sb.length() > 0) {
				sb.append(',');
			}
			sb.append(ctx.valueInBase(val));
		}
		ctx.mnemonic("db");
		ctx.appendOperand(sb.toString());
	}
}
