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

		//-- Comments
		so.setBlockCommentList(new ArrayList<>(m_blockCommentMap.values()));
		so.setLineCommentList(new ArrayList<>(m_lineCommentMap.values()));

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

			List<Comment> cmtList = so.getBlockCommentList();
			if(null != cmtList) {
				for(Comment cmt : cmtList) {
					m_blockCommentMap.put(cmt.getAddress(), cmt);
				}
			}

			cmtList = so.getLineCommentList();
			if(null != cmtList) {
				for(Comment cmt : cmtList) {
					m_lineCommentMap.put(cmt.getAddress(), cmt);
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

	@Nullable
	public Label getLabel(int address) {
		return m_labelMap.get(address);
	}


	/*----------------------------------------------------------------------*/
	/*	CODING:	Comments													*/
	/*----------------------------------------------------------------------*/
	private final Map<Integer, Comment> m_blockCommentMap = new HashMap<>();

	@Nullable
	public Comment getBlockComment(int address) {
		return m_blockCommentMap.get(address);
	}

	public void setBlockComment(int address, String text) throws Exception {
		Comment cmt = m_blockCommentMap.get(address);
		if(null == cmt) {
			cmt = new Comment(address, text);
			m_blockCommentMap.put(address, cmt);
		} else {
			cmt.setComment(text);
		}
		save();
	}

	private final Map<Integer, Comment> m_lineCommentMap = new HashMap<>();

	@Nullable
	public Comment getLineComment(int address) {
		return m_lineCommentMap.get(address);
	}

	public void setLineComment(int address, @Nullable String text) throws Exception {
		Comment cmt = m_lineCommentMap.get(address);
		if(null == cmt) {
			if(text == null)
				return;
			cmt = new Comment(address, text);
			m_lineCommentMap.put(address, cmt);
		} else if(null == text) {
			m_lineCommentMap.remove(address);
		} else {
			cmt.setComment(text);
		}
		save();
	}



}
