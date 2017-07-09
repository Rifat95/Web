package web.core;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Thread local singleton class
 */
public final class App {
	private static final ThreadLocal<App> instance = new ThreadLocal<App>() {
		@Override
		protected App initialValue() {
			return new App();
		}
	};

	private HttpServletRequest request;
	private HttpServletResponse response;
	private HttpSession session;
	private Page page;
	private int userId;
	private String[] userPermissions;

	private App() {}

	public static App getInstance() {
		return instance.get();
	}

	public HttpServletRequest getRequest() {
		return request;
	}

	public HttpServletResponse getResponse() {
		return response;
	}

	public HttpSession getSession() {
		return session;
	}

	public Object getSessAttr(String name, Object defaultValue) {
		if (session.getAttribute(name) == null) {
			session.setAttribute(name, defaultValue);
		}

		return session.getAttribute(name);
	}

	public Page getPage() {
		return page;
	}

	public int getUserId() {
		return userId;
	}

	public void setUser(int id, String[] permissions) {
		userId = id;
		userPermissions = permissions;
	}

	public boolean access(String permission) {
		for (String s : userPermissions) {
			if (s.equals(permission)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Must be called at the start of core.Servlet.process() only.
	 */
	void init(HttpServletRequest request, HttpServletResponse response) {
		this.request = request;
		this.response = response;
		session = request.getSession();
		page = new Page();
		userId = (int) getSessAttr("userId", 0);
		userPermissions = (String[]) getSessAttr("userPermissions", new String[]{"guest"});
	}

	/**
	 * Must be called at the end of core.Servlet.process() only, to avoid memory leak.
	 */
	void clean() {
		instance.remove();
	}
}
