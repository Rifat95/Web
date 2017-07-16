package web.core;

import javax.servlet.http.HttpServletRequest;

/**
 * HttpServletRequest wrapper class
 */
public final class Request {
	private HttpServletRequest servletRequest;

	public Request(HttpServletRequest servletRequest) {
		this.servletRequest = servletRequest;
	}

	public String get(String param) {
		return servletRequest.getParameter(param);
	}

	public String get(String param, String defaultValue) {
		String requestParam = servletRequest.getParameter(param);
		return requestParam != null ? requestParam : defaultValue;
	}
}
