package main.java.servlets.socket;

import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import main.java.models.meta.BodyWS;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BodyDecoder implements Decoder.Text<BodyWS> {

    // private static Gson gson = new Gson();

    /**
     * Decode the information sent through socket.
     * If it's not complete, an empty BodyWS will be returned.
     * A BodyWS json can be treated as follows:
     * private Gson gson = new Gson();
     * FooModel foo = gson.fromJson(body.json, FooModel.class)
     * @param s A <type>,<nonce>,<json> string.
     * @return
     * @throws DecodeException
     */
    @Override
    public BodyWS decode(String text) throws DecodeException {
        return BodyWS.fromString(text);
        /*try {
            BodyWS msg = gson.fromJson(s, BodyWS.class);
            System.out.println("aa");
            System.out.println(msg);
            return msg;
        } catch (JsonSyntaxException e) {
            System.out.println(e.toString());
            BodyWS emptyMessage = new BodyWS();
            return emptyMessage;
        }
        */
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