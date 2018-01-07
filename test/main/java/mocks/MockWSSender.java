package main.java.mocks;

import main.java.socket.WSSender;

import javax.websocket.Session;
import java.util.List;

public class MockWSSender<T> implements WSSender<T> {

    public Session sessionLastSend;
    public T objLastSend;

    public List<Session> sessionsLastListSend;
    public T objLastListSend;

    public Session sessionLastReply;
    public T originObjLastReply;
    public T newObjLastReply;

    @Override
    public void send(Session session, T obj) {
        sessionLastSend = session;
        objLastSend = obj;
    }

    @Override
    public void send(List<Session> sessions, T obj) {
        sessionsLastListSend = sessions;
        objLastListSend = obj;
    }

    @Override
    public void reply(Session session, T originObj, T newObj) {
        sessionLastReply = session;
        originObjLastReply = originObj;
        newObjLastReply = newObj;
    }
}
