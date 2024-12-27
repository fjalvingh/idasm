package to.etc.dec.idasm.disassembler.model;

import java.io.File;

/**
 * This class holds all added/changed information
 * for a disassembly, like code/data regions, custom
 * labels and comment blocks.
 */
final public class InfoModel {
	private final File m_file;

	private final RegionModel m_regionModel = new RegionModel();

	public InfoModel(File file) {
		m_file = file;
	}



}
