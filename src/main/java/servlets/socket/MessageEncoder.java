package main.java.servlets.socket;

import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;


import com.google.gson.Gson;
import main.java.models.meta.MsgWS;

public class MessageEncoder implements Encoder.Text<MsgWS> {

    private static Gson gson = new Gson();

    @Override
    public String encode(MsgWS message) throws EncodeException {
        String json = gson.toJson(message);
        return json;
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