package main.java.utils;

import main.java.dao.*;
import main.java.dao.sql.AuctionDAOSQL;
import main.java.dao.sql.EventDAOSQL;
import main.java.dao.sql.GoodDAOSQL;
import main.java.dao.sql.UserDAOSQL;
import main.java.models.Auction;
import main.java.models.Event;
import main.java.models.Good;
import main.java.models.User;


public class DBFeeder {

    public static User createDummyUser() throws DAOException {
        User dummyUser = DummyGenerator.getDummyUser();
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

    public static Good createDummyGood() throws DAOException {
        Auction dummyAuction = createDummyAuction();

        Good dummyGood = DummyGenerator.getDummyGood();
        dummyGood.auctionId = dummyAuction.id;

        GoodDAO goodDAO = GoodDAOSQL.getInstance();
        Good good = goodDAO.create(dummyGood);
        return good;
    }
}
