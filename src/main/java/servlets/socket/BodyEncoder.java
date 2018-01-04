package main.java.servlets.socket;

import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;


import com.google.gson.Gson;
import main.java.models.meta.BodyWS;

public class BodyEncoder implements Encoder.Text<BodyWS> {

    private static Gson gson = new Gson();

    @Override
    public String encode(BodyWS body) throws EncodeException {
        return body.toString();
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