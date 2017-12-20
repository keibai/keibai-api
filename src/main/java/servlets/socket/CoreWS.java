package main.java.servlets.socket;

import main.java.models.meta.MsgWS;
import main.java.utils.Logger;

import java.io.IOException;

import javax.servlet.http.HttpSession;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint(value = "/ws",
        decoders = MessageDecoder.class,
        encoders = MessageEncoder.class,
        configurator = GetHttpSessionConfigurator.class)
public class CoreWS {

    private final WS[] listeners;

    public CoreWS() {
        listeners = new WS[]{
                new BidWS(),
        };
    }

    @OnOpen
    public void onOpen(Session session, EndpointConfig config) throws IOException {
        HttpSession httpSession = (HttpSession) config.getUserProperties()
                .get(HttpSession.class.getName());
        main.java.utils.HttpSession servletHttpSession = new main.java.utils.HttpSession(httpSession);

        for (WS listener: listeners) {
            listener.onOpen(session, servletHttpSession);
        }
    }

    @OnMessage
    public void onMessage(Session session, MsgWS message) throws IOException {
        if (message.isEmpty()) {
            System.out.println("is empty");
            return;
        }

        for (WS listener: listeners) {
            listener.onMessage(session, message);
        }
    }

    @OnClose
    public void onClose(Session session) throws IOException {
        System.out.println("on close");
        for (WS listener: listeners) {
            listener.onClose(session);
        }
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        Logger.error("Socket error", throwable.toString());
        throwable.printStackTrace();
        for (WS listener: listeners) {
            listener.onClose(session);
        }
    }

//    private static void broadcast(MsgWS message)
//            throws IOException, EncodeException {
//
//        chatEndpoints.forEach(endpoint -> {
//            synchronized (endpoint) {
//                try {
//                    System.out.println(message);
//                    endpoint.session.getBasicRemote().
//                            sendObject(message);
//                } catch (IOException | EncodeException e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//    }

}