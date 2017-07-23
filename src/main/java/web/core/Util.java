package web.core;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import org.json.simple.parser.JSONParser;

/**
 * Static singleton class
 */
public final class Util {
	private static final Util instance = new Util();

	private Util() {}

	/**
	 * For templates only.
	 *
	 * @return The static instance
	 */
	public static Util getInstance() {
		return instance;
	}

	public static Object getJson(String file) throws Exception {
		return new JSONParser()
			.parse(new InputStreamReader(new FileInputStream(path(file))));
	}

	public static String path(String file) {
		return Servlet.getDirectory()+ "/" + file;
	}

	public static String uri(String path) {
		return Servlet.getContext() + "/" + path;
	}

	public static String file(String path) {
		return uri("public/" + path);
	}
}
