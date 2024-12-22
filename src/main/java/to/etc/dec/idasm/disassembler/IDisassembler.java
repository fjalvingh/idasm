package to.etc.dec.idasm.disassembler;

public interface IDisassembler {
	void disassemble(DisContext context) throws Exception;

	/**
	 * Sets default options for this disassembly.
	 */
	void configureDefaults(DisContext context) throws Exception;
}
