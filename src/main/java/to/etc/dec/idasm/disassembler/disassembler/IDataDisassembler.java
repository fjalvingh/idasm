package to.etc.dec.idasm.disassembler.disassembler;

import to.etc.dec.idasm.disassembler.model.Region;

public interface IDataDisassembler {
	void disassemble(DisContext ctx, Region region) throws Exception;
}
