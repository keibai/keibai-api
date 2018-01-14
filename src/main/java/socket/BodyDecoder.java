package main.java.socket;

import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;

import main.java.models.meta.BodyWS;

public class BodyDecoder implements Decoder.Text<BodyWS> {

    /**
     * Decode the information sent through socket.
     * If it's not complete, an empty BodyWS will be returned.
     * A BodyWS json can be treated as follows:
     * private Gson gson = new BetterGson().newInstance();
     * FooModel foo = gson.fromJson(body.json, FooModel.class)
     * @param s A <type>,<nonce>,<json> string.
     * @return
     * @throws DecodeException
     */
    @Override
    public BodyWS decode(String text) throws DecodeException {
        return BodyWS.fromString(text);
    }

    @Override
    public boolean willDecode(String s) {
        return true;
    }

    @Override
    public void init(EndpointConfig endpointConfig) {
        // Custom initialization logic
    }

    @Override
    public void destroy() {
        // Close resources
    }
}