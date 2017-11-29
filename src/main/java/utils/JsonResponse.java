package main.java.utils;

import com.google.gson.Gson;
import main.java.models.meta.Msg;
import main.java.models.meta.Error;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class JsonResponse {

    public static final String OK = "OK";
    public static final String INTERNAL_SERVER_ERROR = "Internal server error.";
    public static final String UNAUTHORIZED = "Unauthorized.";
    public static final String INVALID_REQUEST = "Invalid request.";

    private final HttpServletResponse response;

    public JsonResponse(HttpServletResponse response) {
        this.response = response;
    }

    /* Predefined responses */

    public void empty() throws IOException {
        this.response("{}");
    }

    public void ok() throws IOException {
        this.msg(OK);
    }

    public void internalServerError() throws IOException {
        this.error(INTERNAL_SERVER_ERROR);
    }

    public void unauthorized() throws IOException {
        this.error(UNAUTHORIZED);
    }

    public void invalidRequest() throws IOException {
        this.error(INVALID_REQUEST);
    }

    /* Generic */

    public void msg(String msg) throws IOException {
        Msg obj = new Msg();
        obj.msg = msg;
        this.response(new Gson().toJson(obj));
    }

    public void error(String errorMsg) throws IOException {
        Error obj = new Error();
        obj.error = errorMsg;
        this.response.setStatus(400);
        this.response(new Gson().toJson(obj));
    }

    public void response(String json) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        out.print(json);
        out.flush();
    }
}
