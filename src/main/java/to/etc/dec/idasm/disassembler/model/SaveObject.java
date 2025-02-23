package to.etc.dec.idasm.disassembler.model;

import java.util.ArrayList;
import java.util.List;

public class SaveObject {
	private int m_version;

	private List<Region> m_regionList = new ArrayList<Region>();

	private List<Label> m_userLabelList = new ArrayList<>();

	private List<Comment> m_blockCommentList = new ArrayList<>();

	private List<Comment> m_lineCommentList = new ArrayList<>();

	public int getVersion() {
		return m_version;
	}

	public void setVersion(int version) {
		m_version = version;
	}

	public List<Region> getRegionList() {
		return m_regionList;
	}

	public void setRegionList(List<Region> regionList) {
		m_regionList = regionList;
	}

	public List<Label> getUserLabelList() {
		return m_userLabelList;
	}

	public void setUserLabelList(List<Label> userLabelList) {
		m_userLabelList = userLabelList;
	}

	public List<Comment> getBlockCommentList() {
		return m_blockCommentList;
	}

	public void setBlockCommentList(List<Comment> blockCommentList) {
		m_blockCommentList = blockCommentList;
	}

	public List<Comment> getLineCommentList() {
		return m_lineCommentList;
	}

	public void setLineCommentList(List<Comment> lineCommentList) {
		m_lineCommentList = lineCommentList;
	}
}
