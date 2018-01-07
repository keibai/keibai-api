package main.java.dao.sql;

import main.java.dao.*;
import main.java.models.*;
import main.java.utils.DBFeeder;
import main.java.utils.DummyGenerator;
import org.junit.Test;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.*;


public class BidDBTest extends AbstractDBTest {
    private static final double TEST_NEW_PRICE = 100;
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
        GoodDAO goodDAO = GoodDAOSQL.getInstance();
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

        Good dummyGood = DummyGenerator.getDummyGood();
        dummyGood.auctionId = insertedAuction.id;
        Good insertedGood = goodDAO.create(dummyGood);

        Bid bid = DummyGenerator.getDummyBid();
        bid.ownerId = insertedOwner.id;
        bid.auctionId = insertedAuction.id;
        bid.goodId = insertedGood.id;
        Bid insertedBid = bidDAO.create(bid);
        assertNotNull(insertedBid);
        Bid retrievedBid = bidDAO.getById(insertedBid.id);

        assertEquals(insertedBid, retrievedBid);
        assertNotNull(retrievedBid.createdAt);
    }

    @Test
    public void test_bid_not_found_by_id() throws DAOException {

        BidDAO bidDAO = BidDAOSQL.getInstance();
        Bid bid = bidDAO.getById(24);
        assertNull(bid);
    }

    @Test
    public void test_bid_amount_update() throws DAOException {
        GoodDAO goodDAO = GoodDAOSQL.getInstance();
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

        Good dummyGood = DummyGenerator.getDummyGood();
        dummyGood.auctionId = insertedAuction.id;
        Good insertedGood = goodDAO.create(dummyGood);

        Bid bid = DummyGenerator.getDummyBid();
        bid.ownerId = insertedOwner.id;
        bid.auctionId = insertedAuction.id;
        bid.goodId = insertedGood.id;
        Bid insertedBid = bidDAO.create(bid);
        assertNotNull(insertedBid);
        insertedBid.amount = TEST_NEW_PRICE;
        Bid updatedBid = bidDAO.update(insertedBid);
        Bid retrivedBid = bidDAO.getById(insertedBid.id);
        assertEquals(updatedBid,insertedBid);
    }

    @Test
    public void test_full_bid_update() throws DAOException {
        GoodDAO goodDAO = GoodDAOSQL.getInstance();
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

        Good dummyGood = DummyGenerator.getDummyGood();
        dummyGood.auctionId = insertedAuction.id;
        Good insertedGood = goodDAO.create(dummyGood);

        Bid bid = DummyGenerator.getDummyBid();
        bid.ownerId = insertedOwner.id;
        bid.auctionId = insertedAuction.id;
        bid.goodId = insertedGood.id;
        Bid insertedBid = bidDAO.create(bid);
        assertNotNull(insertedBid);

        Bid bid2 = DummyGenerator.getOtherDummyBid();
        bid2.id = insertedBid.id;
        bid2.ownerId = insertedOwner.id;
        bid2.auctionId = insertedAuction.id;
        bid2.goodId = insertedGood.id;
        Bid updatedBid = bidDAO.update(bid2);

        Bid retrievedBid = bidDAO.getById(updatedBid.id);

        assertEquals(updatedBid,retrievedBid);
    }

    @Test
    public void test_update_in_non_existent_bid() throws DAOException {

        BidDAO bidDAO = BidDAOSQL.getInstance();

        Bid updatedBid = DummyGenerator.getDummyBid();
        updatedBid.id = 1;
        Bid modifiedBid = bidDAO.update(updatedBid);
        assertNull(modifiedBid);
    }

    @Test
    public void test_delete_existent_bid() throws DAOException {
        GoodDAO goodDAO = GoodDAOSQL.getInstance();
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

        Good dummyGood = DummyGenerator.getDummyGood();
        dummyGood.auctionId = insertedAuction.id;
        Good insertedGood = goodDAO.create(dummyGood);

        Bid bid = DummyGenerator.getDummyBid();
        bid.ownerId = insertedOwner.id;
        bid.auctionId = insertedAuction.id;
        bid.goodId = insertedGood.id;
        Bid insertedBid = bidDAO.create(bid);
        assertNotNull(insertedBid);
        boolean deleted = bidDAO.delete(insertedBid.id);
        assertTrue(deleted);

        assertNull(bidDAO.getById(1));

    }

    @Test
    public void test_delete_inexistent_bid() throws DAOException {

        BidDAO bidDAO = BidDAOSQL.getInstance();
        boolean deleted = bidDAO.delete(24);
        assertFalse(deleted);
    }

    /* Lists */
    @Test
    public void test_returned_empty_list_when_there_are_not_bids_for_a_user() throws Exception {
        BidDAO bidDAO = BidDAOSQL.getInstance();
        User dummyUser = DBFeeder.createDummyUser();
        List<Bid> bidList = bidDAO.getListByOwnerId(dummyUser.id);
        assertEquals(0, bidList.size());
    }

    @Test
    public void test_list_of_bids_for_a_user() throws Exception {
        GoodDAO goodDAO = GoodDAOSQL.getInstance();
        BidDAO bidDAO = BidDAOSQL.getInstance();
        Auction dummyAuction = DBFeeder.createDummyAuction();

        Good dummyGood = DummyGenerator.getDummyGood();
        dummyGood.auctionId = dummyAuction.id;
        Good insertedGood = goodDAO.create(dummyGood);

        Bid dummyBid = DummyGenerator.getDummyBid();
        dummyBid.auctionId = dummyAuction.id;
        dummyBid.ownerId = dummyAuction.ownerId;
        dummyBid.goodId = insertedGood.id;
        Bid insertedBid = bidDAO.create(dummyBid);

        Bid otherDummyBid = DummyGenerator.getOtherDummyBid();
        otherDummyBid.auctionId = dummyAuction.id;
        otherDummyBid.ownerId = dummyAuction.ownerId;
        otherDummyBid.goodId = insertedGood.id;
        Bid insertedOtherBid = bidDAO.create(otherDummyBid);

        List<Bid> expectedBidList = new LinkedList<Bid>() {{
           add(insertedBid);
           add(insertedOtherBid);
        }};

        List<Bid> outputBidList = bidDAO.getListByOwnerId(dummyAuction.ownerId);
        assertNotNull(outputBidList);
        assertEquals(expectedBidList.size(), outputBidList.size());

        assertBidListEquals(expectedBidList, outputBidList);
    }

    @Test
    public void test_returned_empty_list_when_there_are_not_bids_for_an_auction() throws Exception {
        GoodDAO goodDAO = GoodDAOSQL.getInstance();
        BidDAO bidDAO = BidDAOSQL.getInstance();
        Auction dummyAuction = DBFeeder.createDummyAuction();

        Good dummyGood = DummyGenerator.getDummyGood();
        dummyGood.auctionId = dummyAuction.id;
        Good insertedGood = goodDAO.create(dummyGood);

        Bid dummyBid = DummyGenerator.getDummyBid();
        dummyBid.auctionId = dummyAuction.id;
        dummyBid.ownerId = dummyAuction.ownerId;
        dummyBid.goodId = insertedGood.id;
        Bid insertedBid = bidDAO.create(dummyBid);

        Bid otherDummyBid = DummyGenerator.getOtherDummyBid();
        otherDummyBid.auctionId = dummyAuction.id;
        otherDummyBid.ownerId = dummyAuction.ownerId;
        otherDummyBid.goodId = insertedGood.id;
        Bid insertedOtherBid = bidDAO.create(otherDummyBid);

        List<Bid> expectedBidList = new LinkedList<Bid>() {{
            add(insertedBid);
            add(insertedOtherBid);
        }};

        List<Bid> outputBidList = bidDAO.getListByAuctionId(dummyAuction.id);
        assertNotNull(outputBidList);
        assertEquals(expectedBidList.size(), outputBidList.size());

        assertBidListEquals(expectedBidList, outputBidList);
    }

    @Test
    public void test_list_of_bids_for_an_auction() throws Exception {
        BidDAO bidDAO = BidDAOSQL.getInstance();
        Auction dummyAuction = DBFeeder.createDummyAuction();
        List<Bid> bidList = bidDAO.getListByAuctionId(dummyAuction.id);
        assertEquals(0, bidList.size());
    }

    public static void assertBidListEquals(List<Bid> expectedBidList, List<Bid> outputBidList) {
        Iterator<Bid> expectedIterator = expectedBidList.iterator();
        Iterator<Bid> outputIterator = outputBidList.iterator();

        while (expectedIterator.hasNext() && outputIterator.hasNext()) {
            Bid expectedBid = expectedIterator.next();
            Bid outputBid = outputIterator.next();

            assertEquals(expectedBid.id, outputBid.id);
            assertEquals(expectedBid.auctionId, outputBid.auctionId);
            assertEquals(expectedBid.ownerId, outputBid.ownerId);
            assertEquals(expectedBid.amount, outputBid.amount, 0.01);
        }
    }
}