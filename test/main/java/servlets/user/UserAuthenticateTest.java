package main.java.servlets.user;

import com.google.gson.Gson;
import main.java.dao.sql.AbstractDBTest;
import main.java.gson.BetterGson;
import main.java.mocks.HttpServletStubber;
import main.java.models.meta.Error;
import main.java.models.User;
import main.java.utils.DBFeeder;
import main.java.utils.DummyGenerator;
import org.junit.Test;

import static org.junit.Assert.*;

public class UserAuthenticateTest extends AbstractDBTest {
    @Test
    public void should_authenticate_existing_user() throws Exception {
        User dummyUser = DBFeeder.createDummyUser();

        User attemptUser = new User();
        attemptUser.email = dummyUser.email;
        attemptUser.password = DummyGenerator.TEST_USER_PASSWORD;
        String attemptUserJson = new BetterGson().newInstance().toJson(attemptUser);

        HttpServletStubber stubber = new HttpServletStubber();
        stubber.body(attemptUserJson).listen();
        new UserAuthenticate().doPost(stubber.servletRequest, stubber.servletResponse);
        User outputUser = new BetterGson().newInstance().fromJson(stubber.gathered(), User.class);

        assertNotNull(outputUser.id);
        assertEquals(dummyUser.name, outputUser.name);
        assertEquals(dummyUser.email, outputUser.email);
        assertEquals(dummyUser.lastName, outputUser.lastName);
        assertEquals(null, outputUser.password);
        assertNotNull(outputUser.createdAt);
        assertNotNull(outputUser.updatedAt);
    }

    @Test
    public void should_not_authenticate_invalid_credentials() throws Exception {
        User dummyUser = DBFeeder.createDummyUser();

        User attemptUser = new User();
        attemptUser.email = dummyUser.email;
        attemptUser.password = "INVALID PASSWORD ATTEMPT";
        String attemptUserJson = new BetterGson().newInstance().toJson(attemptUser);

        HttpServletStubber stubber = new HttpServletStubber();
        stubber.body(attemptUserJson).listen();
        new UserAuthenticate().doPost(stubber.servletRequest, stubber.servletResponse);
        Error error = new BetterGson().newInstance().fromJson(stubber.gathered(), Error.class);

        assertEquals(400, stubber.servletResponse.getStatus());
        assertEquals(UserAuthenticate.PASSWORD_INVALID, error.error);
    }

    @Test
    public void should_not_authenticate_inexisting_user() throws Exception {
        User dummyUser = DummyGenerator.getDummyUser();
        String dummyUserJson = new BetterGson().newInstance().toJson(dummyUser);

        HttpServletStubber stubber = new HttpServletStubber();
        stubber.body(dummyUserJson).listen();
        new UserAuthenticate().doPost(stubber.servletRequest, stubber.servletResponse);
        Error error = new BetterGson().newInstance().fromJson(stubber.gathered(), Error.class);

        assertEquals(UserAuthenticate.EMAIL_NOT_FOUND, error.error);
    }
}