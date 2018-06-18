package web.core;

import java.util.Properties;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(urlPatterns = "/")
public final class Servlet extends HttpServlet {
  private static final long serialVersionUID = 1L;

  private static ServletContext context;
  private static Properties settings;

  public static Object getAttribute(String name) {
    return context.getAttribute(name);
  }

  public static String getSetting(String name) {
    return settings.getProperty(name);
  }

  @Override
  public void init() throws ServletException {
    context = getServletContext();
    settings = (Properties) context.getAttribute("settings");
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) {
    App.init(request, response);
    App.getInstance().run();
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) {
    doGet(request, response);
  }
}
