package to.etc.dec.idasm.disassembler.model;

public class Comment {
	private int m_address;

	private String m_comment;

	public Comment() {
	}

	public Comment(int address, String comment) {
		m_address = address;
		m_comment = comment;
	}

	public int getAddress() {
		return m_address;
	}

	public void setAddress(int address) {
		m_address = address;
	}

	public String getComment() {
		return m_comment;
	}

	public void setComment(String comment) {
		m_comment = comment;
	}
}
