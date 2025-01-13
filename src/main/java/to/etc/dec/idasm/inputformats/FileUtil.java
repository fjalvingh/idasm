package to.etc.dec.idasm.inputformats;

public class FileUtil {
	private FileUtil() {}

	static public String getSuffix(String name) {
		int pos = name.lastIndexOf(".");
		if(pos == -1)
			return "";
		return name.substring(pos + 1);
	}

}
