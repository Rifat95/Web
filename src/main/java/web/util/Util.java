package web.util;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import org.json.simple.parser.JSONParser;
import web.core.Servlet;

/**
 * Static class
 */
public final class Util {
	private Util() {}

	public static Object getJson(String file) throws Exception {
		return new JSONParser()
			.parse(new InputStreamReader(new FileInputStream(Servlet.getPath(file))));
	}

	public static int getInt(String s) {
		try {
			return Integer.parseInt(s);
		} catch (NumberFormatException e) {
			return 0;
		}
	}
}
