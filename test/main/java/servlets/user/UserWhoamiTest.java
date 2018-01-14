package main.java.servlets.user;

import com.google.gson.Gson;
import main.java.dao.sql.AbstractDBTest;
import main.java.gson.BetterGson;
import main.java.mocks.HttpServletStubber;
import main.java.models.User;
import main.java.utils.DBFeeder;
import main.java.utils.ImpreciseDate;
import org.junit.Test;

import static org.junit.Assert.*;

public class UserWhoamiTest extends AbstractDBTest {
    @Test
    public void user_object_is_return_if_user_is_signed_in() throws Exception  {
        User user = DBFeeder.createDummyUser();

        HttpServletStubber stubber = new HttpServletStubber();
        stubber.authenticate(user.id).listen();
        new UserWhoami().doGet(stubber.servletRequest, stubber.servletResponse);
        User outputUser = new BetterGson().newInstance().fromJson(stubber.gathered(), User.class);

        assertEquals(user.name, outputUser.name);
        assertEquals(user.email, outputUser.email);
        assertEquals(user.lastName, outputUser.lastName);
        assertEquals(null, outputUser.password);
        assertEquals(new ImpreciseDate(user.updatedAt), new ImpreciseDate(outputUser.updatedAt));
        assertEquals(new ImpreciseDate(user.createdAt),new ImpreciseDate(outputUser.updatedAt));
    }

    @Test
    public void empty_object_is_returned_if_user_is_not_signed_in() throws Exception {
        HttpServletStubber stubber = new HttpServletStubber();
        stubber.listen();

        new UserWhoami().doGet(stubber.servletRequest, stubber.servletResponse);
        String output = stubber.gathered();

        assertEquals("{}", output);
    }

}