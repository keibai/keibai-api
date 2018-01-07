package main.java.socket;

import main.java.mocks.MockSession;
import main.java.models.meta.BodyWS;
import org.junit.Before;
import org.junit.Test;

import javax.websocket.Session;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class WSSenderTest {

    MockSession mockSession;
    BodyWSSender wsSender;

    @Before
    public void setup() {
        mockSession = new MockSession();
        wsSender = new BodyWSSender();
    }

    @Test
    public void should_send_body() {
        BodyWS body = new BodyWS();
        wsSender.send(mockSession, body);

        assertEquals(body, mockSession.basicRemoteEndpoint.lastSendObject);
    }

    @Test
    public void should_send_body_to_different_recipients() {
        MockSession altMockSession = new MockSession();
        List<Session> sessions = new ArrayList<>();
        sessions.add(mockSession);
        sessions.add(altMockSession);

        BodyWS body = new BodyWS();
        wsSender.send(sessions, body);

        assertEquals(body, mockSession.basicRemoteEndpoint.lastSendObject);
        assertEquals(body, altMockSession.basicRemoteEndpoint.lastSendObject);
    }

    @Test
    public void should_reply_new_body() {
        BodyWS requestBody = new BodyWS();
        requestBody.type = "A";
        requestBody.nonce = "123";
        requestBody.status = 200;
        requestBody.json = "{ foo: 1 }";

        BodyWS replyBody = new BodyWS();
        replyBody.status = 400;
        replyBody.json = "{ error: 2 }";

        wsSender.reply(mockSession, requestBody, replyBody);

        assertEquals(replyBody, mockSession.basicRemoteEndpoint.lastSendObject);
        assertEquals("A", replyBody.type);
        assertEquals("123", replyBody.nonce);
        assertEquals(400, replyBody.status);
        assertEquals("{ error: 2 }", replyBody.json);
    }
}