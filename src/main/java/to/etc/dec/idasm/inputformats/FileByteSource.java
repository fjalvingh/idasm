package to.etc.dec.idasm.inputformats;

import to.etc.dec.idasm.disassembler.disassembler.IByteSource;

final public class FileByteSource implements IByteSource {
	private final int m_offset;

	private final int m_endAddress;

	private byte[] m_memory;

	public FileByteSource(byte[] memory, int addressOffset) {
		m_memory = memory;
		m_offset = addressOffset;
		m_endAddress = addressOffset + memory.length;
	}

	@Override public int getByte(int address) {
		if(address < m_offset) {
			throw new IndexOutOfBoundsException("address 0x" + Long.toHexString(address) + " is below the input file's offset 0x" + Integer.toHexString(m_offset) );
		}
		if(address >= m_endAddress) {
			throw new IndexOutOfBoundsException("address 0x" + Long.toHexString(address) + " is at/after the last address, 0x" + Integer.toHexString(m_endAddress) );
		}
		return m_memory[(int) address - m_offset] & 0xff;
	}

	@Override public int getEndAddress() {
		return m_endAddress;
	}

	@Override public int getStartAddress() {
		return m_offset;
	}
}
