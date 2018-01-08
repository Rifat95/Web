package web.util;

import java.util.ArrayList;
import java.util.Arrays;
import web.core.Servlet;

// Static singleton class
public final class Util {
	private static final Util INSTANCE = new Util();

	private Util() {}

	// To pass the class instance to templates
	public static Util getInstance() {
		return INSTANCE;
	}

	public static String uri(String path) {
		return Servlet.getWebContext() + path;
	}

	public static ArrayList<String> strList(String... values) {
		return new ArrayList<>(Arrays.asList(values));
	}
}
