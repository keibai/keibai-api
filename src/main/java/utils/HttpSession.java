package main.java.utils;

import main.java.models.User;

import javax.servlet.http.HttpServletRequest;

public class HttpSession {
    public static final String USER_ID_KEY = "user";

    private final javax.servlet.http.HttpSession session;

    public HttpSession(HttpServletRequest request) {
        this.session = request.getSession();
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
