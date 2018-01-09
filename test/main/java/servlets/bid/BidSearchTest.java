package main.java.servlets.bid;

import com.google.gson.Gson;
import main.java.dao.AuctionDAO;
import main.java.dao.BidDAO;
import main.java.dao.DAOException;
import main.java.dao.EventDAO;
import main.java.dao.sql.AbstractDBTest;
import main.java.dao.sql.AuctionDAOSQL;
import main.java.dao.sql.BidDAOSQL;
import main.java.dao.sql.EventDAOSQL;
import main.java.mocks.HttpServletStubber;
import main.java.models.Auction;
import main.java.models.Bid;
import main.java.models.Event;
import main.java.models.User;
import main.java.models.meta.Error;
import main.java.utils.DBFeeder;
import main.java.utils.ImpreciseDate;
import org.junit.Test;

import javax.servlet.ServletException;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class BidSearchTest extends AbstractDBTest {
    EventDAO eventDAO = EventDAOSQL.getInstance();
    AuctionDAO auctionDAO = AuctionDAOSQL.getInstance();
    BidDAO bidDAO = BidDAOSQL.getInstance();

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
    public void should_always_return_bid_amount_on_english_auction() throws ServletException, DAOException, IOException {
        Bid dummyBid = DBFeeder.createDummyBid();

        Auction dbAuction = auctionDAO.getById(dummyBid.id);
        Event dbEvent = eventDAO.getById(dbAuction.id);
        dbEvent.auctionType = Event.ENGLISH;
        eventDAO.update(dbEvent);

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

    @Test
    public void should_not_return_bid_amount_if_combinatorial_and_not_owner() throws ServletException, DAOException, IOException {
        User altUser = DBFeeder.createThirdDummyUser();
        Bid dummyBid = DBFeeder.createDummyBid();
        dummyBid.ownerId = altUser.id;
        bidDAO.update(dummyBid);

        Auction dbAuction = auctionDAO.getById(dummyBid.id);
        Event dbEvent = eventDAO.getById(dbAuction.id);
        dbEvent.auctionType = Event.COMBINATORIAL;
        eventDAO.update(dbEvent);

        HttpServletStubber stubber = new HttpServletStubber();
        stubber.parameter("id", String.valueOf(dummyBid.id)).listen();
        new BidSearch().doGet(stubber.servletRequest, stubber.servletResponse);
        Bid outputBid = new Gson().fromJson(stubber.gathered(), Bid.class);

        assertEquals(dummyBid.id, outputBid.id);
        assertEquals(0.0, outputBid.amount, 0);
    }

    @Test
    public void should_return_bid_amount_if_combinatorial_and_owner() throws ServletException, DAOException, IOException {
        Bid dummyBid = DBFeeder.createDummyBid();

        Auction dbAuction = auctionDAO.getById(dummyBid.id);
        Event dbEvent = eventDAO.getById(dbAuction.id);
        dbEvent.auctionType = Event.COMBINATORIAL;
        eventDAO.update(dbEvent);

        HttpServletStubber stubber = new HttpServletStubber();
        stubber.authenticate(dummyBid.ownerId);
        stubber.parameter("id", String.valueOf(dummyBid.id)).listen();
        new BidSearch().doGet(stubber.servletRequest, stubber.servletResponse);
        Bid outputBid = new Gson().fromJson(stubber.gathered(), Bid.class);

        assertEquals(dummyBid.id, outputBid.id);
        assertEquals(dummyBid.amount, outputBid.amount, 0.01);
    }
}