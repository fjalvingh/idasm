package to.etc.dec.idasm.disassembler.model;

final public class DocumentationLink {
	private final String m_url;

	private final String m_name;

	private final String m_pageReference;

	public DocumentationLink(String url, String name, String pageReference) {
		m_url = url;
		m_name = name;
		m_pageReference = pageReference;
	}

	public String getUrl() {
		return m_url;
	}
	public String getName() {
		return m_name;
	}

	public String getPageReference() {
		return m_pageReference;
	}
}
