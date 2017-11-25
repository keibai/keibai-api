package main.java.mocks;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;
import java.io.*;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HttpServletStubber {
    public HttpServletRequest servletRequest;
    public HttpServletResponse servletResponse;

    private StringWriter stringWriter;
    private PrintWriter printWriter;

    private MockHttpSession mockHttpSession;

    private final Map<String, String> parameters;

    public HttpServletStubber() {
        parameters = new HashMap<>();

        servletRequest = mock(HttpServletRequest.class);
        servletResponse = mock(HttpServletResponse.class);

        mockHttpSession = new MockHttpSession();
    }

    /**
     * Initialize the listener, that will store everything the servlets emits.
     * Session is also mocked.
     * Note: if already initialized, it will do nothing.
     * @return
     * @throws IOException
     */
    public HttpServletStubber listen() throws IOException {
        if (printWriter != null) {
            return this;
        }
        // Mock servlet writer.
        stringWriter = new StringWriter();
        printWriter = new PrintWriter(stringWriter);
        when(servletResponse.getWriter()).thenReturn(printWriter);

        // Mock session.
        when(servletRequest.getSession()).thenReturn(mockHttpSession);

        // Mock parameters.
        for (String paramName : parameters.keySet()) {
            when(servletRequest.getParameter(paramName)).thenReturn(parameters.get(paramName));
        }

        return this;
    }

    /**
     * Authenticate as a user.
     * This function makes use of the mocked session storage to fake the data that it is stored in it. Hence, passing a
     * userId will store it as if the user had been authenticated already.
     * Beware! The user with the userId you're passing into the function must already exist for the test to work
     * properly.
     * @param userId Preferably, a existing user identifier that you want to be signed in as.
     * @return
     */
    public HttpServletStubber authenticate(int userId) {
        mockHttpSession.setAttribute(main.java.utils.HttpSession.USER_ID_KEY, userId);

        return this;
    }

    /**
     * Returns the userId of who we are authenticated as.
     * @return
     */
    public int authenticated() {
        Object object = this.mockHttpSession.getAttribute(main.java.utils.HttpSession.USER_ID_KEY);
        return object == null ? -1 : (int) object;
    }

    public HttpServletStubber parameter(String name, String value) {
        parameters.put(name, value);

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

    private class MockHttpSession implements HttpSession {
        Map<String, Object> storage = new HashMap<>();

        @Override
        public long getCreationTime() {
            return 0;
        }

        @Override
        public String getId() {
            return null;
        }

        @Override
        public long getLastAccessedTime() {
            return 0;
        }

        @Override
        public ServletContext getServletContext() {
            return null;
        }

        @Override
        public void setMaxInactiveInterval(int i) {

        }

        @Override
        public int getMaxInactiveInterval() {
            return 0;
        }

        @Override
        public HttpSessionContext getSessionContext() {
            return null;
        }

        @Override
        public Object getAttribute(String s) {
            return storage.get(s);
        }

        @Override
        public Object getValue(String s) {
            return null;
        }

        @Override
        public Enumeration<String> getAttributeNames() {
            return null;
        }

        @Override
        public String[] getValueNames() {
            return new String[0];
        }

        @Override
        public void setAttribute(String s, Object o) {
            storage.put(s, o);
        }

        @Override
        public void putValue(String s, Object o) {

        }

        @Override
        public void removeAttribute(String s) {

        }

        @Override
        public void removeValue(String s) {

        }

        @Override
        public void invalidate() {

        }

        @Override
        public boolean isNew() {
            return false;
        }
    }

}
