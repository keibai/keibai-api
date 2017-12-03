package main.java.servlets.auction;

import com.google.gson.Gson;
import main.java.dao.sql.AbstractDBTest;
import main.java.dao.sql.AuctionDBTest;
import main.java.mocks.HttpServletStubber;
import main.java.models.Auction;
import main.java.models.Event;
import main.java.models.User;
import main.java.models.meta.Error;
import main.java.utils.DBFeeder;
import org.junit.Test;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.*;

public class AuctionListByEventIdTest extends AbstractDBTest {

    @Test
    public void test_when_event_id_parameter_not_set() throws Exception {
        HttpServletStubber stubber = new HttpServletStubber();
        stubber.listen();
        new AuctionListByEventId().doGet(stubber.servletRequest, stubber.servletResponse);
        Error error = new Gson().fromJson(stubber.gathered(), Error.class);

        assertEquals(AuctionListByEventId.ID_NONE, error.error);
    }

    @Test
    public void test_when_event_id_parameter_is_empty() throws Exception {
        HttpServletStubber stubber = new HttpServletStubber();
        stubber.parameter("eventid", "").listen();
        new AuctionListByEventId().doGet(stubber.servletRequest, stubber.servletResponse);
        Error error = new Gson().fromJson(stubber.gathered(), Error.class);

        assertEquals(AuctionListByEventId.ID_INVALID, error.error);
    }

    @Test
    public void test_when_event_id_parameter_is_NaN() throws Exception {
        HttpServletStubber stubber = new HttpServletStubber();
        stubber.parameter("eventid", "OMGNaN").listen();
        new AuctionListByEventId().doGet(stubber.servletRequest, stubber.servletResponse);
        Error error = new Gson().fromJson(stubber.gathered(), Error.class);

        assertEquals(AuctionListByEventId.ID_INVALID, error.error);
    }

    @Test
    public void test_get_auction_list_returns_list_of_auctions() throws Exception {
        Auction auction = DBFeeder.createDummyAuction();
        Auction otherAuction = DBFeeder.createOtherDummyAuction(auction.eventId, auction.ownerId);

        List<Auction> expectedAuctionList = new LinkedList<Auction>() {{
            add(auction);
            add(otherAuction);
        }};

        HttpServletStubber stubber = new HttpServletStubber();
        stubber.parameter("eventid", String.valueOf(auction.eventId)).listen();
        new AuctionListByEventId().doGet(stubber.servletRequest, stubber.servletResponse);
        Auction[] modelList = new Gson().fromJson(stubber.gathered(), Auction[].class);

        AuctionDBTest.assertAuctionListEquals(expectedAuctionList, Arrays.asList(modelList));
    }
}