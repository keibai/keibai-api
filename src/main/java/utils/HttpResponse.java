package main.java.utils;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class HttpResponse {

    private final HttpServletResponse response;

    public HttpResponse(HttpServletResponse response) {
        this.response = response;
    }

    /* Predefined responses */

    public void empty() throws IOException {
        this.response("{}");
    }

    public void ok() throws IOException {
        this.response(JsonCommon.ok());
    }

    public void internalServerError() throws IOException {
        this.errorResponse(JsonCommon.internalServerError(), 500);
    }

    public void unauthorized() throws IOException {
        this.errorResponse(JsonCommon.unauthorized());
    }

    public void invalidRequest() throws IOException {
        this.errorResponse(JsonCommon.invalidRequest());
    }

    /* Generic */

    public void msg(String msg) throws IOException {
        this.response(JsonCommon.msg(msg));
    }

    public void error(String errorMsg) throws IOException {
        error(errorMsg, 400);
    }

    public void error(String errorMsg, int statusCode) throws IOException {
        this.errorResponse(JsonCommon.error(errorMsg), statusCode);
    }

    public void errorResponse(String json) throws IOException {
        this.errorResponse(json, 400);
    }

    public void errorResponse(String json, int statusCode) throws IOException {
        this.response.setStatus(statusCode);
        this.response(json);
    }

    public void response(String json) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        out.print(json);
        out.flush();
    }
}
