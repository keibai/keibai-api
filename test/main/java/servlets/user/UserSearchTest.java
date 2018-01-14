package main.java.servlets.user;

import com.google.gson.Gson;
import main.java.dao.UserDAO;
import main.java.dao.sql.AbstractDBTest;
import main.java.dao.sql.UserDAOSQL;
import main.java.gson.BetterGson;
import main.java.mocks.HttpServletStubber;
import main.java.models.User;
import main.java.models.meta.Error;
import main.java.utils.DBFeeder;
import main.java.utils.DummyGenerator;
import main.java.utils.ImpreciseDate;
import org.junit.Test;

import static org.junit.Assert.*;

public class UserSearchTest extends AbstractDBTest {
    @Test
    public void should_return_user_does_not_exist_if_it_does_not_exist() throws Exception {
        HttpServletStubber stubber = new HttpServletStubber();
        stubber.parameter("id", "1").listen();
        new UserSearch().doGet(stubber.servletRequest, stubber.servletResponse);
        Error error = new BetterGson().newInstance().fromJson(stubber.gathered(), Error.class);

        assertEquals(UserSearch.USER_NOT_FOUND, error.error);
    }

    @Test
    public void should_error_if_parameter_is_invalid() throws Exception {
        HttpServletStubber stubber = new HttpServletStubber();
        stubber.parameter("id", "OMG").listen();
        new UserSearch().doGet(stubber.servletRequest, stubber.servletResponse);
        Error error = new BetterGson().newInstance().fromJson(stubber.gathered(), Error.class);

        assertEquals(UserSearch.ID_INVALID, error.error);
    }

    @Test
    public void should_error_if_no_parameter_is_sent() throws Exception {
        HttpServletStubber stubber = new HttpServletStubber();
        stubber.listen();
        new UserSearch().doGet(stubber.servletRequest, stubber.servletResponse);
        Error error = new BetterGson().newInstance().fromJson(stubber.gathered(), Error.class);

        assertEquals(UserSearch.ID_NONE, error.error);
    }

    @Test
    public void should_return_user_if_it_exists() throws Exception {
        User dummyUser = DBFeeder.createDummyUser();

        HttpServletStubber stubber = new HttpServletStubber();
        stubber.parameter("id", String.valueOf(dummyUser.id)).listen();
        new UserSearch().doGet(stubber.servletRequest, stubber.servletResponse);
        User outputUser = new BetterGson().newInstance().fromJson(stubber.gathered(), User.class);

        assertEquals(dummyUser.id, outputUser.id);
        assertEquals(dummyUser.name, outputUser.name);
        assertEquals(dummyUser.lastName, outputUser.lastName);
        assertEquals(dummyUser.email, outputUser.email);
        assertNull(outputUser.password);
        assertEquals(new ImpreciseDate(dummyUser.createdAt), new ImpreciseDate(outputUser.createdAt));
        assertEquals(new ImpreciseDate(dummyUser.updatedAt), new ImpreciseDate(outputUser.updatedAt));
    }

    @Test
    public void should_hide_credit() throws Exception {
        User dummyUser = DummyGenerator.getDummyUser();
        dummyUser.credit = 10.0;
        UserDAO userDAO = UserDAOSQL.getInstance();
        User dbUser = userDAO.create(dummyUser);

        HttpServletStubber stubber = new HttpServletStubber();
        stubber.parameter("id", String.valueOf(dbUser.id)).listen();
        new UserSearch().doGet(stubber.servletRequest, stubber.servletResponse);
        User outputUser = new BetterGson().newInstance().fromJson(stubber.gathered(), User.class);

        assertEquals(0.0, outputUser.credit, 0.01);
    }
}