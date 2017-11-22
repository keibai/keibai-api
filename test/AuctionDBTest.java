import main.java.dao.*;
import main.java.dao.sql.AuctionDAOSQL;
import main.java.dao.sql.EventDAOSQL;
import main.java.dao.sql.UserDAOSQL;
import main.java.models.Auction;
import main.java.models.Event;
import main.java.models.User;
import org.junit.Test;

import java.sql.Timestamp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class AuctionDBTest extends AbstractDBTest {

    private static final String TEST_NAME = "TestName";
    private static final double TEST_STARTING_PRICE = 1.0;
    private static final Timestamp TEST_START_TIME = new Timestamp(System.currentTimeMillis());
    private static final String TEST_STATUS = "TestStatus";
    private static final boolean TEST_IS_VALID = false;

    private static final String TEST_OWNER_NAME = "TestOwnerName";
    private static final String TEST_OWNER_LAST_NAME = "TestOwnerLastName";
    private static final String TEST_OWNER_PASSWORD = "TestOwnerPassword";
    private static final String TEST_OWNER_EMAIL = "TestOwnerEmail";
    private static final double TEST_OWNER_CREDIT = 100.0;

    private static final String TEST_WINNER_NAME = "TestWinnerName";
    private static final String TEST_WINNER_LAST_NAME = "TestWinnerLastName";
    private static final String TEST_WINNER_PASSWORD = "TestWinnerPassword";
    private static final String TEST_WINNER_EMAIL = "TestWinnerEmail";
    private static final double TEST_WINNER_CREDIT = 1.0;

    private static final String TEST_EVENT_NAME = "TestEventName";
    private static final int TEST_AUCTION_TIME = 100;
    private static final String TEST_EVENT_LOCATION = "TestEventLocation";
    private static final String TEST_EVENT_CATEGORY = "TestEventCategory";
    private static final String TEST_AUCTION_TYPE = "TestAuctionType";

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
        auction.ownerId = 3000;
        Auction insertedAuction = auctionDAO.create(auction);
    }
}
