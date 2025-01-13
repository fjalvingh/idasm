package to.etc.dec.idasm.inputformats;


import to.etc.dec.idasm.disassembler.disassembler.IByteSource;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

/**
 * Uses the BIC input format for PDP-11 / XXDP
 */
public class BicLoader extends AbstractFileFormat {
	public BicLoader() {
		super("bic", "XXDP BIC Format (DEC pdp-11)");
	}

	@Override public IByteSource read(File input) throws Exception {
		try(InputStream is = new FileInputStream(input)) {
			BicReader bicReader = new BicReader(is);
			return bicReader.run();
		}
	}

	@Override public boolean accept(File input) throws Exception {
		return "bic".equalsIgnoreCase(FileUtil.getSuffix(input.getName()));
	}

	static class BicReader {
		private final InputStream m_is;

		/** Input file offset, for error debugging */
		private int m_offset;

		private int m_end;

		/** The lowest load address. */
		private int m_loadAddress = 65536;

		private final byte[] m_memory = new byte[64 * 1024];

		public BicReader(InputStream is) {
			m_is = is;
		}

		IByteSource run() throws Exception {
			for(;;) {
				int newEnd = readBlock();
				if(newEnd == -1) {
					break;
				}
				if(newEnd > m_end) {
					m_end = newEnd;
				}
			}

			int start = m_loadAddress;
			int end = m_end;
			byte[] data = new byte[m_end - start];				// #bytes actually read
			System.arraycopy(m_memory, start, data, 0, m_end - start);
			return new FileByteSource(data, start);
		}

		/**
		 * Format:
		 * <pre>
		 *	Byte 1 - Contains a value of 1 to indicate starting point.
		 * 	Byte 2 - Contains a value of 0. This must follow byte 1.
		 * 	Bytes 3 and 4 - Contains number of bytes (N) in this binary block. It includes bytes 1, 2, 3
		 *  		and 4 but excludes the checksum byte.
		 *	Byte 5 and 6 - Contains the starting memory address where the following data bytes are
		 *		to be stored.
		 *	Byte 7 to N - Data bytes. N <= 509. The maximum number of data bytes is 503.
		 *	Byte N+1 - Contains the checksum byte. The checksum is the two's complement of the sum
		 *  	of the data in all N bytes. It is generated ignoring overflow and carry conditions.
		 * </pre>
		 */
		private int readBlock() throws Exception {
			int b = m_is.read();
			if(b == -1) {
				return -1;
			}
			m_offset++;
			if(b != 1) {
				error("Invalid record start byte: 0x" + Integer.toHexString(b));
			}
			b = read();
			if(b != 0)
				error("Invalid record 2nd byte: 0x" + Integer.toHexString(b));

			int len = readWord();
			if(len > 509 || len < 6)
				error("Invalid data length " + len + ", expecting len <= 509 and >= 6");
			int start = readWord();
			len -= 6;
			if(len == 0)
				return -1;								// End of file

			if(start + len > m_memory.length)
				error("start address 0x" + Integer.toHexString(start) + " and len 0x" + Integer.toHexString(len) + " overflows 64K");
			if(start < m_loadAddress)
				m_loadAddress = start;
			int bytes = m_is.read(m_memory, start, len);
			if(bytes != len)
				error("Failed to read " + len + " bytes, got " + bytes);
			m_offset += bytes;
			int sum = read();						// Forget about the sum

			System.out.println("Loaded 0x"
				+ Integer.toHexString(len)
				+ " (0" + Integer.toOctalString(len) + " oct)"
				+ " bytes at 0x"
				+ Integer.toHexString(start)
				+ " (0" + Integer.toOctalString(start) + " oct)"
			);
			return start + len;
		}

		private int readWord() throws Exception {
			int w = read();
			int v = read();
			return w | (v << 8);
		}

		private int read() throws Exception {
			int r = m_is.read();
			if(r == -1) {
				error("Unexpected end of file");
			}
			return r & 0xff;
		}

		private void error(String s) {
			throw new RuntimeException(s + " at offset 0x" + Integer.toHexString(m_offset));
		}


	}

}
