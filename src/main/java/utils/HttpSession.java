package main.java.utils;

public interface HttpSession {

    int userId();
    void save(String key, Object data);
    Object get(String key);
}
