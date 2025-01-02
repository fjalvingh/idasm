package to.etc.dec.idasm.disassembler.disassembler;

final public class Label {
	private final long m_address;

	private final String m_name;

	private final AddrTarget m_type;

	private int[] m_usedFrom = new int[10];

	private int m_usedFromIndex;

	public Label(long address, String name, AddrTarget type) {
		m_address = address;
		m_name = name;
		m_type = type;
	}

	public long getAddress() {
		return m_address;
	}

	public String getName() {
		return m_name;
	}

	public AddrTarget getType() {
		return m_type;
	}

	public Label from(int addr) {
		if(m_usedFromIndex >= m_usedFrom.length) {
			int[] nw = new int[m_usedFromIndex + 20];
			System.arraycopy(m_usedFrom, 0, nw, 0, m_usedFromIndex);
			m_usedFrom = nw;
		}
		m_usedFrom[m_usedFromIndex++] = addr;
		return this;
	}

	public int[] getXrefs() {
		int[] xrefs = new int[m_usedFromIndex];
		System.arraycopy(m_usedFrom, 0, xrefs, 0, m_usedFromIndex);
		return xrefs;
	}

}
