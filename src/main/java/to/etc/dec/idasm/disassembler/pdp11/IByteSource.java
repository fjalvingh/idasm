package to.etc.dec.idasm.disassembler.pdp11;

public interface IByteSource {
	int getByte(int address);

	int getStartAddress();

	int getEndAddress();
}
