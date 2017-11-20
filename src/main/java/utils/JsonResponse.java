package main.java.utils;

import com.google.gson.Gson;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class JsonResponse {
    private final HttpServletResponse response;

    public JsonResponse(HttpServletResponse response) {
        this.response = response;
    }

    /* Predefined responses */

    public void ok() throws IOException {
        this.msg("OK");
    }

    public void internalServerError() throws IOException {
        this.error("Internal server error.");
    }

    public void unauthorized() throws IOException {
        this.error("Unauthorized");
    }

    /* Generic */

    public void msg(String msg) throws IOException {
        String json = String.format("{ \"msg\": \"%s\" }", msg);
        this.response(json);
    }

    public void error(String errorMsg) throws IOException {
        String json = String.format("{ \"error\": \"%s\" }", errorMsg);
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
