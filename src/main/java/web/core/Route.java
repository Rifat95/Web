package web.core;

import java.lang.reflect.Method;

public final class Route {
  private String uri;
  private Class<? extends Controller> controller;
  private Method action;
  private String permission;
  private boolean token;
  private String[] params;

  Route(String uri, Class<? extends Controller> controller, Method action, String permission, boolean token) {
    this(uri, controller, action, permission, token, null);
  }

  Route(String uri, Class<? extends Controller> controller, Method action, String permission, boolean token, String[] params) {
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

  Class<? extends Controller> getController() {
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
