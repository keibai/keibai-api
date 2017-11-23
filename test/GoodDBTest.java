import main.java.dao.*;
import main.java.dao.sql.AuctionDAOSQL;
import main.java.dao.sql.EventDAOSQL;
import main.java.dao.sql.GoodDAOSQL;
import main.java.dao.sql.UserDAOSQL;
import main.java.models.Auction;
import main.java.models.Event;
import main.java.models.Good;
import main.java.models.User;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class GoodDBTest extends AbstractDBTest {

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
        assertNull(good)+;
    }

    @Test
    public void test_good_update_name() {
        throw new UnsupportedOperationException("TODO: Implement this");
    }

    @Test
    public void test_full_good_update() {
        throw new UnsupportedOperationException("TODO: Implement this");
    }

    @Test
    public void test_update_in_non_existing_good() {
        throw new UnsupportedOperationException("TODO: Implement this");
    }

    @Test
    public void test_delete_existent_good() {
        throw new UnsupportedOperationException("TODO: Implement this");
    }

    @Test
    public void test_delete_inexistent_user() {
        throw new UnsupportedOperationException("TODO: Implement this");
    }
}
