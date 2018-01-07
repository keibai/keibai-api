package main.java.socket;

import main.java.models.meta.BodyWS;
import main.java.utils.HttpSession;

import javax.websocket.Session;

public interface WS {
    void onOpen(Session session, HttpSession httpSession);
    void onMessage(Session session, BodyWS message);
    void onClose(Session session);
    void onError(Session session, Throwable throwable);
}
