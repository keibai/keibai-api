package main.java.servlets.user;

import com.google.gson.Gson;
import main.java.dao.sql.AbstractDBTest;
import main.java.mocks.HttpServletStubber;
import main.java.models.Error;
import main.java.models.Msg;
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
        String attemptUserJson = new Gson().toJson(attemptUser);

        HttpServletStubber stubber = new HttpServletStubber();
        stubber.body(attemptUserJson).listen();
        new UserAuthenticate().doPost(stubber.servletRequest, stubber.servletResponse);
        Msg outputUser = new Gson().fromJson(stubber.gathered(), Msg.class);

        assertEquals("OK", outputUser.msg);
    }

    @Test
    public void should_not_authenticate_invalid_credentials() throws Exception {
        User dummyUser = DBFeeder.createDummyUser();

        User attemptUser = new User();
        attemptUser.email = dummyUser.email;
        attemptUser.password = "INVALID PASSWORD ATTEMPT";
        String attemptUserJson = new Gson().toJson(attemptUser);

        HttpServletStubber stubber = new HttpServletStubber();
        stubber.body(attemptUserJson).listen();
        new UserAuthenticate().doPost(stubber.servletRequest, stubber.servletResponse);
        Error error = new Gson().fromJson(stubber.gathered(), Error.class);

        assertEquals("Invalid password.", error.error);
    }

    @Test
    public void should_not_authenticate_inexisting_user() throws Exception {
        User dummyUser = DummyGenerator.getDummyUser();
        String dummyUserJson = new Gson().toJson(dummyUser);

        HttpServletStubber stubber = new HttpServletStubber();
        stubber.body(dummyUserJson).listen();
        new UserAuthenticate().doPost(stubber.servletRequest, stubber.servletResponse);
        Error error = new Gson().fromJson(stubber.gathered(), Error.class);

        assertEquals("Email is not registered on the system.", error.error);
    }
}