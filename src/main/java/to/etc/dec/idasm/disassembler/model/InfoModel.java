package to.etc.dec.idasm.disassembler.model;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

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

	/**
	 * Save the current model.
	 */
	public void save() throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();

		SaveObject so = new SaveObject();
		List<Region> list = new ArrayList<>(m_regionModel.getRegionCount());
		for(int i = 0; i < m_regionModel.getRegionCount(); i++) {
			Region r = m_regionModel.getRegionByIndex(i);
			if(r.getType() != RegionType.Code) {
				list.add(r);
			}
		}
		so.setVersion(1);
		so.setRegionList(list);
		try(FileOutputStream fos = new FileOutputStream(m_file)) {
			objectMapper.writeValue(fos, so);
		}
	}

	/**
	 * (re)load the model.
	 */
	public void load() throws Exception {
		if(!m_file.exists()) {
			return;
		}
		ObjectMapper objectMapper = new ObjectMapper();
		try(FileInputStream fis = new FileInputStream(m_file)) {
			SaveObject so = objectMapper.readValue(fis, SaveObject.class);

			m_regionModel.initializeFrom(so.getRegionList());
		}
	}

	public void addRegion(RegionType type, int start, int end) {
		m_regionModel.addRegion(type, start, end);
	}

	public RegionModel getRegionModel() {
		return m_regionModel;
	}
}
