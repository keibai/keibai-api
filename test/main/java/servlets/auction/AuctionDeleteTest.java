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
import main.java.utils.JsonResponse;
import org.junit.Test;

import static org.junit.Assert.*;

public class AuctionDeleteTest extends AbstractDBTest {

    @Test
    public void unauthorized_when_user_not_authenticated() throws Exception {
        HttpServletStubber stubber = new HttpServletStubber();
        stubber.listen();
        new AuctionDelete().doPost(stubber.servletRequest, stubber.servletResponse);
        Error error = new Gson().fromJson(stubber.gathered(), Error.class);

        assertEquals(JsonResponse.UNAUTHORIZED, error.error);
    }

    @Test
    public void no_parameter_returns_error() throws Exception {
        User dummyUser = DBFeeder.createDummyUser();

        HttpServletStubber stubber = new HttpServletStubber();
        stubber.authenticate(dummyUser.id);
        stubber.listen();
        new AuctionDelete().doPost(stubber.servletRequest, stubber.servletResponse);
        Error error = new Gson().fromJson(stubber.gathered(), Error.class);

        assertEquals(AuctionDelete.ID_NONE, error.error);
    }

    @Test
    public void id_not_numeric_returns_error() throws Exception {
        User dummyUser = DBFeeder.createDummyUser();

        HttpServletStubber stubber = new HttpServletStubber();
        stubber.authenticate(dummyUser.id);
        stubber.parameter("id", "NaN");
        stubber.listen();
        new AuctionDelete().doPost(stubber.servletRequest, stubber.servletResponse);
        Error error = new Gson().fromJson(stubber.gathered(), Error.class);

        assertEquals(AuctionDelete.ID_INVALID, error.error);
    }

    @Test
    public void non_existing_auction_returns_error() throws Exception {
        User dummyUser = DBFeeder.createDummyUser();

        HttpServletStubber stubber = new HttpServletStubber();
        stubber.authenticate(dummyUser.id);
        stubber.parameter("id", String.valueOf(2));
        stubber.listen();
        new AuctionDelete().doPost(stubber.servletRequest, stubber.servletResponse);
        Error error = new Gson().fromJson(stubber.gathered(), Error.class);

        assertEquals(AuctionDelete.AUCTION_NOT_FOUND, error.error);
    }

    @Test
    public void unauthorized_when_user_is_not_the_event_owner() throws Exception {
        User dummyUser = DBFeeder.createDummyUser();
        Auction dummyAuction = DBFeeder.createDummyAuction();

        HttpServletStubber stubber = new HttpServletStubber();
        stubber.authenticate(dummyUser.id);
        stubber.parameter("id", String.valueOf(dummyAuction.id));
        stubber.listen();
        new AuctionDelete().doPost(stubber.servletRequest, stubber.servletResponse);
        Error error = new Gson().fromJson(stubber.gathered(), Error.class);

        assertEquals(JsonResponse.UNAUTHORIZED, error.error);
    }

    @Test
    public void can_not_delete_if_auction_not_pending() throws Exception {
        Event dummyEvent = DBFeeder.createDummyEvent();
        AuctionDAO auctionDAO = AuctionDAOSQL.getInstance();

        Auction dummyAuction = DummyGenerator.getDummyAuction();
        dummyAuction.ownerId = dummyEvent.ownerId;
        dummyAuction.eventId = dummyEvent.id;
        dummyAuction.status = Auction.IN_PROGRESS;
        Auction dbAuction = auctionDAO.create(dummyAuction);

        HttpServletStubber stubber = new HttpServletStubber();
        stubber.authenticate(dummyEvent.ownerId);
        stubber.parameter("id", String.valueOf(dbAuction.id));
        stubber.listen();
        new AuctionDelete().doPost(stubber.servletRequest, stubber.servletResponse);
        Error error = new Gson().fromJson(stubber.gathered(), Error.class);

        assertEquals(AuctionDelete.WRONG_STATUS, error.error);
    }

    @Test
    public void auction_successfully_deleted() throws Exception {
        EventDAO eventDAO = EventDAOSQL.getInstance();
        Auction dummyAuction = DBFeeder.createDummyAuction();
        Event dummyEvent = eventDAO.getById(dummyAuction.eventId);

        HttpServletStubber stubber = new HttpServletStubber();
        stubber.authenticate(dummyEvent.ownerId);
        stubber.parameter("id", String.valueOf(dummyAuction.id));
        stubber.listen();
        new AuctionDelete().doPost(stubber.servletRequest, stubber.servletResponse);
        Msg msg = new Gson().fromJson(stubber.gathered(), Msg.class);

        assertEquals(AuctionDelete.DELETED, msg.msg);
    }
}