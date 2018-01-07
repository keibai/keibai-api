package main.java.utils;

import main.java.dao.*;
import main.java.dao.sql.*;
import main.java.models.*;


public class DBFeeder {

    public static User createDummyUser() throws DAOException {
        User dummyUser = DummyGenerator.getDummyUser();
        // Hash password like if he had really signed up.
        dummyUser.password = new PasswordAuthentication().hash(dummyUser.password.toCharArray());

        UserDAO userDAO = UserDAOSQL.getInstance();
        User user = userDAO.create(dummyUser);
        return user;
    }

    public static User createOtherDummyUser() throws DAOException {
        User dummyUser = DummyGenerator.getOtherDummyUser();
        // Hash password like if he had really signed up.
        dummyUser.password = new PasswordAuthentication().hash(dummyUser.password.toCharArray());

        UserDAO userDAO = UserDAOSQL.getInstance();
        User user = userDAO.create(dummyUser);
        return user;
    }

    public static Event createDummyEvent() throws DAOException {
        User dummyUser = DummyGenerator.getOtherDummyUser();
        dummyUser.password = new PasswordAuthentication().hash(dummyUser.password.toCharArray());

        UserDAO userDAO = UserDAOSQL.getInstance();
        User user = userDAO.create(dummyUser);

        Event dummyEvent = DummyGenerator.getDummyEvent();
        dummyEvent.ownerId = user.id;

        EventDAO eventDAO = EventDAOSQL.getInstance();
        Event event = eventDAO.create(dummyEvent);
        return event;
    }

    public static Event createDummyEvent(int userId) throws DAOException {
        Event dummyEvent = DummyGenerator.getDummyEvent();
        dummyEvent.ownerId = userId;

        EventDAO eventDAO = EventDAOSQL.getInstance();
        Event event = eventDAO.create(dummyEvent);
        return event;
    }

    public static Event createOtherDummyEvent(int ownerId) throws DAOException {
        Event dummyEvent = DummyGenerator.getDummyEvent();
        dummyEvent.ownerId = ownerId;

        EventDAO eventDAO = EventDAOSQL.getInstance();
        Event event = eventDAO.create(dummyEvent);
        return event;
    }

    public static Auction createDummyAuction() throws DAOException {
        Event dummyEvent = createDummyEvent();

        Auction dummyAuction = DummyGenerator.getDummyAuction();
        dummyAuction.eventId = dummyEvent.id;
        dummyAuction.ownerId = dummyEvent.ownerId;
        dummyAuction.winnerId = dummyEvent.ownerId;

        AuctionDAO auctionDAO = AuctionDAOSQL.getInstance();
        Auction auction = auctionDAO.create(dummyAuction);
        return auction;
    }

    public static Auction createDummyAuction(int eventId, int ownerId) throws DAOException {
        return DBFeeder.createDummyAuction(eventId, ownerId, 0);
    }

    public static Auction createDummyAuction(int eventId, int ownerId, int winnerId) throws DAOException {
        Auction dummyAuction = DummyGenerator.getDummyAuction();
        dummyAuction.eventId = eventId;
        dummyAuction.ownerId = ownerId;
        dummyAuction.winnerId = winnerId;

        AuctionDAO auctionDAO = AuctionDAOSQL.getInstance();
        Auction auction = auctionDAO.create(dummyAuction);
        return auction;
    }

    public static Auction createOtherDummyAuction(int eventId, int ownerId) throws DAOException {
        Auction dummyAuction = DummyGenerator.getOtherDummyAuction();
        dummyAuction.eventId = eventId;
        dummyAuction.ownerId = ownerId;
        dummyAuction.winnerId = 0;

        AuctionDAO auctionDAO = AuctionDAOSQL.getInstance();
        Auction auction = auctionDAO.create(dummyAuction);
        return auction;
    }

    public static Good createDummyGood() throws DAOException {
        Auction dummyAuction = createDummyAuction();

        Good dummyGood = DummyGenerator.getDummyGood();
        dummyGood.auctionId = dummyAuction.id;

        GoodDAO goodDAO = GoodDAOSQL.getInstance();
        Good good = goodDAO.create(dummyGood);
        return good;
    }

    public static Bid createDummyBid() throws DAOException {
        Auction dummyAuction = createDummyAuction();

        GoodDAO goodDAO = GoodDAOSQL.getInstance();
        Good dummyGood = DummyGenerator.getDummyGood();
        dummyGood.auctionId = dummyAuction.id;
        Good insertedGood = goodDAO.create(dummyGood);

        Bid dummyBid = DummyGenerator.getDummyBid();
        dummyBid.auctionId = dummyAuction.id;
        dummyBid.ownerId = dummyAuction.ownerId;
        dummyBid.goodId = insertedGood.id;

        BidDAO bidDAO = BidDAOSQL.getInstance();
        Bid bid = bidDAO.create(dummyBid);
        return bid;
    }

    public static Bid createOtherDummyBid(int auctionId, int ownerId, int goodId) throws DAOException {
        Bid dummyBid = DummyGenerator.getOtherDummyBid();
        dummyBid.auctionId = auctionId;
        dummyBid.ownerId = ownerId;
        dummyBid.goodId = goodId;

        BidDAO bidDAO = BidDAOSQL.getInstance();
        Bid bid = bidDAO.create(dummyBid);
        return bid;
    }
}
