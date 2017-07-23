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
			.parse(new InputStreamReader(new FileInputStream(Servlet.getPath(file))));
	}

	public static String uri(String location) {
		return Servlet.getContext() + "/" + location;
	}

	public static String file(String location) {
		return uri("public/" + location);
	}
}
