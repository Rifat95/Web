package web.util;

import web.core.App;
import web.core.Servlet;

/**
 * Static singleton class
 */
public final class Util {
	private static final Util INSTANCE = new Util();

	private Util() {}

	/**
	 * Only to pass the class instance to templates in web.core.View.toString().
	 *
	 * @return static class instance
	 */
	public static Util getInstance() {
		return INSTANCE;
	}

	/**
	 * @param path the absolute path
	 * @return web context + path
	 */
	public static String uri(String path) {
		return Servlet.getSetting("context.path") + path;
	}

	/**
	 * For templates only, because web.core.App is not accessible from templates.
	 *
	 * @param permission
	 * @return true if user has the specified permission
	 */
	public static boolean access(String permission) {
		return App.getInstance().access(permission);
	}

	/**
	 * For templates only, because web.core.App is not accessible from templates.
	 *
	 * @return the user session id (token)
	 */
	public static String getToken() {
		return App.getInstance().getSession().getId();
	}
}
