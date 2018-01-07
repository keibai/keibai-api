package main.java.utils;

import com.google.gson.Gson;
import main.java.models.meta.Error;
import main.java.models.meta.Msg;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class JsonCommon {

    public static final String OK = "OK";
    public static final String INTERNAL_SERVER_ERROR = "Internal server error.";
    public static final String UNAUTHORIZED = "Unauthorized.";
    public static final String INVALID_REQUEST = "Invalid request.";

    /* Predefined responses */

    public static String empty() {
        return "{}";
    }

    public static String ok() {
        return msg(OK);
    }

    public static String internalServerError() {
        return error(INTERNAL_SERVER_ERROR);
    }

    public static String unauthorized() {
        return error(UNAUTHORIZED);
    }

    public static String invalidRequest() {
        return error(INVALID_REQUEST);
    }

    /* Generic */

    public static String msg(String msg) {
        Msg obj = new Msg();
        obj.msg = msg;
        return new Gson().toJson(obj);
    }

    public static String error(String errorMsg) {
        Error obj = new Error();
        obj.error = errorMsg;
        return new Gson().toJson(obj);
    }
}
