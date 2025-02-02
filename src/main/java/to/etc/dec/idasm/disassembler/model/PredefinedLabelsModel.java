package to.etc.dec.idasm.disassembler.model;

import to.etc.dec.idasm.disassembler.disassembler.AddrTarget;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * This is a per disassembler predefined
 * label model. How it gets loaded is decided on by
 * the disassembler chosen.
 */
public class PredefinedLabelsModel {
	private Documentation m_currentDoc;

	private final Map<String, Documentation> m_docMap = new HashMap<>();

	public void loadLabels(InfoModel model, String resourceName) throws Exception {
		InputStream is = getClass().getResourceAsStream("/" + resourceName);
		if(null == is) {
			throw new RuntimeException("Resource not found: " + resourceName);
		}
		int linenr = 1;
		try(LineNumberReader lnr = new LineNumberReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
			String line;

			while((line = lnr.readLine()) != null) {
				handleLabelLine(model, line, linenr);
				linenr++;
			}
		}
	}

	/**
	 * Label line format:
	 * <pre>
	 *	address		Label		DocRef/-	Description
	 * </pre>
	 * Data is separated by one or more tabs.
	 */
	private void handleLabelLine(InfoModel model, String line, int linenr) {
		if(line.startsWith("#") || line.isBlank()) {
			return;
		}
		String[] segments = line.split("\t+");
		if(segments.length != 4 && segments.length != 3) {
			throw new IllegalStateException("Invalid line " + linenr + ": " +  line);
		}

		try {
			long address = decodeAddress(segments[0]);
			String label = segments[1];
			if(! isValidLabel(label)) {
				throw new IllegalStateException("Invalid label " + label);
			}
			String docLabel = segments[2];
			Documentation doc= null;
			if(!docLabel.startsWith("-")) {
				if(!isValidLabel(docLabel)) {
					throw new IllegalStateException("Invalid document label " + docLabel);
				}
				doc = m_docMap.get(docLabel);
				if(null == doc) {
					throw new IllegalStateException("Unknown document label " + docLabel);
				}
			}

			String desc = segments.length == 4 ? segments[3].trim() : null;
			if(desc != null && desc.isBlank()) {
				desc = null;
			}

			appendLabel(model, (int) address, label, doc, desc);
		} catch(Exception x) {
			throw new IllegalStateException("Invalid line " + linenr + ": " + x);
		}
	}

	protected void appendLabel(InfoModel model, int address, String label, Documentation doc, String desc) {
		Label lbl = model.setLabel(LabelType.Predefined, address, label, AddrTarget.Data);
		lbl.updatePredefined(doc, desc);
	}

	private long decodeAddress(String segment) {
		if(segment.startsWith("0x") || segment.startsWith("0X")) {
			return Long.parseLong(segment.substring(2), 16);
		}
		if(segment.startsWith("0")) {
			return Long.parseLong(segment.substring(1), 8);
		}
		return Long.parseLong(segment);
	}


	public void loadDocumentation(String resourceName) throws Exception {
		InputStream is = getClass().getResourceAsStream("/" + resourceName);
		if(null == is) {
			throw new RuntimeException("Resource not found: " + resourceName);
		}
		try(LineNumberReader lnr = new LineNumberReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
			String line;

			m_currentDoc = null;
			while((line = lnr.readLine()) != null) {
				handleDocLine(line);
			}
		}
	}

	private void handleDocLine(String line) throws Exception {
		if(line.startsWith("#") || line.isBlank()) {
			return;
		}
		char c = line.charAt(0);
		if(Character.isWhitespace(c)) {
			//-- Must be a link
			String[] split = line.trim().split("\\|");
			Documentation cd = m_currentDoc;
			if(null == cd) {
				throw new IllegalStateException("No document current");
			}
			String name = split[0];
			String url = split.length > 1 ? split[1] : null;
			String pages = split.length > 2 ? split[2] : null;
			DocumentationLink link = new DocumentationLink(url, name, pages);
			cd.addLink(link);
		} else {
			//-- Must be a new document label
			String label = line.trim();
			if(!isValidLabel(label))
				throw new IllegalStateException("Invalid label: " + label);
			m_currentDoc = new Documentation(label);
			m_docMap.put(label, m_currentDoc);
		}
	}

	private boolean isValidLabel(String label) {
		for(int i = 0; i < label.length(); i++) {
			char c = label.charAt(i);
			if(!Character.isLetterOrDigit(c)) {
				return false;
			}
		}
		return true;
	}


}
