package to.etc.dec.idasm.disassembler.disassembler;

public interface IByteSource {
	int getByte(int address);

	int getStartAddress();

	int getEndAddress();
}
