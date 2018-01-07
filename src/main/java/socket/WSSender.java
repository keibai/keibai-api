package main.java.socket;

import javax.websocket.Session;
import java.util.List;

public interface WSSender<T> {
    void send(Session session, T obj);
    void send(List<Session> sessions, T obj);
    void reply(Session session, T originObj, T newObj);
}
