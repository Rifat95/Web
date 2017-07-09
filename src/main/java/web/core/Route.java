package web.core;

import java.lang.reflect.Method;

public final class Route {
	private String uri;
	private Class<?> controller;
	private Method action;
	private String permission;
	private Object[] params;

	public Route(String uri, Class<?> controller, Method action, String permission, Object[] params) {
		this.uri = uri;
		this.controller = controller;
		this.action = action;
		this.permission = permission;
		this.params = params;
	}

	public Route(String uri, Class<?> controller, Method action, String permission) {
		this(uri, controller, action, permission, null);
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

	public Object[] getParams() {
		return params;
	}
}
