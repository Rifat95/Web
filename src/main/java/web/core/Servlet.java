package web.core;

import com.mitchellbosecke.pebble.PebbleEngine;
import com.mitchellbosecke.pebble.loader.ServletLoader;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import web.util.ForbiddenException;
import web.util.NotFoundException;

public final class Servlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private static Properties settings;
	private static PebbleEngine templateEngine;
	private static Route[] routes;
	private static Class<MainController> mainClass;
	private static HashMap<String, ResourceBundle> i18nBundles;
	private static HikariDataSource connectionPool;

	public static String getSetting(String name) {
		return settings.getProperty(name);
	}

	static PebbleEngine getTemplateEngine() {
		return templateEngine;
	}

	static ResourceBundle getLanguagePack(String language) {
		return i18nBundles.get(language);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void init() {
		ServletContext servletContext = getServletContext();

		// Configure template engine
		ServletLoader templateLoader = new ServletLoader(servletContext);
		templateLoader.setPrefix("/WEB-INF/templates/");
		templateLoader.setSuffix(".tpl");
		templateEngine = new PebbleEngine.Builder()
			.loader(templateLoader)
			.strictVariables(true)
			.build();

		try {
			ClassLoader loader = servletContext.getClassLoader();

			// Load settings
			settings = new Properties();
			settings.load(loader.getResourceAsStream("conf/settings.properties"));
			settings.put("context.path", servletContext.getContextPath());

			// Load main controller
			mainClass = (Class<MainController>) Class.forName("app.controller.Main");

			// Load routes
			File routeFile = new File(loader.getResource("conf/routes.json").getPath());
			JSONArray routeList = new JSONArray(FileUtils.readFileToString(routeFile, "UTF-8"));
			int nbRoute = routeList.length();
			routes = new Route[nbRoute];

			for (int i = 0; i < nbRoute; i++) {
				JSONObject jo = routeList.getJSONObject(i);
				String uri = jo.getString("uri");
				String[] controllerInfos = jo.getString("controller").split("@");
				String permission = jo.getString("permission");
				boolean token = jo.has("token") ? jo.getBoolean("token") : false;

				Class<?> controller = Class.forName("app.controller." + controllerInfos[0]);
				Method action = getMethod(controllerInfos[1], controller);
				routes[i] = new Route(uri, controller, action, permission, token);
			}

			// Load language packs
			i18nBundles = new HashMap<>();
			String[] languages = Servlet.getSetting("supported.languages").split(",");
			for (String lang : languages) {
				i18nBundles.put(lang, ResourceBundle.getBundle("i18n.strings", new Locale(lang), loader));
			}

			// Load database connections
			Properties dbInfos = new Properties();
			dbInfos.load(loader.getResourceAsStream("conf/db.properties"));
			connectionPool = new HikariDataSource(new HikariConfig(dbInfos));
		} catch (IOException | ClassNotFoundException | NoSuchMethodException | JSONException e) {
			// Fatal error
			e.printStackTrace(); // @todo Replace with logger
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
		String uri = request.getRequestURI().substring(Servlet.getSetting("context.path").length());
		App app = App.getInstance();
		app.init(request, response);
		Page page = app.getPage();
		String token = app.getRequest().get("tk", "");

		try {
			MainController mainController = mainClass.newInstance();
			mainController.init();

			try {
				Route route = getRoute(uri); // Throw web.util.NotFoundException
				String permission = route.getPermission();

				if ((route.hasToken() || request.getMethod().equals("POST")) // Token verification
				&& !token.equals(app.getSession().getId())) {
					throw new ForbiddenException();
				} else if (!permission.equals("all") && !app.access(permission)) { // Permission verification
					throw new ForbiddenException();
				}

				app.setConnection(connectionPool.getConnection());
				route.getAction().invoke(route.getController().newInstance(), (Object[]) route.getParams());
			} catch (Exception e) {
				mainController.handleException(e);
			} finally {
				page.send();
				app.clean();
			}
		} catch (InstantiationException | IllegalAccessException e) {
			// Fatal error
			e.printStackTrace(); // @todo Replace with logger
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

				return new Route(r.getUri(), r.getController(), r.getAction(), r.getPermission(), r.hasToken(), params);
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
