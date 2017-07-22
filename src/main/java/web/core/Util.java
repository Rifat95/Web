package web.core;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import org.json.simple.parser.JSONParser;

public final class Util {
	private Util() {}

	public static Object getJson(String file) throws Exception {
		return new JSONParser()
			.parse(new InputStreamReader(new FileInputStream(Servlet.getPath(file))));
	}
}
