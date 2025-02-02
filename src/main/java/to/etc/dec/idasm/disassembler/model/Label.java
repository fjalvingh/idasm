package to.etc.dec.idasm.disassembler.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.dec.idasm.disassembler.disassembler.AddrTarget;

/**
 * Label class, also able to be saved as JSON.
 */
final public class Label {
	private int m_address;

	private String m_name;

	private LabelType m_type;

	private AddrTarget m_targetType;

	private int[] m_usedFrom = new int[10];

	private int m_usedFromIndex;

	/**
	 * For predefined labels: the documentation, if present
	 */
	@Nullable
	private Documentation m_documentation;

	/**
	 * For predefined labels: the description.
	 */
	@Nullable
	private String m_description;

	public Label() {
	}

	public Label(LabelType type, int address, String name, AddrTarget targetType) {
		m_type = type;
		m_address = address;
		m_name = name;
		m_targetType = targetType;
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

	public AddrTarget getTargetType() {
		return m_targetType;
	}

	public void setTargetType(AddrTarget targetType) {
		m_targetType = targetType;
	}

	@JsonIgnore
	public LabelType getType() {
		return m_type;
	}

	public void setType(LabelType type) {
		m_type = type;
	}

	@JsonIgnore
	@Nullable public Documentation getDocumentation() {
		return m_documentation;
	}

	@JsonIgnore
	@Nullable public String getDescription() {
		return m_description;
	}

	public void updatePredefined(@Nullable Documentation documentation, @Nullable String description) {
		m_documentation = documentation;
		m_description = description;
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
