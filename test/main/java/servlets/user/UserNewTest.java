package main.java.servlets.user;

import com.google.gson.Gson;
import main.java.dao.sql.AbstractDBTest;
import main.java.mocks.HttpServletStubber;
import main.java.utils.DummyGenerator;
import main.java.models.User;
import org.junit.Test;

import static org.junit.Assert.*;

public class UserNewTest extends AbstractDBTest {
    @Test
    public void should_create_new_user() throws Exception {
        User dummyUser = DummyGenerator.getDummyUser();
        String dummyUserJson = new Gson().toJson(dummyUser);

        HttpServletStubber stubber = new HttpServletStubber();
        stubber.body(dummyUserJson).listen();
        new UserNew().doPost(stubber.servletRequest, stubber.servletResponse);
        User outputUser = new Gson().fromJson(stubber.gathered(), User.class);

        assertNotNull(outputUser.id);
        assertEquals(dummyUser.name, outputUser.name);
        assertEquals(dummyUser.email, outputUser.email);
        assertEquals(dummyUser.lastName, outputUser.lastName);
        assertEquals(null, outputUser.password);
        assertNotNull(outputUser.createdAt);
        assertNotNull(outputUser.updatedAt);
    }

    @Test
    public void should_authenticate_after_creating_user() throws Exception {
        User dummyUser = DummyGenerator.getDummyUser();
        String dummyUserJson = new Gson().toJson(dummyUser);

        HttpServletStubber stubber = new HttpServletStubber();
        stubber.body(dummyUserJson).listen();
        new UserNew().doPost(stubber.servletRequest, stubber.servletResponse);
        User outputUser = new Gson().fromJson(stubber.gathered(), User.class);

        assertEquals(outputUser.id, stubber.authenticated());
    }
}