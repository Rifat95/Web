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

	private static ServletContext servletContext;
	private static PebbleEngine engine;
	private static Route[] routes;
	private static HashMap<String, ResourceBundle> i18nBundles;
	private static HikariDataSource connectionPool;

	public static String getWebContext() {
		return servletContext.getContextPath();
	}

	static String getInitParam(String name) {
		return servletContext.getInitParameter(name);
	}

	static PebbleEngine getEngine() {
		return engine;
	}

	static ResourceBundle getBundle(String language) {
		return i18nBundles.get(language);
	}

	@Override
	public void init() {
		servletContext = getServletContext();
		engine = new PebbleEngine.Builder()
			.loader(new ServletLoader(servletContext))
			.strictVariables(true)
			.build();

		try {
			ClassLoader loader = servletContext.getClassLoader();

			// Load routes
			String pkg = getInitParam("package") + ".controller.";
			InputStream is = loader.getResourceAsStream("conf/routes.json");
			JSONArray array = (JSONArray) new JSONParser().parse(new InputStreamReader(is));
			Iterator<?> it = array.iterator();
			routes = new Route[array.size()];

			int i = 0;
			while (it.hasNext()) {
				JsonObject jo = new JsonObject((JSONObject) it.next());
				String uri = jo.getStr("uri");
				Class<?> controller = Class.forName(pkg + jo.getStr("controller"));
				Method action = getMethod(jo.getStr("action"), controller);
				String permission = jo.getStr("permission");
				boolean token = jo.getBool("token");

				routes[i] = new Route(uri, controller, action, permission, token);
				i++;
			}

			// Load strings
			i18nBundles = new HashMap<>();
			String[] languages = getInitParam("languages").split(",");
			for (String lang : languages) {
				i18nBundles.put(lang, ResourceBundle.getBundle("i18n.strings", new Locale(lang), loader));
			}

			// Load database connections
			Properties dbInfos = new Properties();
			dbInfos.load(loader.getResourceAsStream("conf/db.properties"));
			connectionPool = new HikariDataSource(new HikariConfig(dbInfos));
		} catch (Exception e) {
			// Fatal error
			System.exit(-1);
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
		String uri = request.getRequestURI().substring(getWebContext().length());
		App app = App.getInstance();
		app.init(request, response);
		Page page = app.getPage();
		String token = app.getRequest().get("tk", "");

		try {
			Route route = getRoute(uri); // Throw web.util.NotFoundException

			// Token verification
			if ((route.hasToken() || request.getMethod().equals("POST"))
			&& !token.equals(app.getSession().getId())) {
				throw new ForbiddenException();
			} else if (!app.access(route.getPermission())) {
				throw new ForbiddenException();
			}

			app.setConnection(connectionPool.getConnection());
			Object controller = route.getController().newInstance();
			route.getAction().invoke(controller, (Object[]) route.getParams());
		} catch (ForbiddenException e) {
			setError(app, response, 403);
		} catch (NotFoundException e) {
			setError(app, response, 404);
		} catch (InvocationTargetException e) {
			if (e.getCause() instanceof NotFoundException) {
				setError(app, response, 404);
			} else if (e.getCause() instanceof RedirectionException) {
				page.setRedirection(e.getMessage());
			} else {
				setError(app, response, 500);
			}
		} catch (Exception e) {
			setError(app, response, 500);
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
				String[] params = new String[nbParam];

				for (int i = 1; i <= nbParam; i++) {
					params[i - 1] = m.group(i);
				}

				return new Route(r.getUri(), r.getController(), r.getAction(), r.getPermission(),
					r.hasToken(), params);
			}
		}

		throw new NotFoundException();
	}

	private void setError(App app, HttpServletResponse response, int code) {
		Page p = app.getPage();
		p.setTitle(app.getT().t("core.error", code));
		p.setView(new View("core/error-" + code));
		response.setStatus(code);
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
