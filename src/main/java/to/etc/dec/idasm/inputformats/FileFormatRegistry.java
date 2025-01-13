package to.etc.dec.idasm.inputformats;

import org.eclipse.jdt.annotation.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileFormatRegistry {
	static final private List<IFileFormat> m_formatList = new ArrayList<>();

	private FileFormatRegistry() {}

	public static void register(IFileFormat fileFormat) {
		m_formatList.add(fileFormat);
	}

	public static List<IFileFormat> getFormatList() {
		return m_formatList;
	}

	@Nullable
	public static IFileFormat findFormat(File file) throws Exception {
		for(IFileFormat ff : getFormatList()) {
			if(ff.accept(file)) {
				return ff;
			}
		}
		return null;
	}

	@Nullable
	public static IFileFormat findFormatByName(String name) throws Exception {
		for(IFileFormat ff : getFormatList()) {
			if(ff.name().equalsIgnoreCase(name)) {
				return ff;
			}
		}
		return null;
	}

	static {
		register(new BinLoader());
		register(new BicLoader());
	}

}
