package to.etc.dec.idasm.inputformats;

abstract class AbstractFileFormat implements IFileFormat {
	private final String m_name;
	private final String m_description;

	public AbstractFileFormat(String name, String description) {
		m_name = name;
		m_description = description;
	}

	@Override public String description() {
		return m_description;
	}

	@Override public String name() {
		return m_name;
	}
}
