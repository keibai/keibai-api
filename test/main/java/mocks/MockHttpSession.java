package main.java.mocks;

import main.java.utils.HttpSession;

public class MockHttpSession implements HttpSession {

    int storedUserId;

    @Override
    public int userId() {
        return storedUserId;
    }

    public void setUserId(int value) {
        storedUserId = value;
    }

    @Override
    public void save(String key, Object data) {

    }

    @Override
    public Object get(String key) {
        return null;
    }
}
