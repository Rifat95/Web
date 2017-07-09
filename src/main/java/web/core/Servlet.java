package web.core;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import web.util.ForbiddenException;
import web.util.NotFoundException;
import web.util.RedirectionException;
import web.util.Util;

public final class Servlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static String directory;
	private static Route[] routes;

	public static String getPath(String file) {
		return directory + "/" + file;
	}

	@Override
	public void init() {
		directory = getServletContext().getRealPath("/WEB-INF");

		try {
			JSONArray array = (JSONArray) Util.getJson("conf/routes.json");
			Iterator<?> it = array.iterator();
			routes = new Route[array.size()];
			String pkg = getServletContext().getInitParameter("package") + ".";

			int i = 0;
			while (it.hasNext()) {
				JSONObject json = (JSONObject) it.next();
				String uri = (String) json.get("uri");
				Class<?> controller = Class.forName(pkg + json.get("controller"));
				Method action = getMethod((String) json.get("action"), controller);
				String permission = (String) json.get("permission");

				routes[i] = new Route(uri, controller, action, permission);
				i++;
			}
		} catch (Exception e) {
			e.printStackTrace();
			// Fatal error
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
		String uri = request.getRequestURI()
			.substring(getServletContext().getContextPath().length());
		App app = App.getInstance();
		app.init(request, response);

		try {
			if (request.getMethod().equals("POST")
					&& !request.getParameter("token").equals(app.getSession().getId())) {
				throw new ForbiddenException();
			}

			Route route = getRoute(uri);
			if (!app.access(route.getPermission())) {
				//...
				throw new ForbiddenException();
			}

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
		}

		app.clean();
	}

	private Route getRoute(String uri) {
		Matcher m;

		for (Route r : routes) {
			m = Pattern.compile(r.getUri()).matcher(uri);
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
