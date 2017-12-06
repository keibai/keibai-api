package main.java.servlets.auction;

import com.google.gson.Gson;
import main.java.dao.sql.AbstractDBTest;
import main.java.mocks.HttpServletStubber;
import main.java.models.Auction;
import main.java.models.meta.Error;
import main.java.utils.DBFeeder;
import main.java.utils.ImpreciseDate;
import org.junit.Test;

import static org.junit.Assert.*;

public class AuctionSearchTest extends AbstractDBTest {
    @Test
    public void should_return_auction_does_not_exist_if_it_does_not_exist() throws Exception {
        HttpServletStubber stubber = new HttpServletStubber();
        stubber.parameter("id", "1").listen();
        new AuctionSearch().doGet(stubber.servletRequest, stubber.servletResponse);
        Error error = new Gson().fromJson(stubber.gathered(), Error.class);

        assertEquals(AuctionSearch.AUCTION_NOT_FOUND, error.error);
    }

    @Test
    public void should_error_if_parameter_is_invalid() throws Exception {
        HttpServletStubber stubber = new HttpServletStubber();
        stubber.parameter("id", "OMG").listen();
        new AuctionSearch().doGet(stubber.servletRequest, stubber.servletResponse);
        Error error = new Gson().fromJson(stubber.gathered(), Error.class);

        assertEquals(AuctionSearch.ID_INVALID, error.error);
    }

    @Test
    public void should_error_if_no_parameter_is_sent() throws Exception {
        HttpServletStubber stubber = new HttpServletStubber();
        stubber.listen();
        new AuctionSearch().doGet(stubber.servletRequest, stubber.servletResponse);
        Error error = new Gson().fromJson(stubber.gathered(), Error.class);

        assertEquals(AuctionSearch.ID_NONE, error.error);
    }

    @Test
    public void should_return_auction_if_it_exists() throws Exception {
        Auction dummyAuction = DBFeeder.createDummyAuction();

        HttpServletStubber stubber = new HttpServletStubber();
        stubber.parameter("id", String.valueOf(dummyAuction.id)).listen();
        new AuctionSearch().doGet(stubber.servletRequest, stubber.servletResponse);
        Auction outputAuction = new Gson().fromJson(stubber.gathered(), Auction.class);

        assertEquals(dummyAuction.id, outputAuction.id);
        assertEquals(dummyAuction.name, outputAuction.name);
        assertEquals(dummyAuction.startingPrice, outputAuction.startingPrice, 0.000000001);
        assertEquals(new ImpreciseDate(dummyAuction.startTime), new ImpreciseDate(outputAuction.startTime));
        assertEquals(dummyAuction.valid, outputAuction.valid);
        assertEquals(dummyAuction.eventId, outputAuction.eventId);
        assertEquals(dummyAuction.ownerId, outputAuction.ownerId);
        assertEquals(dummyAuction.status, outputAuction.status);
        assertEquals(dummyAuction.winnerId, outputAuction.winnerId);
    }
}