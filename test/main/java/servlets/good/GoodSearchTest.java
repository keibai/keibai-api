package main.java.servlets.good;

import com.google.gson.Gson;
import main.java.dao.sql.AbstractDBTest;
import main.java.gson.BetterGson;
import main.java.mocks.HttpServletStubber;
import main.java.models.Good;
import main.java.models.meta.Error;
import main.java.utils.DBFeeder;
import org.junit.Test;

import static org.junit.Assert.*;

public class GoodSearchTest extends AbstractDBTest {

    @Test
    public void should_return_good_does_not_exist_if_not_exists() throws Exception {
        HttpServletStubber stubber = new HttpServletStubber();
        stubber.parameter("id", "1").listen();
        new GoodSearch().doGet(stubber.servletRequest, stubber.servletResponse);

        Error error = new BetterGson().newInstance().fromJson(stubber.gathered(), Error.class);
        assertEquals(GoodSearch.GOOD_NOT_FOUND_ERROR, error.error);
    }

    @Test
    public void should_error_if_parameter_is_invalid() throws Exception {
        HttpServletStubber stubber = new HttpServletStubber();
        stubber.parameter("id", "OMG").listen();
        new GoodSearch().doGet(stubber.servletRequest, stubber.servletResponse);
        Error error = new BetterGson().newInstance().fromJson(stubber.gathered(), Error.class);

        assertEquals(GoodSearch.ID_ERROR, error.error);
    }

    @Test
    public void should_error_if_no_parameter_is_sent() throws Exception {
        HttpServletStubber stubber = new HttpServletStubber();
        stubber.listen();
        new GoodSearch().doGet(stubber.servletRequest, stubber.servletResponse);
        Error error = new BetterGson().newInstance().fromJson(stubber.gathered(), Error.class);

        assertEquals(GoodSearch.ID_NONE_ERROR, error.error);
    }

    @Test
    public void should_return_good_if_it_exists() throws Exception {
        Good dummyGood = DBFeeder.createDummyGood();

        HttpServletStubber stubber = new HttpServletStubber();
        stubber.parameter("id", String.valueOf(dummyGood.id)).listen();
        new GoodSearch().doGet(stubber.servletRequest, stubber.servletResponse);
        Good outputGood = new BetterGson().newInstance().fromJson(stubber.gathered(), Good.class);

        assertEquals(dummyGood.id, outputGood.id);
        assertEquals(dummyGood.name, outputGood.name);
        assertEquals(dummyGood.auctionId, outputGood.auctionId);
    }
}