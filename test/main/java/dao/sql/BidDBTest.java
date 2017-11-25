package main.java.dao.sql;

import main.java.dao.*;
import main.java.models.Auction;
import main.java.models.Bid;
import main.java.models.Event;
import main.java.models.User;
import main.java.utils.DummyGenerator;
import org.junit.Test;

import static org.junit.Assert.*;

public class BidDBTest extends AbstractDBTest {

    @Test(expected = DAOException.class)
    public void test_insertion_of_bid_without_auction_throws_DAOException() throws DAOException {
        BidDAO bidDAO = BidDAOSQL.getInstance();
        UserDAO userDAO = UserDAOSQL.getInstance();

        User dummyOwner = DummyGenerator.getDummyUser();
        User insertedOwner = userDAO.create(dummyOwner);

        Bid dummyBid = DummyGenerator.getDummyBid();
        dummyBid.ownerId = insertedOwner.id;

        Bid insertedBid = bidDAO.create(dummyBid);
    }

    @Test(expected = DAOException.class)
    public void test_insertion_of_bid_without_owner_throws_DAOException() throws DAOException {
        BidDAO bidDAO = BidDAOSQL.getInstance();
        AuctionDAO auctionDAO = AuctionDAOSQL.getInstance();

        Auction dummyAuction = DummyGenerator.getDummyAuction();
        Auction insertedauction = auctionDAO.create(dummyAuction);

        Bid dummyBid = DummyGenerator.getDummyBid();
        dummyBid.ownerId = insertedauction.id;
        Bid insertedBid = bidDAO.create(dummyBid);
    }

    @Test
    public void test_insertion_and_retrieval_of_bid() throws DAOException {
        BidDAO bidDAO = BidDAOSQL.getInstance();
        EventDAO eventDAO = EventDAOSQL.getInstance();
        UserDAO userDAO = UserDAOSQL.getInstance();
        User owner = DummyGenerator.getDummyUser();
        User insertedOwner = userDAO.create(owner);
        assertNotNull(insertedOwner);

        Event event = DummyGenerator.getDummyEvent();
        event.ownerId = insertedOwner.id;
        Event insertedEvent = eventDAO.create(event);

        AuctionDAO auctionDAO = AuctionDAOSQL.getInstance();
        Auction auction = DummyGenerator.getDummyAuction();
        auction.ownerId = insertedOwner.id;
        auction.eventId = insertedEvent.id;
        Auction insertedAuction = auctionDAO.create(auction);
        assertNotNull(insertedAuction);

        Bid bid = DummyGenerator.getDummyBid();
        bid.ownerId = insertedOwner.id;
        bid.auctionId = insertedAuction.id;
        Bid insertedBid = bidDAO.create(bid);
        assertNotNull(insertedBid);
        Bid retrievedBid = bidDAO.getById(insertedBid.id);

        assertEquals(insertedBid, retrievedBid);

    }

    @Test
    public void test_bid_not_found_by_id() {
        throw new UnsupportedOperationException("TODO: Implement this");
    }

    @Test
    public void test_bid_amount_update() {
        throw new UnsupportedOperationException("TODO: Implement this");
    }

    @Test
    public void test_full_bid_update() {
        throw new UnsupportedOperationException("TODO: Implement this");
    }

    @Test
    public void test_update_in_non_existent_bid() {
        throw new UnsupportedOperationException("TODO: Implement this");
    }

    @Test
    public void test_delete_existent_bid() {
        throw new UnsupportedOperationException("TODO: Implement this");
    }

    @Test
    public void test_delete_inexistent_bid() {
        throw new UnsupportedOperationException("TODO: Implement this");
    }
}