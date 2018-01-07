package main.java.socket;

import main.java.models.meta.BodyWS;
import main.java.utils.DefaultHttpSession;
import main.java.utils.HttpSession;
import main.java.utils.Logger;

import java.io.IOException;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint(value = "/ws",
        decoders = BodyDecoder.class,
        encoders = BodyEncoder.class,
        configurator = GetHttpSessionConfigurator.class)
public class CoreWS {

    private final WS[] listeners;

    public CoreWS() {
        listeners = new WS[]{
                new BidWS(),
        };
    }

    /**
     * Open WebSocket connection if a HttpSession can be built. Doesn't necessary mean that the user is signed in.
     * Otherwise a 500 response is thrown.
     */
    @OnOpen
    public void onOpen(Session session, EndpointConfig config) throws IOException {
        javax.servlet.http.HttpSession httpSession = (javax.servlet.http.HttpSession) config.getUserProperties()
                .get(javax.servlet.http.HttpSession.class.getName());
        HttpSession servletHttpSession = new DefaultHttpSession(httpSession);

        for (WS listener: listeners) {
            listener.onOpen(session, servletHttpSession);
        }
    }

    @OnMessage
    public void onMessage(Session session, BodyWS body) throws IOException {
        if (body.isEmpty()) {
            return;
        }

        for (WS listener: listeners) {
            listener.onMessage(session, body);
        }
    }

    @OnClose
    public void onClose(Session session) throws IOException {
        for (WS listener: listeners) {
            listener.onClose(session);
        }
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        Logger.error("Socket error", throwable.toString());
        for (WS listener: listeners) {
            listener.onError(session, throwable);
        }
    }
}