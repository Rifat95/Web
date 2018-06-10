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

  public Object get(String attr) {
    return servletSession.getAttribute(attr);
  }

  public Object get(String attr, Object defaultValue) {
    Object sessionAttr = servletSession.getAttribute(attr);

    if (sessionAttr == null) {
      sessionAttr = defaultValue;
      servletSession.setAttribute(attr, defaultValue);
    }

    return sessionAttr;
  }

  public void set(String attr, Object value) {
    servletSession.setAttribute(attr, value);
  }

  public String getId() {
    return servletSession.getId();
  }

  public void destroy() {
    servletSession.invalidate();
  }
}
