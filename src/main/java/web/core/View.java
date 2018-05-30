package web.core;

import com.mitchellbosecke.pebble.error.PebbleException;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import web.util.ServerException;
import web.util.Util;

public final class View {
    private String template;
    private HashMap<String, Object> data;

    public View(String name) {
        template = name;
        data = new HashMap<>();
    }

    public void set(String var, Object value) {
        data.put(var, value);
    }

    @Override
    public String toString() {
        App app = App.getInstance();
        data.put("u", Util.getInstance());
        data.put("t", app.getT());
        data.put("messages", app.getPage().getMessages());

        try {
            StringWriter sw = new StringWriter();
            Servlet.getTemplateEngine().getTemplate(template).evaluate(sw, data);
            return sw.toString();
        } catch (PebbleException | IOException e) {
            throw new ServerException(e);
        }
    }
}
