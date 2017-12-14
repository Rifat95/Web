package web.core;

import java.sql.Connection;
import java.sql.SQLException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// Thread local singleton class
public final class App {
	private static final ThreadLocal<App> INSTANCE = new ThreadLocal<App>() {
		@Override
		protected App initialValue() {
			return new App();
		}
	};

	private Request request;
	private HttpServletResponse response;
	private Session session;
	private Page page;
	private int userId;
	private String[] userPermissions;
	private Translator t;
	private Connection connection;

	private App() {}

	public static App getInstance() {
		return INSTANCE.get();
	}

	public Request getRequest() {
		return request;
	}

	public HttpServletResponse getResponse() {
		return response;
	}

	public Session getSession() {
		return session;
	}

	public Page getPage() {
		return page;
	}

	public int getUserId() {
		return userId;
	}

	public Translator getT() {
		return t;
	}

	public Connection getConnection() {
		return connection;
	}

	public void setConnection(Connection c) {
		connection = c;
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
	void init(HttpServletRequest servletRequest, HttpServletResponse servletResponse) {
		request = new Request(servletRequest);
		response = servletResponse;
		session = new Session(servletRequest.getSession());
		page = new Page();
		userId = (int) session.get("userId", 0);
		userPermissions = (String[]) session.get("userPermissions", new String[]{"guest"});
		t = new Translator();
	}

	/**
	 * Must be called at the end of core.Servlet.process() only, to avoid memory leak.
	 */
	void clean() {
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
