package web.core;

import javax.servlet.http.HttpSession;

/**
 * HttpSession wrapper class
 */
public final class Session {
  private HttpSession servletSession;

  Session(HttpSession servletSession) {
    this.servletSession = servletSession;
  }

  public Object get(String attribute) {
    return servletSession.getAttribute(attribute);
  }

  public Object get(String attribute, Object defaultValue) {
    Object sessionAttr = servletSession.getAttribute(attribute);

    if (sessionAttr == null) {
      sessionAttr = defaultValue;
      servletSession.setAttribute(attribute, defaultValue);
    }

    return sessionAttr;
  }

  public void set(String attribute, Object value) {
    servletSession.setAttribute(attribute, value);
  }

  public String getId() {
    return servletSession.getId();
  }

  public void destroy() {
    servletSession.invalidate();
  }
}
