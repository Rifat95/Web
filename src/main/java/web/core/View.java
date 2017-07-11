package web.core;

import com.mitchellbosecke.pebble.template.PebbleTemplate;
import java.io.StringWriter;
import java.util.HashMap;
import web.util.NotFoundException;

public final class View {
	public static final String VIEW = "view/";
	public static final String ENTITY = "entity/";

	private String file;
	private HashMap<String, Object> data;

	public View(String name) {
		this(name, VIEW);
	}

	public View(String name, String type) {
		file = Servlet.getPath("views/" + type + name + ".tpl");
		data = new HashMap<>();
	}

	public View add(String var, Object value) {
		data.put(var, value);
		return this;
	}

	@Override
	public String toString() {
		try {
			PebbleTemplate tpl = Servlet.getEngine().getTemplate(file);
			StringWriter sw = new StringWriter();
			tpl.evaluate(sw, data);

			return sw.toString();
		} catch (Exception e) {
			e.printStackTrace();
			throw new NotFoundException();
		}
	}
}
