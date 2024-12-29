package to.etc.dec.idasm.disassembler;

import org.eclipse.jdt.annotation.NonNull;

public interface IDisassembler {
	void disassemble(DisContext context) throws Exception;

	/**
	 * Sets default options for this disassembly.
	 */
	void configureDefaults(DisContext context) throws Exception;

	@NonNull
	IDataDisassembler	getDataDisassembler();

	/**
	 * Returns the max size of an instruction.
	 */
	int getMaxInstructionSizeInChars(NumericBase base);

	int getAddressSizeInBits();

	int getMaxMnemonicSize();
}
