package web.core;

import java.lang.reflect.Method;

public final class Route {
    private String uri;
    private Class<?> controller;
    private Method action;
    private String permission;
    private String[] params;
    private boolean token;

    Route(String uri, Class<?> controller, Method action, String permission, boolean token) {
        this(uri, controller, action, permission, token, null);
    }

    Route(String uri, Class<?> controller, Method action, String permission, boolean token, String[] params) {
        this.uri = uri;
        this.controller = controller;
        this.action = action;
        this.permission = permission;
        this.token = token;
        this.params = params;
    }

    String getUri() {
        return uri;
    }

    Class<?> getController() {
        return controller;
    }

    Method getAction() {
        return action;
    }

    String getPermission() {
        return permission;
    }

    boolean hasToken() {
        return token;
    }

    String[] getParams() {
        return params;
    }
}
