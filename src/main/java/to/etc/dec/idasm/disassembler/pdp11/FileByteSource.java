package to.etc.dec.idasm.disassembler.pdp11;

import java.io.File;
import java.io.FileInputStream;

final public class FileByteSource implements IByteSource {
	private final File m_file;

	private byte[] m_memory;

	public FileByteSource(File file) throws Exception{
		m_file = file;
		int len = (int) file.length();
		try(FileInputStream fis = new FileInputStream(file)) {
			m_memory = fis.readAllBytes();
		}
	}

	@Override public int getByte(int address) {
		if(address > Integer.MAX_VALUE) {
			throw new IndexOutOfBoundsException("address 0x" + Long.toHexString(address) + " is greater than 0x" + Integer.toHexString(Integer.MAX_VALUE));
		}
		return m_memory[(int) address];
	}

	@Override public int getEndAddress() {
		return m_memory.length;
	}

	@Override public int getStartAddress() {
		return 0;
	}
}
