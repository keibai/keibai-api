package main.java.dao.sql;

import main.java.dao.BidDAO;
import main.java.dao.DAOException;
import main.java.dao.UserDAO;
import main.java.models.Bid;
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
}