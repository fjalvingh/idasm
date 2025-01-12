package to.etc.dec.idasm.disassembler.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import to.etc.dec.idasm.disassembler.disassembler.AddrTarget;

/**
 * Label class, also able to be saved as JSON.
 */
final public class Label {
	private int m_address;

	private String m_name;

	private AddrTarget m_type;

	private int[] m_usedFrom = new int[10];

	private int m_usedFromIndex;

	private boolean m_userDefined;

	public Label(int address, String name, AddrTarget type, boolean userDefined) {
		m_address = address;
		m_name = name;
		m_type = type;
		m_userDefined = userDefined;
	}

	public int getAddress() {
		return m_address;
	}

	public void setAddress(int address) {
		m_address = address;
	}

	public String getName() {
		return m_name;
	}

	public void setName(String name) {
		m_name = name;
	}

	public AddrTarget getType() {
		return m_type;
	}

	public void setType(AddrTarget type) {
		m_type = type;
	}

	@JsonIgnore
	public boolean isUserDefined() {
		return m_userDefined;
	}

	public void setUserDefined(boolean userDefined) {
		m_userDefined = userDefined;
	}

	@JsonIgnore
	public Label from(int addr) {
		if(m_usedFromIndex >= m_usedFrom.length) {
			int[] nw = new int[m_usedFromIndex + 20];
			System.arraycopy(m_usedFrom, 0, nw, 0, m_usedFromIndex);
			m_usedFrom = nw;
		}
		m_usedFrom[m_usedFromIndex++] = addr;
		return this;
	}

	@JsonIgnore
	public int[] getXrefs() {
		int[] xrefs = new int[m_usedFromIndex];
		System.arraycopy(m_usedFrom, 0, xrefs, 0, m_usedFromIndex);
		return xrefs;
	}
}
