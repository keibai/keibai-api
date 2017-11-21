package main.java.utils;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.apache.commons.io.IOUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public class HttpRequest {
    private final HttpServletRequest request;

    public HttpRequest(HttpServletRequest request) {
        this.request = request;
    }

    public String extractPostRequestBody() throws IOException {
        return IOUtils.toString(request.getReader());
    }

    public <T> T extractPostRequestBody(Class<T> className) throws IOException, JsonSyntaxException {
        String json = extractPostRequestBody();
        return new Gson().fromJson(json, className);
    }
}
