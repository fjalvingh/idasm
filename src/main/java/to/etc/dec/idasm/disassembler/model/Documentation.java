package to.etc.dec.idasm.disassembler.model;

import java.util.ArrayList;
import java.util.List;

/**
 * A documentation label.
 */
final public class Documentation {
	private final String m_code;

	private final List<DocumentationLink> m_links = new ArrayList<>();

	public Documentation(String code) {
		m_code = code;
	}

	public void addLink(DocumentationLink link) {
		m_links.add(link);
	}

	public String getCode() {
		return m_code;
	}
	public List<DocumentationLink> getLinks() {
		return m_links;
	}
}
