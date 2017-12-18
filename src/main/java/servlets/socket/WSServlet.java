package main.java.servlets.socket;

import main.java.models.meta.Message;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint(value = "/ws", decoders = MessageDecoder.class, encoders = MessageEncoder.class)
public class WSServlet {

    private Session session;
    private static final Set<WSServlet> chatEndpoints = new CopyOnWriteArraySet<>();

    @OnOpen
    public void onOpen(Session session) throws IOException {
        System.out.println("Hola");
        this.session = session;
        chatEndpoints.add(this);
        // Get session and WebSocket connection
    }

    @OnMessage
    public void onMessage(Session session, Message message) throws IOException {
        // Handle new messages
        System.out.println(message);
        try {
            Message newMessage = new Message();
            newMessage.setContent("received" + message.getContent());
            broadcast(newMessage);
        } catch (EncodeException e) {
            e.printStackTrace();
        }
    }

    @OnClose
    public void onClose(Session session) throws IOException {
        // WebSocket connection closes
        chatEndpoints.remove(this);
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        // Do error handling here
    }

    private static void broadcast(Message message)
            throws IOException, EncodeException {

        chatEndpoints.forEach(endpoint -> {
            synchronized (endpoint) {
                try {
                    System.out.println(message);
                    endpoint.session.getBasicRemote().
                            sendObject(message);
                } catch (IOException | EncodeException e) {
                    e.printStackTrace();
                }
            }
        });
    }

}