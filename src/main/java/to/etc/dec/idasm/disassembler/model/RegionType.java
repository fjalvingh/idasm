package to.etc.dec.idasm.disassembler.model;

public enum RegionType {
	/** While this type is present it should never be in the region list. */
	Code,
	ByteData,
	WordDataBE,
	WordDataLE,
	LongDataBE,
	LongDataLE,
	StringAsciiC,
	StringAsciiByte,
	StringAsciiWordLE,
	StringAsciiWordBE,
	StringAsciiBit7,
}
