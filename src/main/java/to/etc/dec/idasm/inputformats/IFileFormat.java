package to.etc.dec.idasm.inputformats;

import to.etc.dec.idasm.disassembler.disassembler.IByteSource;

import java.io.File;

public interface IFileFormat {
	boolean accept(File input) throws Exception;

	IByteSource read(File input) throws Exception;

	String name();

	String description();
}
