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
        };
        for (String invalidEntry: INVALID) {
            BodyWS body = BodyWS.fromString(invalidEntry);
            assertTrue(body.isEmpty());
        }
    }

    @Test
    public void valid_should_decode_as_BodyWS() {
        String[][] VALID = new String[][]{
                new String[]{",,", "", "", ""},
                new String[]{"123,123,{}", "123", "123", "{}"},
                new String[]{"abc,def,{ id: 1, amount: 1 }", "abc", "def", "{ id: 1, amount: 1 }"},
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
        assertEquals("abc,123,{ id: 1, amount: 1 }", body.toString());
    }
}