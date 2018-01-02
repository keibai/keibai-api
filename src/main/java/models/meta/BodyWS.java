package main.java.models.meta;

public class BodyWS {
    public String type;
    public String nonce;
    public String json;

    public boolean isEmpty() {
        return type == null || type.equals("")
            && nonce == null || nonce.equals("")
            && json == null || json.equals("");
    }

    @Override
    public String toString() {
        return String.format("%s,%s,%s", type, nonce, json);
    }
}