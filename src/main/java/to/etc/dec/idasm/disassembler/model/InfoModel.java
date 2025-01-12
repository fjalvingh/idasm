package to.etc.dec.idasm.disassembler.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.dec.idasm.disassembler.disassembler.AddrTarget;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This class holds all added/changed information
 * for a disassembly, like code/data regions, custom
 * labels and comment blocks.
 */
final public class InfoModel {
	private final File m_file;

	private final RegionModel m_regionModel = new RegionModel();

	private final Map<Integer, Label> m_labelMap = new HashMap<>();

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

		//-- Labels
		List<Label> userLabels = m_labelMap.values().stream()
			.filter(a -> a.isUserDefined())
			.collect(Collectors.toList());
		so.setUserLabelList(userLabels);
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

			m_labelMap.clear();
			List<Label> list = so.getUserLabelList();
			if(null != list) {
				for(Label userLabel : list) {
					m_labelMap.put(userLabel.getAddress(), userLabel);
				}
			}
		}
	}

	public void addRegion(RegionType type, int start, int end) {
		m_regionModel.addRegion(type, start, end);
	}

	public RegionModel getRegionModel() {
		return m_regionModel;
	}


	/*----------------------------------------------------------------------*/
	/*	CODING:	Labels														*/
	/*----------------------------------------------------------------------*/

	/**
	 * Set a user label.
	 */
	public Label setLabel(int address, String label, AddrTarget type) {
		Label old = m_labelMap.get(address);
		if(null == old) {
			old = new Label(address, label, type, true);
			m_labelMap.put(address, old);
			return old;
		}

		//-- Update this label
		old.setType(type);
		old.setName(label);
		old.setUserDefined(true);
		return old;
	}

	/**
	 * Add an automatic label. If a label already exists, either
	 * auto or user, return that one.
	 */
	public Label addAutoLabel(int address, AddrTarget type, String proposedName) {
		Label old = m_labelMap.get(address);
		if(null != old) {
			return old;
		}

		old = new Label(address, proposedName, type, false);
		m_labelMap.put(address, old);
		return old;
	}

	//
	//	List<Label> list = m_labelMap.computeIfAbsent(address, k -> new ArrayList<>());
	//	Label alt = list.stream()
	//		.filter(a -> a.getAddress() == address && a.getName().equals(label))
	//		.findFirst()
	//		.orElse(null);
	//	if(null != alt) {
	//		alt.from(referencedFromAddress);
	//		return alt;
	//	}
	//	//if(m_render)
	//	//	throw new IllegalStateException("Label " + label + " being created after pass 1");
	//	alt = new Label(address, label, type, false).from(referencedFromAddress);
	//	list.add(alt);
	//	return alt;
	//}

	@Nullable
	public Label getLabel(int address) {
		return m_labelMap.get(address);
	}
}
