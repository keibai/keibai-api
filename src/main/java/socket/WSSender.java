package main.java.socket;

import main.java.models.meta.BodyWS;

import javax.websocket.EncodeException;
import javax.websocket.Session;
import java.io.IOException;
import java.util.List;

public class WSSender {

    /**
     * Send the body as a text message.
     * Session will be synchronized while sending the data.
     */
    void send(Session session, BodyWS body) {
        synchronized (session) {
            try {
                session.getBasicRemote().sendObject(body);
            } catch (IOException | EncodeException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Send the body to each of the sessions, one by one, as a text messages.
     * Sessions will be synchronized while sending the data (as they are being used).
     * {@link #send(List, BodyWS)}
     */
    void send(List<Session> sessions, BodyWS body) {
        for (Session session: sessions) {
            send(session, body);
        }
    }

    /**
     * Reply to a request.
     * This method will overwrite the type and nonce of the newBody, with the originBody values.
     */
    void reply(Session session, BodyWS originBody, BodyWS newBody) {
        newBody.type = originBody.type;
        newBody.nonce = originBody.nonce;

        send(session, newBody);
    }
}
