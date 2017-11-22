package main.java.servlets.user;

import com.google.gson.Gson;
import main.java.dao.sql.AbstractDBTest;
import main.java.models.DummyGenerator;
import main.java.models.User;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.*;

import static org.junit.Assert.*;

public class UserNewTest extends AbstractDBTest {
    @Test
    public void doPost() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        User dummyUser = DummyGenerator.getDummyUser();
        String dummyUserJson = new Gson().toJson(dummyUser);
        Reader reader = new StringReader(dummyUserJson);
        BufferedReader bufferedReader = new BufferedReader(reader);
        when(request.getReader()).thenReturn(bufferedReader);

        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);

        new UserNew().doPost(request, response);

        writer.flush();
        String output = stringWriter.toString();
        User outputUser = new Gson().fromJson(output, User.class);

        assertEquals(dummyUser.name, outputUser.name);
        assertEquals(dummyUser.email, outputUser.email);
        assertEquals(dummyUser.lastName, outputUser.lastName);
        assertEquals(null, outputUser.password);
        assertNotNull(outputUser.createdAt);
        assertNotNull(outputUser.updatedAt);
    }

}