package main.java.servlets.socket;

import main.java.models.meta.BodyWS;
import org.junit.Test;

import javax.websocket.DecodeException;

import static org.junit.Assert.*;

public class BodyDecoderTest {

    @Test
    public void invalid_should_decode_empty() throws DecodeException {
        String[] INVALID = new String[]{
                "",
                ",",
                "123,123",
                "abc,abc",
        };
        for (String invalidEntry : INVALID) {
            BodyDecoder bodyDecoder = new BodyDecoder();
            BodyWS body = bodyDecoder.decode(invalidEntry);
            assertTrue(body.isEmpty());
        }
    }

    @Test
    public void valid_should_decode_as_BodyWS() throws DecodeException {
        String[][] VALID = new String[][]{
                new String[]{",,", "", "", ""},
                new String[]{"123,123,{}", "123", "123", "{}"},
                new String[]{"abc,def,{ id: 1, amount: 1}", "abc", "def", "{ id: 1, amount: 1}"},
        };
        for (String[] validEntry: VALID) {
            BodyDecoder bodyDecoder = new BodyDecoder();
            BodyWS body = bodyDecoder.decode(validEntry[0]);
            assertEquals(body.type, validEntry[1]);
            assertEquals(body.nonce, validEntry[2]);
            assertEquals(body.json, validEntry[3]);
        }
    }
}