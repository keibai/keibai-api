package main.java.servlets.socket;

import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import main.java.models.meta.MsgWS;

public class MessageDecoder implements Decoder.Text<MsgWS> {

    private static Gson gson = new Gson();

    @Override
    public MsgWS decode(String s) throws DecodeException {
        try {
            MsgWS msg = gson.fromJson(s, MsgWS.class);
            System.out.println("aa");
            System.out.println(msg);
            return msg;
        } catch (JsonSyntaxException e) {
            System.out.println(e.toString());
            MsgWS emptyMessage = new MsgWS();
            return emptyMessage;
        }
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