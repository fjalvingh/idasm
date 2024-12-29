package to.etc.dec.idasm.disassembler;

import to.etc.dec.idasm.disassembler.model.Region;

/**
 * Disassembler part which
 */
final public class DataDisassembler implements IDataDisassembler {
	@Override
	public void disassemble(DisContext ctx, Region region) throws Exception {
		switch(region.getType()) {
			default:
				throw new IllegalStateException("Should not be called with type=" + region.getType());

			case ByteData:
				decodeBytes(ctx, region, DataType.Byte);
				return;

			case WordData:
				decodeBytes(ctx, region, DataType.Word);
				return;

			case LongData:
				decodeBytes(ctx, region, DataType.Long);
				return;

			case StringAsciiC:
				//-- Strings terminated with a null byte
				decodeCStrings(ctx, region);

		}
	}

	private void decodeCStrings(DisContext ctx, Region region) {
		StringBuilder sb = new StringBuilder();

		int addr = ctx.getCurrentAddress();
		ctx.mnemonic("db.b");
		boolean instr = false;
		while(addr < region.getEnd()) {
			int val = ctx.byteAt(addr++);
			if(val == 0) {
				if(instr) {
					sb.append("\"");
				}
				sb.append(",0");
				ctx.appendOperand(sb.toString());
				ctx.setCurrentAddress(addr);
				return;
			}

			if(isAsciiValidChar(val)) {
				if(!instr) {
					//-- Append a new string
					if(sb.length() > 0) {
						sb.append(",");
					}
					sb.append("\"");
					instr = true;
				}
				if(val == '\"') {
					sb.append("\\\"");
				} else if(val == '\\') {
					sb.append("\\\\");
				} else {
					sb.append((char) val);
				}
			} else {
				if(instr) {
					sb.append("\"");
					instr = false;
				}
				if(sb.length() > 0) {
					sb.append(",");
				}
				sb.append(ctx.valueInBase(val));
			}
		}

		//-- Terminated without a 0 -> dump
		if(instr) {
			sb.append("\"");
		}
		ctx.appendOperand(sb.toString());
		ctx.setCurrentAddress(addr);
	}

	private static boolean isAsciiValidChar(int c) {
		return c >= 32 && c < 256;
	}

	private void decodeBytes(DisContext ctx, Region region, DataType dataType) {
		int addr = ctx.getCurrentAddress();

		//-- Do we have a span of the same values of the data type?
		long val = ctx.getValueAt(addr, dataType);
		int peekAddr = addr + dataType.getLen();
		while(val == ctx.getValueAt(peekAddr, dataType) && peekAddr < region.getEnd() ) {
			peekAddr += dataType.getLen();
		}
		int rll = peekAddr - addr;
		if(rll >= 4) {
			//-- render as ds.b value, count
			ctx.appendDss(dataType, rll, val);
			ctx.setCurrentAddress(addr + rll * dataType.getLen());
			return;
		}

		int itemLimit = 8 / dataType.getLen();
		int maxBytes = region.getEnd() - ctx.getCurrentAddress();
		int itemsAvail = maxBytes / dataType.getLen();
		if(itemsAvail > itemLimit) {
			itemsAvail = itemLimit;
		}

		//-- Just dump bytes
		StringBuilder sb = new StringBuilder();
		while(itemsAvail-- > 0) {
			val = ctx.getValueAt(addr, dataType);
			if(sb.length() > 0) {
				sb.append(',');
			}
			sb.append(ctx.valueInBase(val));
			addr += dataType.getLen();
		}
		ctx.mnemonic("dd." + dataType.getSuffix());
		ctx.appendOperandPart(sb.toString());
		ctx.setCurrentAddress(addr);
	}
}
