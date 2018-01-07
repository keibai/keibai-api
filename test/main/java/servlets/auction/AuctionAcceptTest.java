package main.java.servlets.auction;

import com.google.gson.Gson;
import main.java.dao.AuctionDAO;
import main.java.dao.sql.AbstractDBTest;
import main.java.dao.sql.AuctionDAOSQL;
import main.java.mocks.HttpServletStubber;
import main.java.models.Auction;
import main.java.models.Event;
import main.java.models.User;
import main.java.models.meta.Error;
import main.java.utils.*;
import org.junit.Test;

import static org.junit.Assert.*;

public class AuctionAcceptTest extends AbstractDBTest {

    @Test
    public void test_update_can_not_be_done_if_user_not_authenticated() throws Exception {
        HttpServletStubber stubber = new HttpServletStubber();
        stubber.listen();
        new AuctionAccept().doPost(stubber.servletRequest, stubber.servletResponse);
        Error error = new Gson().fromJson(stubber.gathered(), Error.class);

        assertEquals(JsonCommon.UNAUTHORIZED, error.error);
    }

    @Test
    public void test_auction_not_exist_if_auction_id_is_0() throws Exception {
        User dummyUser = DBFeeder.createDummyUser();
        Event dummyEvent = DBFeeder.createDummyEvent();

        HttpServletStubber stubber = new HttpServletStubber();
        stubber.authenticate(dummyUser.id);
        stubber.parameter("id", "0").listen();
        new AuctionAccept().doPost(stubber.servletRequest, stubber.servletResponse);
        Error error = new Gson().fromJson(stubber.gathered(), Error.class);

        assertEquals(AuctionAccept.AUCTION_NOT_EXIST, error.error);
    }

    @Test
    public void test_error_if_auction_status_not_valid() throws Exception {
        Event dummyEvent = DBFeeder.createDummyEvent();
        AuctionDAO auctionDAO = AuctionDAOSQL.getInstance();
        Auction auction = DummyGenerator.getDummyAuction();
        auction.status = Auction.IN_PROGRESS;
        auction.ownerId = dummyEvent.ownerId;
        auction.eventId = dummyEvent.id;
        Auction dummyAuction = auctionDAO.create(auction);


        HttpServletStubber stubber = new HttpServletStubber();
        stubber.authenticate(dummyEvent.ownerId);
        stubber.parameter("id", String.valueOf(dummyAuction.id)).listen();
        new AuctionAccept().doPost(stubber.servletRequest, stubber.servletResponse);
        Error error = new Gson().fromJson(stubber.gathered(), Error.class);

        assertEquals(AuctionAccept.INVALID_STATUS, error.error);
    }

    @Test
    public void test_error_if_auction_does_not_exist() throws Exception {
        User dummyUser = DBFeeder.createDummyUser();
        Event dummyEvent = DBFeeder.createDummyEvent();

        HttpServletStubber stubber = new HttpServletStubber();
        stubber.authenticate(dummyUser.id);
        stubber.parameter("id", "2").listen();
        new AuctionAccept().doPost(stubber.servletRequest, stubber.servletResponse);
        Error error = new Gson().fromJson(stubber.gathered(), Error.class);

        assertEquals(AuctionAccept.AUCTION_NOT_EXIST, error.error);
    }

    @Test
    public void test_unauthorized_when_owner_is_not_the_user() throws Exception {
        User dummyUser = DBFeeder.createDummyUser();
        Auction dummyAuction = DBFeeder.createDummyAuction();

        HttpServletStubber stubber = new HttpServletStubber();
        stubber.authenticate(dummyUser.id);
        stubber.parameter("id", String.valueOf(dummyAuction.id)).listen();
        new AuctionAccept().doPost(stubber.servletRequest, stubber.servletResponse);
        Error error = new Gson().fromJson(stubber.gathered(), Error.class);

        assertEquals(JsonCommon.UNAUTHORIZED, error.error);
    }

    @Test
    public void test_auction_status_update_is_performed() throws Exception {
        Event dummyEvent = DBFeeder.createDummyEvent();
        AuctionDAO auctionDAO = AuctionDAOSQL.getInstance();
        Auction auction = DummyGenerator.getDummyAuction();
        auction.status = Auction.PENDING;
        auction.ownerId = dummyEvent.ownerId;
        auction.eventId = dummyEvent.id;
        Auction dummyAuction = auctionDAO.create(auction);

        // Wait 1 second to avoid createdAt == updatedAt
        Thread.sleep(1000);

        HttpServletStubber stubber = new HttpServletStubber();
        stubber.authenticate(dummyEvent.ownerId);
        stubber.parameter("id", String.valueOf(dummyAuction.id)).listen();
        new AuctionAccept().doPost(stubber.servletRequest, stubber.servletResponse);
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