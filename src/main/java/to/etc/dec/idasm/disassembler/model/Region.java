package to.etc.dec.idasm.disassembler.model;

/**
 * This describes a region of non-code. Must be marshallable as JSON.
 */
public class Region {
	private int m_start;

	private int m_end;

	private RegionType m_type;

	public Region() {
	}

	public Region(int start, int end, RegionType type) {
		m_start = start;
		m_end = end;
		m_type = type;
	}

	public int getStart() {
		return m_start;
	}

	public void setStart(int start) {
		m_start = start;
	}

	public int getEnd() {
		return m_end;
	}

	public void setEnd(int end) {
		m_end = end;
	}

	public RegionType getType() {
		return m_type;
	}

	public void setType(RegionType type) {
		m_type = type;
	}

	public void update(RegionType type, int start, int end) {
		m_type = type;
		m_start = start;
		m_end = end;
	}
}
