package main.java.servlets.auction;

import com.google.gson.Gson;
import main.java.dao.EventDAO;
import main.java.dao.sql.AbstractDBTest;
import main.java.dao.sql.EventDAOSQL;
import main.java.mocks.HttpServletStubber;
import main.java.models.Auction;
import main.java.models.Event;
import main.java.models.User;
import main.java.models.meta.Error;
import main.java.utils.DBFeeder;
import main.java.utils.ImpreciseDate;
import main.java.utils.HttpResponse;
import main.java.utils.JsonCommon;
import org.junit.Test;

import static org.junit.Assert.*;

public class AuctionUpdateStatusTest extends AbstractDBTest {

    @Test
    public void test_update_can_not_be_done_if_user_not_authenticated() throws Exception {
        HttpServletStubber stubber = new HttpServletStubber();
        stubber.body("").listen();
        new AuctionUpdateStatus().doPost(stubber.servletRequest, stubber.servletResponse);
        Error error = new Gson().fromJson(stubber.gathered(), Error.class);

        assertEquals(JsonCommon.UNAUTHORIZED, error.error);
    }

    @Test
    public void test_invalid_request_if_body_is_empty() throws Exception {
        User dummyUser = DBFeeder.createDummyUser();

        HttpServletStubber stubber = new HttpServletStubber();
        stubber.authenticate(dummyUser.id);
        stubber.body("").listen();
        new AuctionUpdateStatus().doPost(stubber.servletRequest, stubber.servletResponse);
        Error error = new Gson().fromJson(stubber.gathered(), Error.class);

        assertEquals(JsonCommon.INVALID_REQUEST, error.error);
    }

    @Test
    public void test_invalid_request_if_auction_id_is_0() throws Exception {
        User dummyUser = DBFeeder.createDummyUser();
        Event dummyEvent = DBFeeder.createDummyEvent();

        Auction wrongAuction = new Auction();
        wrongAuction.id = 0;
        wrongAuction.status = Auction.PENDING;

        String wrongAuctionJson = new Gson().toJson(wrongAuction);

        HttpServletStubber stubber = new HttpServletStubber();
        stubber.authenticate(dummyUser.id);
        stubber.body(wrongAuctionJson).listen();
        new AuctionUpdateStatus().doPost(stubber.servletRequest, stubber.servletResponse);
        Error error = new Gson().fromJson(stubber.gathered(), Error.class);

        assertEquals(JsonCommon.INVALID_REQUEST, error.error);
    }

    @Test
    public void test_error_if_event_status_not_valid() throws Exception {
        EventDAO eventDAO = EventDAOSQL.getInstance();
        Auction dummyAuction = DBFeeder.createDummyAuction();
        Event dummyEvent = eventDAO.getById(dummyAuction.eventId);

        Auction wrongAuction = new Auction();
        wrongAuction.id = dummyEvent.id;
        wrongAuction.status = "WRONG STATUS";
        String wrongAuctionJson = new Gson().toJson(wrongAuction);

        HttpServletStubber stubber = new HttpServletStubber();
        stubber.authenticate(dummyEvent.ownerId);
        stubber.body(wrongAuctionJson).listen();
        new AuctionUpdateStatus().doPost(stubber.servletRequest, stubber.servletResponse);
        Error error = new Gson().fromJson(stubber.gathered(), Error.class);

        assertEquals(AuctionUpdateStatus.INVALID_STATUS, error.error);
    }

    @Test
    public void test_error_if_auction_does_not_exist() throws Exception {
        User dummyUser = DBFeeder.createDummyUser();
        Event dummyEvent = DBFeeder.createDummyEvent();

        Auction wrongAuction = new Auction();
        wrongAuction.id = 2;
        wrongAuction.status = Auction.PENDING;
        String wrongAuctionJson = new Gson().toJson(wrongAuction);

        HttpServletStubber stubber = new HttpServletStubber();
        stubber.authenticate(dummyUser.id);
        stubber.body(wrongAuctionJson).listen();
        new AuctionUpdateStatus().doPost(stubber.servletRequest, stubber.servletResponse);
        Error error = new Gson().fromJson(stubber.gathered(), Error.class);

        assertEquals(AuctionUpdateStatus.AUCTION_NOT_EXIST, error.error);
    }

    @Test
    public void test_unauthorized_when_owner_is_not_the_user() throws Exception {
        User dummyUser = DBFeeder.createDummyUser();
        Auction dummyAuction = DBFeeder.createDummyAuction();

        Auction updateAuction = new Auction();
        updateAuction.id = dummyAuction.id;
        updateAuction.status = Auction.ACCEPTED;
        String updateAuctionJson = new Gson().toJson(updateAuction);

        HttpServletStubber stubber = new HttpServletStubber();
        stubber.authenticate(dummyUser.id);
        stubber.body(updateAuctionJson).listen();
        new AuctionUpdateStatus().doPost(stubber.servletRequest, stubber.servletResponse);
        Error error = new Gson().fromJson(stubber.gathered(), Error.class);

        assertEquals(JsonCommon.UNAUTHORIZED, error.error);
    }

    @Test
    public void test_auction_status_update_is_performed() throws Exception {
        EventDAO eventDAO = EventDAOSQL.getInstance();
        Auction dummyAuction = DBFeeder.createDummyAuction();
        Event dummyEvent = eventDAO.getById(dummyAuction.eventId);

        // Wait 1 second to avoid createdAt == updatedAt
        Thread.sleep(1000);

        Auction updateAuction = new Auction();
        updateAuction.id = dummyAuction.id;
        updateAuction.status = Auction.ACCEPTED;
        String updateAuctionJson = new Gson().toJson(updateAuction);

        HttpServletStubber stubber = new HttpServletStubber();
        stubber.authenticate(dummyEvent.ownerId);
        stubber.body(updateAuctionJson).listen();
        new AuctionUpdateStatus().doPost(stubber.servletRequest, stubber.servletResponse);
        Auction outputAuction = new Gson().fromJson(stubber.gathered(), Auction.class);

        assertEquals(dummyAuction.id, outputAuction.id);
        assertEquals(dummyAuction.name, outputAuction.name);
        assertEquals(dummyAuction.eventId, outputAuction.eventId);
        assertEquals(dummyAuction.ownerId, outputAuction.ownerId);
        assertEquals(Auction.ACCEPTED, outputAuction.status);
        assertEquals(new ImpreciseDate(dummyAuction.startTime), new ImpreciseDate(outputAuction.startTime));
        assertEquals(dummyAuction.startingPrice, outputAuction.startingPrice, 0.0000001);
        assertEquals(dummyAuction.winnerId, outputAuction.winnerId);
    }
}