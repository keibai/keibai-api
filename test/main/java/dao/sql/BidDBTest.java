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

    @Test(expected = DAOException.class)
    public void test_insertion_of_bid_without_owner_throws_DAOException() {
        throw new UnsupportedOperationException("TODO: Implement this");
    }

    @Test
    public void test_insertion_and_retrieval_of_bid() {
        throw new UnsupportedOperationException("TODO: Implement this");
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