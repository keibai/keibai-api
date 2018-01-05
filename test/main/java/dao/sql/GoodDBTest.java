package main.java.dao.sql;

import main.java.dao.*;
import main.java.models.Auction;
import main.java.models.Event;
import main.java.models.Good;
import main.java.models.User;
import main.java.utils.DBFeeder;
import main.java.utils.DummyGenerator;
import org.junit.Test;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.*;

public class GoodDBTest extends AbstractDBTest {

    private static final String TEST_NEW_NAME = "TestNewName";

    @Test
    public void test_good_is_properly_inserted_and_retrieved_from_db() throws DAOException, NotFoundException {
        UserDAO userDAO = UserDAOSQL.getInstance();
        EventDAO eventDAO = EventDAOSQL.getInstance();
        AuctionDAO auctionDAO = AuctionDAOSQL.getInstance();
        GoodDAO goodDAO = GoodDAOSQL.getInstance();

        User owner = DummyGenerator.getDummyUser();
        User insertedOwner = userDAO.create(owner);

        Event event = DummyGenerator.getDummyEvent();
        event.ownerId = insertedOwner.id;
        Event insertedEvent = eventDAO.create(event);

        Auction auction = DummyGenerator.getDummyAuction();
        auction.ownerId = insertedOwner.id;
        auction.eventId = insertedEvent.id;
        Auction insertedAuction = auctionDAO.create(auction);

        Good good = DummyGenerator.getDummyGood();
        good.auctionId = insertedAuction.id;
        Good insertedGood = goodDAO.create(good);

        Good retrievedGood = goodDAO.getById(insertedGood.id);
        assertEquals(insertedGood, retrievedGood);
    }

    @Test(expected = DAOException.class)
    public void test_good_insertion_fails_if_auction_not_exists() throws DAOException {
        GoodDAO goodDAO = GoodDAOSQL.getInstance();
        Good good = DummyGenerator.getDummyGood();
        Good insertedGood = goodDAO.create(good);
    }

    @Test
    public void test_when_good_not_found_by_id()  throws  DAOException{
        GoodDAO goodDAO = GoodDAOSQL.getInstance();
        Good good = goodDAO.getById(24);
        assertNull(good);
    }

    @Test
    public void test_returned_empty_list_when_there_are_not_goods_for_an_auction() throws Exception {
        GoodDAO goodDAO = GoodDAOSQL.getInstance();
        Auction dummyAuction = DBFeeder.createDummyAuction();
        List<Good> goodList = goodDAO.getListByAuctionId(dummyAuction.id);
        assertEquals(0, goodList.size());
    }

    @Test
    public void test_list_of_goods_for_an_auction() throws Exception {
        GoodDAO goodDAO = GoodDAOSQL.getInstance();
        Auction dummyAuction = DBFeeder.createDummyAuction();

        Good dummyGood1 = DummyGenerator.getDummyGood();
        dummyGood1.auctionId = dummyAuction.id;
        Good insertedGood1 = goodDAO.create(dummyGood1);

        Good dummyGood2 = DummyGenerator.getOtherDummyGood();
        dummyGood2.auctionId = dummyAuction.id;
        Good insertedGood2 = goodDAO.create(dummyGood2);

        List<Good> expectedGoodList = new LinkedList<Good>() {{
           add(insertedGood1);
           add(insertedGood2);
        }};

        List<Good> outputGoodList = goodDAO.getListByAuctionId(dummyAuction.id);
        assertNotNull(outputGoodList);
        assertEquals(expectedGoodList.size(), outputGoodList.size());

        assertGoodListEquals(expectedGoodList, outputGoodList);
    }

    @Test
    public void test_good_update_name() throws DAOException {

        UserDAO userDAO = UserDAOSQL.getInstance();
        EventDAO eventDAO = EventDAOSQL.getInstance();
        AuctionDAO auctionDAO = AuctionDAOSQL.getInstance();
        GoodDAO goodDAO = GoodDAOSQL.getInstance();

        User owner = DummyGenerator.getDummyUser();
        User insertedOwner = userDAO.create(owner);

        Event event = DummyGenerator.getDummyEvent();
        event.ownerId = insertedOwner.id;
        Event insertedEvent = eventDAO.create(event);

        Auction auction = DummyGenerator.getDummyAuction();
        auction.ownerId = insertedOwner.id;
        auction.eventId = insertedEvent.id;
        Auction insertedAuction = auctionDAO.create(auction);

        Good good = DummyGenerator.getDummyGood();
        good.auctionId = insertedAuction.id;
        Good insertedGood = goodDAO.create(good);

        insertedGood.name = TEST_NEW_NAME;
        Good updatedGood = goodDAO.update(insertedGood);

        Good retrievedGood = goodDAO.getById(updatedGood.id);

        assertEquals(updatedGood, retrievedGood);
        assertNotNull(updatedGood);
        assertNotEquals(good.name, retrievedGood.name);
    }

    @Test
    public void test_full_good_update() throws DAOException {
        UserDAO userDAO = UserDAOSQL.getInstance();
        EventDAO eventDAO = EventDAOSQL.getInstance();
        AuctionDAO auctionDAO = AuctionDAOSQL.getInstance();
        GoodDAO goodDAO = GoodDAOSQL.getInstance();

        User owner = DummyGenerator.getDummyUser();
        User insertedOwner = userDAO.create(owner);

        Event event = DummyGenerator.getDummyEvent();
        event.ownerId = insertedOwner.id;
        Event insertedEvent = eventDAO.create(event);

        Auction auction = DummyGenerator.getDummyAuction();
        auction.ownerId = insertedOwner.id;
        auction.eventId = insertedEvent.id;
        Auction insertedAuction = auctionDAO.create(auction);

        Good good = DummyGenerator.getDummyGood();
        good.auctionId = insertedAuction.id;
        Good insertedGood = goodDAO.create(good);

        Good good2 = DummyGenerator.getOtherDummyGood();
        good2.id = insertedGood.id;
        good2.auctionId = insertedAuction.id;
        Good updateGood = goodDAO.update(good2);
        assertEquals(good2,updateGood);
    }

    @Test
    public void test_update_in_non_existing_good() throws DAOException {
        GoodDAO goodDAO = GoodDAOSQL.getInstance();

        Good updatedGood = DummyGenerator.getDummyGood();
        updatedGood.id = 1;
        Good modifiedGood = goodDAO.update(updatedGood);
        assertNull(modifiedGood);
    }

    @Test
    public void test_delete_existent_good() throws DAOException {

        UserDAO userDAO = UserDAOSQL.getInstance();
        EventDAO eventDAO = EventDAOSQL.getInstance();
        AuctionDAO auctionDAO = AuctionDAOSQL.getInstance();
        GoodDAO goodDAO = GoodDAOSQL.getInstance();

        User owner = DummyGenerator.getDummyUser();
        User insertedOwner = userDAO.create(owner);

        Event event = DummyGenerator.getDummyEvent();
        event.ownerId = insertedOwner.id;
        Event insertedEvent = eventDAO.create(event);

        Auction auction = DummyGenerator.getDummyAuction();
        auction.ownerId = insertedOwner.id;
        auction.eventId = insertedEvent.id;
        Auction insertedAuction = auctionDAO.create(auction);

        Good good = DummyGenerator.getDummyGood();
        good.auctionId = insertedAuction.id;
        Good insertedGood = goodDAO.create(good);
        assertNotNull(insertedGood);

        boolean deleted = goodDAO.delete(insertedGood.id);
        assertTrue(deleted);

        assertNull(goodDAO.getById(insertedGood.id));
    }

    @Test
    public void test_delete_inexistent_user() throws DAOException {
        GoodDAO goodDAO = GoodDAOSQL.getInstance();
        boolean deleted = goodDAO.delete(24);
        assertFalse(deleted);
    }

    public static void assertGoodListEquals(List<Good> expectedGoodList, List<Good> outputGoodList) {
        Iterator<Good> expectedIterator = expectedGoodList.iterator();
        Iterator<Good> outputIterator = outputGoodList.iterator();

        while (expectedIterator.hasNext() && outputIterator.hasNext()) {
            Good expectedGood = expectedIterator.next();
            Good outputGood = outputIterator.next();

            assertEquals(expectedGood.id, outputGood.id);
            assertEquals(expectedGood.name, outputGood.name);
            assertEquals(expectedGood.auctionId, outputGood.auctionId);
            assertEquals(expectedGood.image, outputGood.image);
        }
    }
}
