package main.java.utils;

import javax.servlet.http.HttpServletRequest;

public class HttpSession {
    public static final int SESSION_DURATION = 365 * 24 * 60 * 60; // A year.
    public static final String USER_ID_KEY = "user";

    private final javax.servlet.http.HttpSession session;

    public HttpSession(HttpServletRequest request) {
        this.session = request.getSession();
        this.session.setMaxInactiveInterval(SESSION_DURATION);
    }

    public int userId() {
        Object object = this.get(HttpSession.USER_ID_KEY);
        return object == null ? -1 : (int) object;
    }

    public void save(String key, Object data) {
        this.session.setAttribute(key, data);
    }

    public Object get(String key) {
        return this.session.getAttribute(key);
    }
}
