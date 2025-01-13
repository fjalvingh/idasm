package to.etc.dec.idasm.inputformats;

import to.etc.dec.idasm.disassembler.disassembler.IByteSource;

import java.io.File;
import java.io.FileInputStream;

final public class BinLoader extends AbstractFileFormat {
	public BinLoader() {
		super("bin", "Binary file");
	}

	@Override public IByteSource read(File file) throws Exception {
		int len = (int) file.length();
		try(FileInputStream fis = new FileInputStream(file)) {
			byte[] memory = fis.readAllBytes();
			return new FileByteSource(memory, 0);
		}
	}

	@Override public boolean accept(File input) throws Exception {
		return "bin".equalsIgnoreCase(FileUtil.getSuffix(input.getName()));
	}
}
