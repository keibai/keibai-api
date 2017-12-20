package main.java.dao.sql;

import main.java.dao.*;
import main.java.models.Auction;
import main.java.utils.DBFeeder;
import main.java.utils.DummyGenerator;
import main.java.models.Event;
import main.java.models.User;
import main.java.utils.ImpreciseDate;
import org.junit.Test;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.*;

public class AuctionDBTest extends AbstractDBTest {

    private static final String TEST_NEW_NAME = "TestNewName";

    @Test
    public void test_auction_is_properly_inserted_and_retrieved_from_db() throws DAOException, NotFoundException {
        AuctionDAO auctionDAO = AuctionDAOSQL.getInstance();
        UserDAO userDAO = UserDAOSQL.getInstance();
        EventDAO eventDAO = EventDAOSQL.getInstance();

        User owner = DummyGenerator.getDummyUser();
        User insertedOwner = userDAO.create(owner);
        assertNotNull(insertedOwner);

        User winner = DummyGenerator.getOtherDummyUser();
        User insertedWinner = userDAO.create(winner);
        assertNotNull(insertedWinner);

        Event event = DummyGenerator.getDummyEvent();
        event.ownerId = insertedOwner.id;
        Event insertedEvent = eventDAO.create(event);
        assertNotNull(insertedEvent);

        Auction auction = DummyGenerator.getDummyAuction();
        auction.eventId = insertedEvent.id;
        auction.ownerId = insertedOwner.id;
        auction.winnerId = insertedWinner.id;
        Auction insertedAuction = auctionDAO.create(auction);
        assertNotNull(insertedAuction);

        Auction retrievedAuction = auctionDAO.getById(insertedAuction.id);

        assertEquals(insertedAuction, retrievedAuction);
    }

    @Test
    public void test_get_inexistent_auction() throws DAOException {
        AuctionDAO auctionDAO = AuctionDAOSQL.getInstance();
        Auction auction = auctionDAO.getById(34);
        assertNull(auction);
    }

    @Test(expected = DAOException.class)
    public void test_insert_of_auction_without_event_fails() throws DAOException {
        AuctionDAO auctionDAO = AuctionDAOSQL.getInstance();
        UserDAO userDAO = UserDAOSQL.getInstance();

        User owner = DummyGenerator.getDummyUser();
        User insertedOwner = userDAO.create(owner);
        assertNotNull(insertedOwner);

        User winner = DummyGenerator.getOtherDummyUser();
        User insertedWinner = userDAO.create(winner);
        assertNotNull(insertedWinner);

        Auction auction = DummyGenerator.getDummyAuction();
        auction.ownerId = insertedOwner.id;
        auction.winnerId = insertedWinner.id;
        Auction insertedAuction = auctionDAO.create(auction);
    }

    @Test(expected = DAOException.class)
    public void test_insert_of_auction_without_owner_fails() throws DAOException {
        AuctionDAO auctionDAO = AuctionDAOSQL.getInstance();
        UserDAO userDAO = UserDAOSQL.getInstance();
        EventDAO eventDAO = EventDAOSQL.getInstance();

        User owner = DummyGenerator.getDummyUser();
        User insertedOwner = userDAO.create(owner);
        assertNotNull(insertedOwner);

        User winner = DummyGenerator.getOtherDummyUser();
        User insertedWinner = userDAO.create(winner);
        assertNotNull(insertedWinner);

        Event event = DummyGenerator.getDummyEvent();
        event.ownerId = insertedOwner.id;
        Event insertedEvent = eventDAO.create(event);
        assertNotNull(insertedEvent);

        Auction auction = DummyGenerator.getDummyAuction();
        auction.eventId = insertedEvent.id;
        auction.winnerId = insertedWinner.id;
        Auction insertedAuction = auctionDAO.create(auction);
    }

    @Test
    public void test_insert_of_auction_without_winner() throws DAOException {
        AuctionDAO auctionDAO = AuctionDAOSQL.getInstance();
        UserDAO userDAO = UserDAOSQL.getInstance();
        EventDAO eventDAO = EventDAOSQL.getInstance();

        User owner = DummyGenerator.getDummyUser();
        User insertedOwner = userDAO.create(owner);
        assertNotNull(insertedOwner);

        User winner = DummyGenerator.getOtherDummyUser();
        User insertedWinner = userDAO.create(winner);
        assertNotNull(insertedWinner);

        Event event = DummyGenerator.getDummyEvent();
        event.ownerId = insertedOwner.id;
        Event insertedEvent = eventDAO.create(event);
        assertNotNull(insertedEvent);

        Auction auction = DummyGenerator.getDummyAuction();
        auction.eventId = insertedEvent.id;
        auction.ownerId = insertedOwner.id;
        Auction insertedAuction = auctionDAO.create(auction);
        assertTrue(insertedAuction.winnerId == 0);
    }

    @Test
    public void test_returned_empty_list_when_there_are_not_auctions_for_an_event() throws Exception {
        AuctionDAO auctionDAO = AuctionDAOSQL.getInstance();
        Event dummyEvent = DBFeeder.createDummyEvent();
        List<Auction> auctionList = auctionDAO.getListByEventId(dummyEvent.id);
        assertEquals(0, auctionList.size());
    }

    @Test
    public void test_list_of_auctions_for_an_event() throws Exception {
        AuctionDAO auctionDAO = AuctionDAOSQL.getInstance();
        Event dummyEvent = DBFeeder.createDummyEvent();

        Auction auction = DummyGenerator.getDummyAuction();
        auction.ownerId = dummyEvent.ownerId;
        auction.eventId = dummyEvent.id;
        Auction insertedAuction = auctionDAO.create(auction);

        Auction auctionOther = DummyGenerator.getOtherDummyAuction();
        auctionOther.ownerId = dummyEvent.ownerId;
        auctionOther.eventId = dummyEvent.id;
        Auction insertedOtherAuction = auctionDAO.create(auctionOther);

        List<Auction> expectedAuctionList = new LinkedList<Auction>() {{
            add(insertedAuction);
            add(insertedOtherAuction);
        }};

        List<Auction> outputAuctionList = auctionDAO.getListByEventId(dummyEvent.id);
        assertNotNull(outputAuctionList);
        assertEquals(expectedAuctionList.size(), outputAuctionList.size());

        assertAuctionListEquals(expectedAuctionList, outputAuctionList);
    }

    @Test
    public void test_update_auction_winner_id() throws DAOException {
        AuctionDAO auctionDAO = AuctionDAOSQL.getInstance();
        UserDAO userDAO = UserDAOSQL.getInstance();
        EventDAO eventDAO = EventDAOSQL.getInstance();

        User owner = DummyGenerator.getDummyUser();
        User insertedOwner = userDAO.create(owner);
        assertNotNull(insertedOwner);

        User winner = DummyGenerator.getOtherDummyUser();
        User insertedWinner = userDAO.create(winner);
        assertNotNull(insertedWinner);

        Event event = DummyGenerator.getDummyEvent();
        event.ownerId = insertedOwner.id;
        Event insertedEvent = eventDAO.create(event);
        assertNotNull(insertedEvent);

        Auction auction = DummyGenerator.getDummyAuction();
        auction.eventId = insertedEvent.id;
        auction.ownerId = insertedOwner.id;
        Auction insertedAuction = auctionDAO.create(auction);
        assertNotNull(insertedAuction);
        assertTrue(insertedAuction.winnerId == 0);

        insertedAuction.winnerId = insertedWinner.id;
        Auction updatedAuction = auctionDAO.update(insertedAuction);
        assertNotNull(updatedAuction);
        assertEquals(updatedAuction.winnerId, insertedWinner.id);
    }

    @Test
    public void test_auction_name_update() throws DAOException {
        AuctionDAO auctionDAO = AuctionDAOSQL.getInstance();
        UserDAO userDAO = UserDAOSQL.getInstance();
        EventDAO eventDAO = EventDAOSQL.getInstance();

        User owner = DummyGenerator.getDummyUser();
        User insertedOwner = userDAO.create(owner);
        assertNotNull(insertedOwner);

        User winner = DummyGenerator.getOtherDummyUser();
        User insertedWinner = userDAO.create(winner);
        assertNotNull(insertedWinner);

        Event event = DummyGenerator.getDummyEvent();
        event.ownerId = insertedOwner.id;
        Event insertedEvent = eventDAO.create(event);
        assertNotNull(insertedEvent);

        Auction auction = DummyGenerator.getDummyAuction();
        auction.eventId = insertedEvent.id;
        auction.ownerId = insertedOwner.id;
        Auction insertedAuction = auctionDAO.create(auction);
        assertNotNull(insertedAuction);
        assertTrue(insertedAuction.winnerId == 0);

        insertedAuction.name = TEST_NEW_NAME;
        Auction updatedAuction = auctionDAO.update(insertedAuction);
        assertNotNull(updatedAuction);
        assertEquals(updatedAuction.winnerId, 0);
        assertEquals(updatedAuction.name, insertedAuction.name);
    }

    @Test
    public void test_auction_full_update() throws DAOException {
        AuctionDAO auctionDAO = AuctionDAOSQL.getInstance();
        UserDAO userDAO = UserDAOSQL.getInstance();
        EventDAO eventDAO = EventDAOSQL.getInstance();

        User owner = DummyGenerator.getDummyUser();
        User insertedOwner = userDAO.create(owner);
        assertNotNull(insertedOwner);

        User winner = DummyGenerator.getOtherDummyUser();
        User insertedWinner = userDAO.create(winner);
        assertNotNull(insertedWinner);

        Event event = DummyGenerator.getDummyEvent();
        event.ownerId = insertedOwner.id;
        Event insertedEvent = eventDAO.create(event);
        assertNotNull(insertedEvent);

        Auction auction = DummyGenerator.getDummyAuction();
        auction.eventId = insertedEvent.id;
        auction.ownerId = insertedOwner.id;
        Auction insertedAuction = auctionDAO.create(auction);

        Auction auction2 = DummyGenerator.getOtherDummyAuction();
        auction2.id = insertedAuction.id;
        auction2.eventId = insertedEvent.id;
        auction2.ownerId = insertedOwner.id;
        auction2.winnerId = insertedWinner.id;
        Auction updatedAuction = auctionDAO.update(auction2);

        assertEquals(auction2, updatedAuction);
    }

    @Test
    public void test_delete_existent_auction() throws DAOException {
        AuctionDAO auctionDAO = AuctionDAOSQL.getInstance();
        UserDAO userDAO = UserDAOSQL.getInstance();
        EventDAO eventDAO = EventDAOSQL.getInstance();

        User owner = DummyGenerator.getDummyUser();
        User insertedOwner = userDAO.create(owner);
        assertNotNull(insertedOwner);

        User winner = DummyGenerator.getOtherDummyUser();
        User insertedWinner = userDAO.create(winner);
        assertNotNull(insertedWinner);

        Event event = DummyGenerator.getDummyEvent();
        event.ownerId = insertedOwner.id;
        Event insertedEvent = eventDAO.create(event);
        assertNotNull(insertedEvent);

        Auction auction = DummyGenerator.getDummyAuction();
        auction.eventId = insertedEvent.id;
        auction.ownerId = insertedOwner.id;
        auction.winnerId = insertedWinner.id;
        Auction insertedAuction = auctionDAO.create(auction);
        assertNotNull(insertedAuction);


        boolean deleted = auctionDAO.delete(insertedAuction.id);
        assertTrue(deleted);

        assertNull(auctionDAO.getById(insertedAuction.id));
    }

    @Test
    public void test_delete_inexistent_auction() throws DAOException {
        AuctionDAO auctionDAO = AuctionDAOSQL.getInstance();
        boolean isDeleted = auctionDAO.delete(1234);
        assertFalse(isDeleted);
    }

    public static void assertAuctionListEquals(List<Auction> expectedAuctionList, List<Auction> outputAuctionList) {
        Iterator<Auction> expectedIterator = expectedAuctionList.iterator();
        Iterator<Auction> outputIterator = outputAuctionList.iterator();

        while (expectedIterator.hasNext() && outputIterator.hasNext()) {
            Auction expectedAuction = expectedIterator.next();
            Auction outputAuction = outputIterator.next();

            assertEquals(expectedAuction.id, outputAuction.id);
            assertEquals(expectedAuction.name, outputAuction.name);
            assertEquals(expectedAuction.startingPrice, outputAuction.startingPrice, 0.0000001);
            assertEquals(new ImpreciseDate(expectedAuction.startTime), new ImpreciseDate(outputAuction.startTime));
            assertEquals(expectedAuction.eventId, outputAuction.eventId);
            assertEquals(expectedAuction.ownerId, outputAuction.ownerId);
            assertEquals(expectedAuction.status, outputAuction.status);
            assertEquals(expectedAuction.winnerId, outputAuction.winnerId);
        }
    }

}
