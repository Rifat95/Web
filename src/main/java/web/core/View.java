package web.core;

import com.mitchellbosecke.pebble.error.PebbleException;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import web.util.ServerException;
import web.util.Util;

public final class View {
	private String file;
	private HashMap<String, Object> data;

	public View(String name) {
		file = "/WEB-INF/templates/" + name + ".tpl";
		data = new HashMap<>();
	}

	public HashMap<String, Object> getData() {
		return data;
	}

	public View setData(HashMap<String, Object> map) {
		data = map;
		return this;
	}

	public View add(String var, Object value) {
		data.put(var, value);
		return this;
	}

	@Override
	public String toString() {
		data.put("u", Util.getInstance());
		data.put("t", App.getInstance().getT());

		try {
			StringWriter sw = new StringWriter();
			Servlet.getEngine().getTemplate(file).evaluate(sw, data);
			return sw.toString();
		} catch (PebbleException | IOException e) {
			throw new ServerException(e);
		}
	}
}
