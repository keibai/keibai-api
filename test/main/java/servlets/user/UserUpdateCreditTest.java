package main.java.servlets.user;

import com.google.gson.Gson;
import main.java.dao.sql.AbstractDBTest;
import main.java.mocks.HttpServletStubber;
import main.java.models.User;
import main.java.models.meta.Error;
import main.java.utils.*;
import org.junit.Test;

import static org.junit.Assert.*;

public class UserUpdateCreditTest extends AbstractDBTest {

    @Test
    public void should_not_update_user_if_bad_request() throws Exception {
        User dummyUser = DBFeeder.createDummyUser();

        HttpServletStubber stubber = new HttpServletStubber();
        stubber.authenticate(dummyUser.id);
        stubber.body("").listen();
        new UserUpdateCredit().doPost(stubber.servletRequest, stubber.servletResponse);
        Error error = new Gson().fromJson(stubber.gathered(), Error.class);

        assertEquals(JsonCommon.INVALID_REQUEST, error.error);
    }

    @Test
    public void should_show_error_if_user_does_not_exist() throws Exception {
        User dummyUser = DummyGenerator.getDummyUser();

        User updatedUser = new User();
        updatedUser.id = dummyUser.id;
        updatedUser.credit = 2;
        String updatedUserJson = new Gson().toJson(updatedUser);

        HttpServletStubber stubber = new HttpServletStubber();
        stubber.authenticate(dummyUser.id);
        stubber.body(updatedUserJson).listen();
        new UserUpdateCredit().doPost(stubber.servletRequest, stubber.servletResponse);
        Error error = new Gson().fromJson(stubber.gathered(), Error.class);

        assertEquals(UserUpdateCredit.USER_NOT_EXIST, error.error);
    }

    @Test
    public void should_not_update_user_if_his_credit_becomes_negative() throws Exception {
        User dummyUser = DBFeeder.createDummyUser();
        User updatedUser = new User();
        updatedUser.id = dummyUser.id;
        updatedUser.credit = -0.5;
        String updatedUserJson = new Gson().toJson(updatedUser);

        HttpServletStubber stubber = new HttpServletStubber();
        stubber.authenticate(dummyUser.id);
        stubber.body(updatedUserJson).listen();
        new UserUpdateCredit().doPost(stubber.servletRequest, stubber.servletResponse);
        Error error = new Gson().fromJson(stubber.gathered(), Error.class);

        assertEquals(UserUpdateCredit.INVALID_CREDIT_AMOUNT, error.error);
    }

    @Test
    public void should_update_user_credit() throws Exception {
        User dummyUser = DBFeeder.createDummyUser();
        // Wait 1 second to avoid createdAt == updatedAt
        Thread.sleep(1000);

        User updatedUser = new User();
        updatedUser.id = dummyUser.id;
        updatedUser.credit = 2;
        String updatedUserJson = new Gson().toJson(updatedUser);

        HttpServletStubber stubber = new HttpServletStubber();
        stubber.authenticate(dummyUser.id);
        stubber.body(updatedUserJson).listen();
        new UserUpdateCredit().doPost(stubber.servletRequest, stubber.servletResponse);
        User outputUser = new Gson().fromJson(stubber.gathered(), User.class);

        assertEquals(dummyUser.id, outputUser.id);
        assertEquals(dummyUser.name, outputUser.name);
        assertEquals(dummyUser.lastName, outputUser.lastName);
        assertNull(outputUser.password);
        assertEquals(dummyUser.email, outputUser.email);
        assertEquals(dummyUser.credit + updatedUser.credit, outputUser.credit, 0.0000001);
        assertEquals(new ImpreciseDate(dummyUser.createdAt), new ImpreciseDate(outputUser.createdAt));
        assertNotEquals(new ImpreciseDate(dummyUser.updatedAt), new ImpreciseDate(outputUser.updatedAt));
    }
}