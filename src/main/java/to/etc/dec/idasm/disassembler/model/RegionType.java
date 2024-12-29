package to.etc.dec.idasm.disassembler.model;

public enum RegionType {
	/** While this type is present it should never be in the region list. */
	Code,
	ByteData,
	WordData,
	LongData,
	StringAsciiC,
	StringAsciiByte,
	StringAsciiWordLE,
	StringAsciiWordBE,
	StringAsciiBit7,
}
