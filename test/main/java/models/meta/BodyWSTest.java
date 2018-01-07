package main.java.models.meta;

import org.junit.Test;

import static org.junit.Assert.*;

public class BodyWSTest {

    @Test
    public void invalid_should_decode_empty() {
        String[] INVALID = new String[]{
                "",
                ",",
                "123,123",
                "abc,abc",
                "123,123,123",
                "abc,abc,abc",
                "abc,abc,abc," // status should be numeric.
        };
        for (String invalidEntry: INVALID) {
            BodyWS body = BodyWS.fromString(invalidEntry);
            assertTrue(body.isEmpty());
        }
    }

    @Test
    public void valid_should_decode_as_BodyWS() {
        String[][] VALID = new String[][]{
                new String[]{",,200,", "", "", ""},
                new String[]{"123,123,400,{}", "123", "123", "{}"},
                new String[]{"abc,def,500,{ id: 1, amount: 1 }", "abc", "def", "{ id: 1, amount: 1 }"},
        };
        for (String[] validEntry: VALID) {
            BodyWS body = BodyWS.fromString(validEntry[0]);
            assertEquals(body.type, validEntry[1]);
            assertEquals(body.nonce, validEntry[2]);
            assertEquals(body.json, validEntry[3]);
        }
    }

    @Test
    public void should_encode_with_comas() {
        BodyWS body = new BodyWS();
        body.type = "abc";
        body.nonce = "123";
        body.json = "{ id: 1, amount: 1 }";
        assertEquals("abc,123,200,{ id: 1, amount: 1 }", body.toString());
    }

    @Test
    public void BodyWS_status_code_should_be_200_by_default() {
        BodyWS body = new BodyWS();
        assertEquals(200, body.status);

        String bodyText = body.toString();
        assertEquals("null," + body.nonce + ",200,null", bodyText);

        BodyWS recoveredBody = BodyWS.fromString(bodyText);
        assertEquals(200, recoveredBody.status);
    }
}