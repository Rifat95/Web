package web.core;

import com.zaxxer.hikari.HikariDataSource;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import web.util.ForbiddenException;
import web.util.NotFoundException;
import web.util.RedirectionException;

/**
 * Thread local singleton
 */
public final class App {
  private static final ThreadLocal<App> INSTANCE = new ThreadLocal<>();

  private Request request;
  private HttpServletResponse response;
  private Session session;
  private AppUser user;
  private Page page;
  private Translator t;
  private Connection connection;

  public static App getInstance() {
    return INSTANCE.get();
  }

  /**
   * Must be called from core.Servlet.doGet() only.
   *
   * @param request
   * @param response
   */
  static void init(HttpServletRequest request, HttpServletResponse response) {
    INSTANCE.set(new App(request, response));
  }

  private App(HttpServletRequest servletRequest, HttpServletResponse servletResponse) {
    request = new Request(servletRequest);
    response = servletResponse;
    session = new Session(servletRequest.getSession());
    user = (AppUser) session.get("appUser", new AppUser());
    page = new Page(this);
    t = new Translator(this);
  }

  public Request getRequest() {
    return request;
  }

  public Session getSession() {
    return session;
  }

  public AppUser getUser() {
    return user;
  }

  public Page getPage() {
    return page;
  }

  public Translator getT() {
    return t;
  }

  public Connection getConnection() {
    return connection;
  }

  HttpServletResponse getResponse() {
    return response;
  }

  void run() {
    HikariDataSource dataSource = (HikariDataSource) Servlet.getAttribute("dataSource");
    Initializable appInitializer = (Initializable) Servlet.getAttribute("appInitializer");
    String token = request.get("tk");
    Route route = null;

    try {
      route = getRoute();
      String permission = route.getPermission();

      if ((route.hasToken() || request.isPost()) && !token.equals(session.getId())) {
        throw new ForbiddenException("token");
      } else if (!permission.equals("all") && !user.hasPermission(permission)) {
        throw new ForbiddenException(permission);
      }

      connection = dataSource.getConnection();
      appInitializer.onRequestStart(this);
      route.getAction().invoke(route.getController().newInstance(), (Object[]) route.getParams());
      appInitializer.onRequestFinish(this);
    } catch (NotFoundException e) {
      response.setStatus(404);
      appInitializer.handleException(e, this);
    } catch (ForbiddenException e) {
      response.setStatus(403);
      appInitializer.handleException(e, this, route);
    } catch (InvocationTargetException e) {
      Throwable cause = e.getCause();

      if (cause instanceof NotFoundException) {
        response.setStatus(404);
        appInitializer.handleException((NotFoundException) cause, this);
      } else if (cause instanceof RedirectionException) {
        page.setRedirection(cause.getMessage());
      } else {
        response.setStatus(500);
        appInitializer.handleException((Exception) cause, this);
      }
    } catch (IllegalAccessException | IllegalArgumentException | InstantiationException | SQLException e) {
      response.setStatus(500);
      appInitializer.handleException(e, this);
    } finally {
      page.send();
      clean();
    }
  }

  @SuppressWarnings("unchecked")
  private Route getRoute() {
    String uri = request.getUri();
    List<Route> routes = (List<Route>) Servlet.getAttribute("routes");

    for (Route r : routes) {
      Matcher m = Pattern.compile(r.getUri()).matcher(uri);
      if (m.matches()) {
        int nbParam = m.groupCount();
        String[] params = new String[nbParam];

        for (int i = 1; i <= nbParam; i++) {
          params[i - 1] = m.group(i);
        }

        return new Route(r.getUri(), r.getController(), r.getAction(), r.getPermission(), r.hasToken(), params);
      }
    }

    throw new NotFoundException();
  }

  private void clean() {
    if (connection != null) {
      try {
        connection.close();
      } catch (SQLException e) {
        // Ignore
      }
    }

    INSTANCE.remove();
  }
}
