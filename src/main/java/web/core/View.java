package web.core;

import com.mitchellbosecke.pebble.PebbleEngine;
import com.mitchellbosecke.pebble.error.PebbleException;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import web.exception.ServerException;

public final class View {
  private String template;
  private Map<String, Object> data;

  public View(String name) {
    template = name;
    data = new HashMap<>();
  }

  public View set(String attribute, Object value) {
    data.put(attribute, value);
    return this;
  }

  @Override
  public String toString() {
    App app = App.getInstance();
    PebbleEngine engine = (PebbleEngine) Servlet.getAttribute("templateEngine");

    data.put("u", Util.getInstance());
    data.put("t", app.getT());
    data.put("messages", app.getPage().getMessages());

    try {
      StringWriter sw = new StringWriter();
      engine.getTemplate(template).evaluate(sw, data);
      return sw.toString();
    } catch (PebbleException | IOException e) {
      throw new ServerException(e);
    }
  }
}
