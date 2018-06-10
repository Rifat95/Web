package web.core;

import javax.servlet.http.HttpServletRequest;

/**
 * HttpServletRequest wrapper class
 */
public final class Request {
    private App app;
    private HttpServletRequest servletRequest;

    Request(App app, HttpServletRequest servletRequest) {
        this.app = app;
        this.servletRequest = servletRequest;
    }

    public String getURI() {
        return servletRequest.getRequestURI().substring(app.getSetting("context.path").length());
    }

    public String get(String param) {
        String requestParam = servletRequest.getParameter(param);
        return requestParam != null ? requestParam : "";
    }

    public String[] getAll(String param) {
        String[] requestParam = servletRequest.getParameterValues(param);
        return requestParam != null ? requestParam : new String[0];
    }

    public String getIpAddress() {
        return servletRequest.getRemoteAddr();
    }

    public boolean contains(String... params) {
        for (String param : params) {
            if (servletRequest.getParameter(param) == null) {
                return false;
            }
        }

        return true;
    }

    public boolean isGet() {
        return servletRequest.getMethod().equals("GET");
    }

    public boolean isPost() {
        return servletRequest.getMethod().equals("POST");
    }
}
