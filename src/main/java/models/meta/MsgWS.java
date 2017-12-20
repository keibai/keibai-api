package main.java.models.meta;

public class MsgWS {
    public String type;
    public String nonce;
    public Object object;

    public boolean isEmpty() {
        return type == null || type.equals("")
            && nonce == null || nonce.equals("")
            && object == null;
    }

    @Override
    public String toString() {
        return String.format("%s,%s,%s", type, nonce, object == null ? "" : object.toString());
    }
}