package to.etc.dec.idasm.disassembler.pdp11;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.dec.idasm.disassembler.disassembler.AddrTarget;
import to.etc.dec.idasm.disassembler.disassembler.DataDisassembler;
import to.etc.dec.idasm.disassembler.disassembler.DisContext;
import to.etc.dec.idasm.disassembler.disassembler.IDataDisassembler;
import to.etc.dec.idasm.disassembler.disassembler.IDisassembler;
import to.etc.dec.idasm.disassembler.disassembler.Label;
import to.etc.dec.idasm.disassembler.disassembler.NumericBase;

import java.util.List;

public class PdpDisassembler implements IDisassembler {
	@Override public void configureDefaults(DisContext context) throws Exception {
		context.setBase(NumericBase.Oct);
	}

	@NonNull @Override public IDataDisassembler getDataDisassembler() {
		return new DataDisassembler();
	}

	@Override public int getAddressSizeInBits() {
		return 16;
	}

	@Override public int getMaxMnemonicSize() {
		return 7;
	}

	@Override public int getMaxInstructionSizeInChars(NumericBase base) {
		switch(base) {
			default:
				throw new IllegalStateException(base + "??");

			case Oct:
				return 6 * 4;

			case Hex:
				return 4 * 4;
		}
	}

	@Override public void disassemble(DisContext ctx) throws Exception {
		int inst = ctx.getWordLE();

		boolean byteMode = (inst & 0100000) != 0;

		int part = inst & 0170000;
		AddrTarget target = AddrTarget.Data;
		if(part != 00170000 && part != 00070000 && part != 0 && part != 0100000) {

			switch(inst & 0170000){
				case 0010000:
				case 0110000:
					ctx.mnemonicB("mov", byteMode);
					break;

				case 0020000:
				case 0120000:
					ctx.mnemonicB("cmp", byteMode);
					break;

				case 0030000:
				case 0130000:
					ctx.mnemonicB("bit", byteMode);
					break;

				case 0040000:
				case 0140000:
					ctx.mnemonicB("bic", byteMode);
					break;

				case 0050000:
				case 0150000:
					ctx.mnemonicB("bis", byteMode);
					break;

				case 0060000:
					ctx.mnemonic("add");
					break;

				case 0160000:
					ctx.mnemonic("sub");
					break;

				case 0170000:
					ctx.mnemonic("mul");
					ctx.appendOperandPart(reg(inst >> 6));
					ctx.appendOperandPart(",");
					ctx.appendOperandPart(decodeAddressing(ctx, inst, AddrTarget.Data));
					return;

				case 0171000:
					ctx.mnemonic("div");
					ctx.appendOperandPart(reg(inst >> 6));
					ctx.appendOperandPart(",");
					ctx.appendOperandPart(decodeAddressing(ctx, inst, AddrTarget.Data));
					return;

				case 0172000:
					ctx.mnemonic("ash");
					ctx.appendOperandPart(reg(inst >> 6));
					ctx.appendOperandPart(",");
					ctx.appendOperandPart(decodeAddressing(ctx, inst, AddrTarget.Data));
					return;

				case 0173000:
					ctx.mnemonic("ashc");
					ctx.appendOperandPart(reg(inst >> 6));
					ctx.appendOperandPart(",");
					ctx.appendOperandPart(decodeAddressing(ctx, inst, AddrTarget.Data));
					return;

				case 0174000:
					ctx.mnemonic("xor");
					ctx.appendOperandPart(reg(inst >> 6));
					ctx.appendOperandPart(",");
					ctx.appendOperandPart(decodeAddressing(ctx, inst, AddrTarget.Data));
					return;

				case 0077000:
					ctx.mnemonic("sob");
					ctx.appendOperandPart(reg(inst >> 6));
					ctx.appendOperandPart(",");
					int off = inst & 0x3f;
					if((off & 0x20) != 0)
						off |= 0xffffffc0;
					off = ctx.getCurrentAddress() + 2 * off;
					Label label = ctx.addAutoLabel(off, AddrTarget.Code);
					ctx.appendOperandPart(label.getName());
					return;
			}

			String source = decodeAddressing(ctx, (inst >> 6) & 077, AddrTarget.Data);
			String dest = decodeAddressing(ctx, inst & 077, AddrTarget.Data);
			ctx.appendOperandPart(source);
			ctx.appendOperandPart(",");
			ctx.appendOperandPart(dest);
			return;
		}

		switch((inst >> 6) & 0x1ff){
			case 0001:
				target = AddrTarget.Code;
				ctx.mnemonic("jmp");
				break;

			case 0003:
				ctx.mnemonic("swab");
				break;

			case 00050:
			case 01050:
				ctx.mnemonicB("clr", byteMode);
				break;

			case 00051:
			case 01051:
				ctx.mnemonicB("com", byteMode);
				break;

			case 00052:
			case 01052:
				ctx.mnemonicB("inc", byteMode);
				break;

			case 00053:
			case 01053:
				ctx.mnemonicB("dec", byteMode);
				break;

			case 00054:
			case 01054:
				ctx.mnemonicB("neg", byteMode);
				break;

			case 00055:
			case 01055:
				ctx.mnemonicB("adc", byteMode);
				break;

			case 00056:
			case 01056:
				ctx.mnemonicB("sbc", byteMode);
				break;

			case 00057:
			case 01057:
				ctx.mnemonicB("tst", byteMode);
				break;

			case 00060:
			case 01060:
				ctx.mnemonicB("ror", byteMode);
				break;

			case 00061:
			case 01061:
				ctx.mnemonicB("rol", byteMode);
				break;

			case 00062:
			case 01062:
				ctx.mnemonicB("asr", byteMode);
				break;

			case 00063:
			case 01063:
				ctx.mnemonicB("asl", byteMode);
				break;

			case 01064:
				ctx.mnemonicB("mtps", byteMode);
				break;

			case 00065:
				ctx.mnemonic("mfpi");
				break;

			case 01065:
				ctx.mnemonic("mfpd");
				break;

			case 00066:
				ctx.mnemonic("mtpi");
				break;

			case 01066:
				ctx.mnemonic("mtpd");
				break;

			case 00067:
				ctx.mnemonic("sxt");
				break;

			case 01067:
				ctx.mnemonic("mfps");
				break;
		}
		if(!ctx.getOpcodeString().isEmpty()) {
			ctx.appendOperandPart(decodeAddressing(ctx, inst, target));
			return;
		}

		//-- Branches
		//System.out.println(">> " + Integer.toOctalString(inst >> 8) + " " + Integer.toOctalString(inst));
		switch(inst >> 8){
			case 0001:
				ctx.mnemonic("br");
				break;

			case 0002:
				ctx.mnemonic("bne");
				break;

			case 0003:
				ctx.mnemonic("beq");
				break;

			case 0004:
				ctx.mnemonic("bge");
				break;

			case 0005:
				ctx.mnemonic("blt");
				break;

			case 0006:
				ctx.mnemonic("bgt");
				break;

			case 0007:
				ctx.mnemonic("ble");
				break;

			case 0200:
				ctx.mnemonic("bpl");
				break;

			case 0201:
				ctx.mnemonic("bmi");
				break;

			case 0202:
				ctx.mnemonic("bhi");
				break;

			case 0203:
				ctx.mnemonic("blos");
				break;

			case 0204:
				ctx.mnemonic("bvc");
				break;

			case 0205:
				ctx.mnemonic("bvs");
				break;

			case 0206:
				ctx.mnemonic("bcc");
				break;

			case 0207:
				ctx.mnemonic("bcs");
				break;
		}

		if(!ctx.getOpcodeString().isEmpty()) {
			int offset = inst & 0xff;
			if((offset & 0x80) != 0) {
				//-- -ve
				offset |= 0xffffff00;
			}
			int addr = ctx.getCurrentAddress() + offset * 2;
			Label label = ctx.addAutoLabel(addr, AddrTarget.Code);
			ctx.appendOperandPart(label.getName());
			return;
		}

		if((inst >> 9) == 004) {            // jsr
			ctx.mnemonic("jsr");
			ctx.appendOperandPart(reg(inst >> 6));
			ctx.appendOperandPart(",");
			ctx.appendOperandPart(decodeAddressing(ctx, inst, AddrTarget.Code));
			return;
		}

		if((inst >> 3) == 020) {
			ctx.mnemonic("rts");
			ctx.appendOperandPart(reg(inst));
			return;
		}

		if((inst >> 6) == 0064) {
			ctx.mnemonic("mark");
			ctx.appendOperandPart(ctx.valueInBase(inst & 0x3f));
			return;
		}

		if((inst >> 8) == 0210) {
			ctx.mnemonic("emt");
			ctx.appendOperandPart(ctx.valueInBase(inst & 0xff));
			return;
		}
		if((inst >> 8) == 0211) {
			ctx.mnemonic("trap");
			ctx.appendOperandPart(ctx.valueInBase(inst & 0xff));
			return;
		}

		if(inst == 0240 || inst == 0260) {
			//-- Scc/Ccc without flags = nop
			ctx.mnemonic("nop");
			return;
		}

		if((inst >> 4) == 012) {
			//Cnn
			ctx.mnemonic("Ccc");
			ctx.appendOperandPart(decodeFlags(inst));
			return;
		}
		if((inst >> 4) == 013) {
			//Cnn
			ctx.mnemonic("Scc");
			ctx.appendOperandPart(decodeFlags(inst));
			return;
		}

		switch(inst){
			case 0:
				ctx.mnemonic("halt");
				return;

			case 1:
				ctx.mnemonic("wait");
				return;

			case 02:
				ctx.mnemonic("rti");
				return;

			case 03:
				ctx.mnemonic("bpt");
				return;
			case 04:
				ctx.mnemonic("iot");
				return;

			case 05:
				ctx.mnemonic("reset");
				return;

			case 06:
				ctx.mnemonic("rtt");
				return;
		}

		ctx.mnemonic("ILLEGAL");
	}

	private String decodeFlags(int inst) {
		StringBuilder sb = new StringBuilder();
		if((inst & 0x8) != 0)
			sb.append("N");
		if((inst & 0x4) != 0)
			sb.append("Z");
		if((inst & 0x2) != 0)
			sb.append("V");
		if((inst & 0x1) != 0)
			sb.append("C");
		return sb.toString();
	}

	private String decodeAddressing(DisContext ctx, int pat, AddrTarget target) {
		int m = (pat >> 3) & 07;
		int r = (pat & 07);

		if(r == 7) {
			switch(m){
				case 2:
					int imm = ctx.getWordLE();
					if(target == AddrTarget.Code) {
						//-- jump target
						Label label = ctx.addAutoLabel(imm, target);
						return label.getName();
					}
					List<Label> labels = ctx.getLabels(imm);
					if(labels != null) {
						return "#" + labels.get(0).getName();
					}

					return "#" + ctx.valueInBase(imm);

				case 3:
					int abs = ctx.getWordLE();
					if(target == AddrTarget.Code) {
						Label label = ctx.addAutoLabel(abs, target);
						return label.getName();
					}

					labels = ctx.getLabels(abs);
					if(labels != null) {
						return "@#" + labels.get(0).getName();
					}
					return "@#" + ctx.valueInBase(abs);

				case 6:
					int relpc = ctx.getWordLE();
					Label label = ctx.addAutoLabel(relpc * 2 + ctx.getCurrentAddress(), target);
					return label.getName() + "(pc)";

				case 7:
					relpc = ctx.getWordLE();
					label = ctx.addAutoLabel(relpc * 2 + ctx.getCurrentAddress(), target);
					return "@" + label.getName() + "(pc)";
			}
		}
		switch(m){
			default:
				throw new IllegalStateException("? mode=" + m);
			case 0:
				return reg(r);                // Rn

			case 1:
				return "(" + reg(r) + ")";    // (Rn)

			case 2:
				return "(" + reg(r) + ")+";    // (Rn)+

			case 3:
				return "@(" + reg(r) + ")+";

			case 4:
				return "-(" + reg(r) + ")";

			case 5:
				return "@-(" + reg(r) + ")";

			case 6:
				int index = ctx.getWordLE();
				Label label = ctx.addAutoLabel(index, target);
				return label.getName() + "(" + reg(r) + ")";

			case 7:
				index = ctx.getWordLE();
				label = ctx.addAutoLabel(index, target);
				return "@" + label.getName() + "(" + reg(index) + ")";
		}
	}

	private String reg(int regno) {
		regno &= 07;
		if(regno == 7)
			return "pc";
		if(regno == 6)
			return "sp";
		return "r" + regno;
	}
}
