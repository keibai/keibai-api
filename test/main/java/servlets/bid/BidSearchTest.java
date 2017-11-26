package main.java.servlets.bid;

import com.google.gson.Gson;
import main.java.dao.sql.AbstractDBTest;
import main.java.mocks.HttpServletStubber;
import main.java.models.Bid;
import main.java.models.meta.Error;
import main.java.utils.DBFeeder;
import main.java.utils.ImpreciseDate;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BidSearchTest extends AbstractDBTest {

    @Test
    public void should_return_bid_does_not_exist_if_not_exists() throws Exception {
        HttpServletStubber stubber = new HttpServletStubber();
        stubber.parameter("id", "1").listen();
        new BidSearch().doGet(stubber.servletRequest, stubber.servletResponse);

        Error error = new Gson().fromJson(stubber.gathered(), Error.class);
        assertEquals(BidSearch.BID_NOT_FOUND_ERROR, error.error);
    }

    @Test
    public void should_error_if_parameter_is_invalid() throws Exception {
        HttpServletStubber stubber = new HttpServletStubber();
        stubber.parameter("id", "OMG").listen();
        new BidSearch().doGet(stubber.servletRequest, stubber.servletResponse);
        Error error = new Gson().fromJson(stubber.gathered(), Error.class);

        assertEquals(BidSearch.ID_ERROR, error.error);
    }

    @Test
    public void should_error_if_no_parameter_is_sent() throws Exception {
        HttpServletStubber stubber = new HttpServletStubber();
        stubber.listen();
        new BidSearch().doGet(stubber.servletRequest, stubber.servletResponse);
        Error error = new Gson().fromJson(stubber.gathered(), Error.class);

        assertEquals(BidSearch.ID_NONE_ERROR, error.error);
    }

    @Test
    public void should_return_bid_if_it_exists() throws Exception {
        Bid dummyBid = DBFeeder.createDummyBid();

        HttpServletStubber stubber = new HttpServletStubber();
        stubber.parameter("id", String.valueOf(dummyBid.id)).listen();
        new BidSearch().doGet(stubber.servletRequest, stubber.servletResponse);
        Bid outputBid = new Gson().fromJson(stubber.gathered(), Bid.class);

        assertEquals(dummyBid.id, outputBid.id);
        assertEquals(dummyBid.amount, outputBid.amount, 0.01);
        assertEquals(new ImpreciseDate(dummyBid.createdAt), new ImpreciseDate(outputBid.createdAt));
        assertEquals(dummyBid.auctionId, outputBid.auctionId);
        assertEquals(dummyBid.ownerId, outputBid.ownerId);
    }
}