package main.java.socket;

import main.java.models.meta.BodyWS;
import main.java.utils.JsonCommon;

public class BodyWSCommon {

    public static BodyWS ok() {
        BodyWS body = new BodyWS();
        body.json = JsonCommon.ok();
        return body;
    }

    public static BodyWS ok(String json) {
        BodyWS body = new BodyWS();
        body.json = json;
        return body;
    }

    public static BodyWS internalServerError() {
        BodyWS body = new BodyWS();
        body.status = 500;
        body.json = JsonCommon.internalServerError();
        return body;
    }

    public static BodyWS unauthorized() {
        BodyWS body = new BodyWS();
        body.status = 400;
        body.json = JsonCommon.unauthorized();
        return body;
    }

    public static BodyWS invalidRequest() {
        BodyWS body = new BodyWS();
        body.status = 400;
        body.json = JsonCommon.invalidRequest();
        return body;
    }

    public static BodyWS error(String json) {
        return error(json, 400);
    }

    public static BodyWS error(String json, int statusCode) {
        BodyWS body = new BodyWS();
        body.status = statusCode;
        body.json = json;
        return body;
    }
}
