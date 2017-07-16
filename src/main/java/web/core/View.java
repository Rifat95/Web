package web.core;

import com.mitchellbosecke.pebble.PebbleEngine;
import com.mitchellbosecke.pebble.loader.FileLoader;
import java.io.StringWriter;
import java.util.HashMap;
import web.util.NotFoundException;

public final class View {
	public static final String VIEW = "view/";
	public static final String ENTITY = "entity/";
	public static final String ROOT = "";
	public static final PebbleEngine ENGINE = new PebbleEngine.Builder()
		.loader(new FileLoader())
		.strictVariables(true)
		.build();

	private String file;
	private HashMap<String, Object> data;

	public View(String name) {
		this(name, VIEW);
	}

	public View(String name, String type) {
		file = Servlet.getPath("views/" + type + name + ".tpl");
		data = new HashMap<>();
	}

	public HashMap<String, Object> getData() {
		return data;
	}

	public View add(String var, Object value) {
		data.put(var, value);
		return this;
	}

	@Override
	public String toString() {
		try {
			StringWriter sw = new StringWriter();
			ENGINE.getTemplate(file).evaluate(sw, data);
			return sw.toString();
		} catch (Exception e) {
			e.printStackTrace();
			throw new NotFoundException();
		}
	}
}
