package web.core;

import com.mitchellbosecke.pebble.PebbleEngine;
import com.mitchellbosecke.pebble.loader.ServletLoader;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import web.util.ForbiddenException;
import web.util.JsonObject;
import web.util.NotFoundException;
import web.util.RedirectionException;

public final class Servlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private static String webContext;
	private static PebbleEngine engine;
	private static Route[] routes;
	private static HashMap<String, ResourceBundle> i18nBundles;
	private static HikariDataSource connectionPool;

	public static String getWebContext() {
		return webContext;
	}

	public static PebbleEngine getEngine() {
		return engine;
	}

	public static ResourceBundle getBundle(String language) {
		return i18nBundles.get(language);
	}

	@Override
	public void init() {
		ServletContext sc = getServletContext();
		ClassLoader loader = sc.getClassLoader();
		webContext = sc.getContextPath();
		engine = new PebbleEngine.Builder()
			.loader(new ServletLoader(sc))
			.strictVariables(true)
			.build();

		try {
			// Load routes
			String pkg = sc.getInitParameter("package") + ".controller.";
			InputStream is = loader.getResourceAsStream("conf/routes.json");
			JSONArray array = (JSONArray) new JSONParser().parse(new InputStreamReader(is));
			Iterator<?> it = array.iterator();
			routes = new Route[array.size()];

			int i = 0;
			while (it.hasNext()) {
				JsonObject jo = new JsonObject((JSONObject) it.next());
				String uri = jo.get("uri");
				Class<?> controller = Class.forName(pkg + jo.get("controller"));
				Method action = getMethod(jo.get("action"), controller);
				String permission = jo.get("permission");
				routes[i] = new Route(uri, controller, action, permission);
				i++;
			}

			// Load strings
			i18nBundles = new HashMap<>();
			String[] languages = sc.getInitParameter("languages").split(",");
			for (String lang : languages) {
				i18nBundles.put(lang, ResourceBundle.getBundle("i18n.strings", new Locale(lang), loader));
			}

			// Load database connections
			Properties dbInfos = new Properties();
			dbInfos.load(loader.getResourceAsStream("conf/db.properties"));
			connectionPool = new HikariDataSource(new HikariConfig(dbInfos));
		} catch (Exception e) {
			e.printStackTrace();
			// Fatal error
		}
	}

	@Override
	public void destroy() {
		if (connectionPool != null) {
			connectionPool.close();
		}
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) {
		process(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) {
		process(request, response);
	}

	private void process(HttpServletRequest request, HttpServletResponse response) {
		String uri = request.getRequestURI().substring(webContext.length());
		App app = App.getInstance();
		app.init(request, response);
		Page page = app.getPage();

		try {
			if (request.getMethod().equals("POST")) {
				String token = request.getParameter("token");
				if (token == null || !token.equals(request.getSession().getId())) {
					throw new ForbiddenException();
				}
			}

			Route route = getRoute(uri);
			if (!app.access(route.getPermission())) {
				//...
				throw new ForbiddenException();
			}

			app.setConnection(connectionPool.getConnection());
			Object controller = route.getController().newInstance();
			route.getAction().invoke(controller, route.getParams());
		} catch (NotFoundException e) {
			// Error 404
			e.printStackTrace();
		} catch (ForbiddenException e) {
			// Error 403
			e.printStackTrace();
		} catch (RedirectionException e) {
			// Redirection
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			if (e.getCause() instanceof NotFoundException) {
				// Error 404
				e.printStackTrace();
			} else if (e.getCause() instanceof RedirectionException) {
				// Redirection
				e.printStackTrace();
			} else {
				// Error 500
				e.printStackTrace();
			}
		} catch (Exception e) {
			// Error 500
			e.printStackTrace();
		} finally {
			page.send();
			app.clean();
		}
	}

	private Route getRoute(String uri) {
		for (Route r : routes) {
			Matcher m = Pattern.compile(r.getUri()).matcher(uri);
			if (m.matches()) {
				int nbParam = m.groupCount();
				Object[] params = new Object[nbParam];
				for (int i = 1; i <= nbParam; i++) {
					params[i - 1] = m.group(i);
				}

				return new Route(r.getUri(), r.getController(), r.getAction(),
					r.getPermission(), params);
			}
		}

		throw new NotFoundException();
	}

	private Method getMethod(String name, Class<?> c) throws NoSuchMethodException {
		for (Method m : c.getDeclaredMethods()) {
			if (m.getName().equals(name)) {
				return m;
			}
		}

		throw new NoSuchMethodException();
	}
}
