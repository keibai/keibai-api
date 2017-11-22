package main.java.mocks;

import com.google.gson.Gson;
import main.java.models.DummyGenerator;
import main.java.models.User;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HttpServletStubber {
    public HttpServletRequest servletRequest;
    public HttpServletResponse servletResponse;

    private StringWriter stringWriter;
    private PrintWriter printWriter;

    public HttpServletStubber() {
        servletRequest = mock(HttpServletRequest.class);
        servletResponse = mock(HttpServletResponse.class);
    }

    /**
     * Initialize the listener, that will store everything the servlets emits.
     * @return
     * @throws IOException
     */
    public HttpServletStubber listen() throws IOException {
        if (printWriter != null) {
            return this;
        }
        stringWriter = new StringWriter();
        printWriter = new PrintWriter(stringWriter);
        when(servletResponse.getWriter()).thenReturn(printWriter);

        return this;
    }

    /**
     * Body data used when POSTing.
     * You'll most likely be sending an object, so convert it first to string.
     * @param body String-based data that you're sending the servlet.
     * @return
     * @throws IOException
     */
    public HttpServletStubber body(String body) throws IOException {
        Reader reader = new StringReader(body);
        BufferedReader bufferedReader = new BufferedReader(reader);
        when(servletRequest.getReader()).thenReturn(bufferedReader);

        return this;
    }

    /**
     * Gathered response data.
     * @return String-based response data, coming from the HttpServlet printer.
     * You might want to convert it later to an object.
     */
    public String gathered() {
        printWriter.flush();
        return stringWriter.toString();
    }

}
