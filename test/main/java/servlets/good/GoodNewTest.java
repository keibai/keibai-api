package main.java.servlets.good;

import com.google.gson.Gson;
import main.java.dao.AuctionDAO;
import main.java.dao.DAOException;
import main.java.dao.EventDAO;
import main.java.dao.sql.AbstractDBTest;
import main.java.dao.sql.AuctionDAOSQL;
import main.java.dao.sql.EventDAOSQL;
import main.java.mocks.HttpServletStubber;
import main.java.models.Auction;
import main.java.models.Event;
import main.java.models.Good;
import main.java.models.User;
import main.java.models.meta.Error;
import main.java.utils.DBFeeder;
import main.java.utils.DummyGenerator;
import main.java.utils.HttpResponse;
import main.java.utils.JsonCommon;
import org.junit.Test;

import javax.servlet.ServletException;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class GoodNewTest extends AbstractDBTest {

    @Test
    public void test_not_authenticate_user_can_not_create_good() throws DAOException, IOException, ServletException {
        HttpServletStubber stubber = new HttpServletStubber();

        Good attemptGood = DummyGenerator.getDummyGood();
        String attemptGoodJson = new Gson().toJson(attemptGood);
        stubber.body(attemptGoodJson).listen();
        new GoodNew().doPost(stubber.servletRequest, stubber.servletResponse);

        Error error = new Gson().fromJson(stubber.gathered(), Error.class);
        assertEquals("Unauthorized.", error.error);
    }

    @Test
    public void test_good_with_blank_name_can_not_be_created() throws DAOException, IOException, ServletException {
        Good attemptGood = DummyGenerator.getDummyGood();
        attemptGood.name = "";
        common_good_error_test(attemptGood, GoodNew.NAME_ERROR);
    }

    @Test
    public void test_good_with_no_image_can_not_be_created() throws ServletException, DAOException, IOException {
        Good attemptGood = DummyGenerator.getDummyGood();
        attemptGood.image="";
        common_good_error_test(attemptGood, GoodNew.IMAGE_ERROR);
    }

    @Test
    public void should_create_new_good() throws IOException, ServletException, DAOException {
        Auction dummyAuction = DBFeeder.createDummyAuction();

        Good attemptGood = DummyGenerator.getDummyGood();
        attemptGood.auctionId = dummyAuction.id;
        String attemptGoodJson = new Gson().toJson(attemptGood);

        HttpServletStubber stubber = new HttpServletStubber();
        stubber.authenticate(dummyAuction.ownerId);
        stubber.body(attemptGoodJson).listen();
        new GoodNew().doPost(stubber.servletRequest, stubber.servletResponse);

        Good outputGood = new Gson().fromJson(stubber.gathered(), Good.class);

        assertEquals(attemptGood.name, outputGood.name);
        assertEquals(attemptGood.image, outputGood.image);
        assertEquals(attemptGood.auctionId, outputGood.auctionId);
    }

    @Test
    public void test_good_creation_fails_if_not_owner_of_the_auction() throws Exception {
        EventDAO eventDAO = EventDAOSQL.getInstance();
        AuctionDAO auctionDAO = AuctionDAOSQL.getInstance();

        User auctionOwner = DBFeeder.createDummyUser();
        User eventOwner = DBFeeder.createOtherDummyUser();

        Event dummyEvent = DummyGenerator.getDummyEvent();
        dummyEvent.ownerId = eventOwner.id;
        Event dbEvent = eventDAO.create(dummyEvent);

        Auction dummyAuction = DummyGenerator.getDummyAuction();
        dummyAuction.ownerId = auctionOwner.id;
        dummyAuction.eventId = dbEvent.id;
        dummyAuction.winnerId = 0;
        Auction dbAuction = auctionDAO.create(dummyAuction);

        Good attemptGood = DummyGenerator.getDummyGood();
        attemptGood.auctionId = dbAuction.id;
        String attemptGoodJson = new Gson().toJson(attemptGood);

        HttpServletStubber stubber = new HttpServletStubber();
        stubber.authenticate(eventOwner.id);
        stubber.body(attemptGoodJson).listen();
        new GoodNew().doPost(stubber.servletRequest, stubber.servletResponse);

        Error error = new Gson().fromJson(stubber.gathered(), Error.class);
        assertEquals(JsonCommon.UNAUTHORIZED, error.error);
    }

    @Test
    public void test_good_creation_success_if_owner_of_the_auction() throws Exception {
        EventDAO eventDAO = EventDAOSQL.getInstance();
        AuctionDAO auctionDAO = AuctionDAOSQL.getInstance();

        User auctionOwner = DBFeeder.createDummyUser();
        User eventOwner = DBFeeder.createOtherDummyUser();

        Event dummyEvent = DummyGenerator.getDummyEvent();
        dummyEvent.ownerId = eventOwner.id;
        Event dbEvent = eventDAO.create(dummyEvent);

        Auction dummyAuction = DummyGenerator.getDummyAuction();
        dummyAuction.ownerId = auctionOwner.id;
        dummyAuction.eventId = dbEvent.id;
        dummyAuction.winnerId = 0;
        Auction dbAuction = auctionDAO.create(dummyAuction);

        Good attemptGood = DummyGenerator.getDummyGood();
        attemptGood.auctionId = dbAuction.id;
        String attemptGoodJson = new Gson().toJson(attemptGood);

        HttpServletStubber stubber = new HttpServletStubber();
        stubber.authenticate(auctionOwner.id);
        stubber.body(attemptGoodJson).listen();
        new GoodNew().doPost(stubber.servletRequest, stubber.servletResponse);

        Good outputGood = new Gson().fromJson(stubber.gathered(), Good.class);

        assertEquals(attemptGood.name, outputGood.name);
        assertEquals(attemptGood.image, outputGood.image);
        assertEquals(attemptGood.auctionId, outputGood.auctionId);
    }

    private void common_good_error_test(Good attemptGood, String errorMsg) throws DAOException, IOException, ServletException {
        Auction dummyAuction = DBFeeder.createDummyAuction();

        attemptGood.auctionId = dummyAuction.id;
        String attemptGoodJson = new Gson().toJson(attemptGood);

        HttpServletStubber stubber = new HttpServletStubber();
        stubber.authenticate(dummyAuction.ownerId);
        stubber.body(attemptGoodJson).listen();
        new GoodNew().doPost(stubber.servletRequest, stubber.servletResponse);

        Error error = new Gson().fromJson(stubber.gathered(), Error.class);
        assertEquals(errorMsg, error.error);
    }

}