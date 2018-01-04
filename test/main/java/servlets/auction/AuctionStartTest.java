package main.java.servlets.auction;

import com.google.gson.Gson;
import main.java.dao.AuctionDAO;
import main.java.dao.EventDAO;
import main.java.dao.sql.AbstractDBTest;
import main.java.dao.sql.AuctionDAOSQL;
import main.java.dao.sql.EventDAOSQL;
import main.java.mocks.HttpServletStubber;
import main.java.models.Auction;
import main.java.models.Event;
import main.java.models.User;
import main.java.models.meta.Error;
import main.java.models.meta.Msg;
import main.java.utils.DBFeeder;
import main.java.utils.DummyGenerator;
import main.java.utils.ImpreciseDate;
import main.java.utils.JsonResponse;
import org.junit.Test;

import java.sql.Timestamp;

import static org.junit.Assert.*;

public class AuctionStartTest extends AbstractDBTest {

    @Test
    public void test_auction_not_accepted_can_not_be_started() throws Exception {
        EventDAO eventDAO = EventDAOSQL.getInstance();
        AuctionDAO auctionDAO = AuctionDAOSQL.getInstance();

        User dummyUser = DBFeeder.createDummyUser();

        Event event = DummyGenerator.getDummyEvent();
        event.status = Event.OPENED;
        event.ownerId = dummyUser.id;
        Event dbEvent = eventDAO.create(event);

        Auction auction = DummyGenerator.getDummyAuction();
        auction.status = Auction.PENDING;
        auction.ownerId = dummyUser.id;
        auction.eventId = dbEvent.id;
        Auction dbAuction = auctionDAO.create(auction);

        HttpServletStubber stubber = new HttpServletStubber();
        stubber.authenticate(dummyUser.id);
        stubber.parameter("id", String.valueOf(dbAuction.id)).listen();
        new AuctionStart().doPost(stubber.servletRequest, stubber.servletResponse);
        Error error = new Gson().fromJson(stubber.gathered(), Error.class);

        assertEquals(AuctionStart.WRONG_AUCTION_STATUS, error.error);
    }

    @Test
    public void test_auction_can_not_be_started_if_user_is_not_the_owner() throws Exception {
        EventDAO eventDAO = EventDAOSQL.getInstance();
        AuctionDAO auctionDAO = AuctionDAOSQL.getInstance();

        User dummyUser = DBFeeder.createDummyUser();
        User otherDummyUser = DBFeeder.createOtherDummyUser();

        Event event = DummyGenerator.getDummyEvent();
        event.status = Event.OPENED;
        event.ownerId = otherDummyUser.id;
        Event dbEvent = eventDAO.create(event);

        Auction auction = DummyGenerator.getDummyAuction();
        auction.status = Auction.ACCEPTED;
        auction.ownerId = dummyUser.id;
        auction.eventId = dbEvent.id;
        Auction dbAuction = auctionDAO.create(auction);

        HttpServletStubber stubber = new HttpServletStubber();
        stubber.authenticate(dummyUser.id);
        stubber.parameter("id", String.valueOf(dbAuction.id)).listen();
        new AuctionStart().doPost(stubber.servletRequest, stubber.servletResponse);
        Error error = new Gson().fromJson(stubber.gathered(), Error.class);

        assertEquals(JsonResponse.UNAUTHORIZED, error.error);
    }

    @Test
    public void test_auction_with_finished_event_can_not_be_started() throws Exception {
        EventDAO eventDAO = EventDAOSQL.getInstance();
        AuctionDAO auctionDAO = AuctionDAOSQL.getInstance();

        User dummyUser = DBFeeder.createDummyUser();

        Event event = DummyGenerator.getDummyEvent();
        event.status = Event.FINISHED;
        event.ownerId = dummyUser.id;
        Event dbEvent = eventDAO.create(event);

        Auction auction = DummyGenerator.getDummyAuction();
        auction.status = Auction.ACCEPTED;
        auction.ownerId = dummyUser.id;
        auction.eventId = dbEvent.id;
        Auction dbAuction = auctionDAO.create(auction);

        HttpServletStubber stubber = new HttpServletStubber();
        stubber.authenticate(dummyUser.id);
        stubber.parameter("id", String.valueOf(dbAuction.id)).listen();
        new AuctionStart().doPost(stubber.servletRequest, stubber.servletResponse);
        Error error = new Gson().fromJson(stubber.gathered(), Error.class);

        assertEquals(AuctionStart.EVENT_FINISHED, error.error);
    }

    @Test
    public void test_auction_successfully_started() throws Exception {
        EventDAO eventDAO = EventDAOSQL.getInstance();
        AuctionDAO auctionDAO = AuctionDAOSQL.getInstance();

        User dummyUser = DBFeeder.createDummyUser();

        Event event = DummyGenerator.getDummyEvent();
        event.status = Event.OPENED;
        event.ownerId = dummyUser.id;
        Event dbEvent = eventDAO.create(event);

        Auction auction = DummyGenerator.getDummyAuction();
        auction.status = Auction.ACCEPTED;
        auction.ownerId = dummyUser.id;
        auction.eventId = dbEvent.id;
        Auction dbAuction = auctionDAO.create(auction);

        HttpServletStubber stubber = new HttpServletStubber();
        stubber.authenticate(dummyUser.id);
        stubber.parameter("id", String.valueOf(dbAuction.id)).listen();
        Timestamp now = new Timestamp(System.currentTimeMillis());
        new AuctionStart().doPost(stubber.servletRequest, stubber.servletResponse);
        Msg msg = new Gson().fromJson(stubber.gathered(), Msg.class);

        Event updatedEvent = eventDAO.getById(dbEvent.id);
        Auction updatedAuction = auctionDAO.getById(dbAuction.id);

        assertEquals("OK", msg.msg);
        assertEquals(Event.IN_PROGRESS, updatedEvent.status);
        assertEquals(Auction.IN_PROGRESS, updatedAuction.status);
        assertNotNull(updatedAuction.startTime);
        assertEquals(new ImpreciseDate(now), new ImpreciseDate(updatedAuction.startTime));
    }
}