package web.core;

import com.zaxxer.hikari.HikariDataSource;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletContext;
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

    private ServletContext context;
    private Properties settings;
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
     * Must be called from core.Servlet only.
     */
    static void init(HttpServletRequest request, HttpServletResponse response) {
        INSTANCE.set(new App(request, response));
    }

    private App(HttpServletRequest servletRequest, HttpServletResponse servletResponse) {
        context = servletRequest.getServletContext();
        settings = (Properties) context.getAttribute("settings");
        request = new Request(this, servletRequest);
        response = servletResponse;
        session = new Session(servletRequest.getSession());
        user = (AppUser) session.get("appUser", new AppUser());
        page = new Page(this);
        t = new Translator(this);
    }

    public String getSetting(String name) {
        return settings.getProperty(name);
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

    ServletContext getContext() {
        return context;
    }

    HttpServletResponse getResponse() {
        return response;
    }

    void run() {
        try {
            Route route = getRoute();
            String permission = route.getPermission();
            String token = request.get("tk");

            if ((route.hasToken() || request.isPost()) && !token.equals(session.getId())) {
                throw new ForbiddenException("token");
            } else if (!permission.equals("all") && !user.hasPermission(permission)) {
                throw new ForbiddenException(permission);
            }

            HikariDataSource dataSource = (HikariDataSource) context.getAttribute("dataSource");
            connection = dataSource.getConnection();
            route.getAction().invoke(route.getController().newInstance(), (Object[]) route.getParams());
        } catch (NotFoundException e) {
            response.setStatus(404);
            // mainController.handleException(e);
        } catch (ForbiddenException e) {
            response.setStatus(403);
            // mainController.handleException(e);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();

            if (cause instanceof NotFoundException) {
                response.setStatus(404);
                // mainController.handleException((NotFoundException) cause);
            } else if (cause instanceof RedirectionException) {
                page.setRedirection(cause.getMessage());
            } else {
                response.setStatus(500);
                // mainController.handleException((Exception) cause);
            }
        } catch (Exception e) {
            response.setStatus(500);
            // mainController.handleException(e);
        } finally {
            page.send();
            clean();
        }
    }

    private Route getRoute() {
        String uri = request.getURI();
        Route[] routes = (Route[]) context.getAttribute("routes");

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
