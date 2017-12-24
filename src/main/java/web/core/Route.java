package web.core;

import java.lang.reflect.Method;

public final class Route {
	private String uri;
	private Class<?> controller;
	private Method action;
	private String permission;
	private String[] params;
	private boolean token;

	public Route(String uri, Class<?> controller, Method action, String permission, boolean token) {
		this(uri, controller, action, permission, token, null);
	}

	public Route(String uri, Class<?> controller, Method action, String permission, boolean token, String[] params) {
		this.uri = uri;
		this.controller = controller;
		this.action = action;
		this.permission = permission;
		this.token = token;
		this.params = params;
	}

	public String getUri() {
		return uri;
	}

	public Class<?> getController() {
		return controller;
	}

	public Method getAction() {
		return action;
	}

	public String getPermission() {
		return permission;
	}

	public boolean hasToken() {
		return token;
	}

	public String[] getParams() {
		return params;
	}
}
