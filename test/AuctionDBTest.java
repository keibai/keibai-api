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

        User owner = new User();
        owner.name = TEST_OWNER_NAME;
        owner.lastName = TEST_OWNER_LAST_NAME;
        owner.password = TEST_OWNER_PASSWORD;
        owner.email = TEST_OWNER_EMAIL;
        owner.credit = TEST_OWNER_CREDIT;
        userDAO.createUser(owner);
        owner.id = 1;

        User winner = new User();
        winner.name = TEST_WINNER_NAME;
        winner.lastName = TEST_WINNER_LAST_NAME;
        winner.password = TEST_WINNER_PASSWORD;
        winner.email = TEST_WINNER_EMAIL;
        winner.credit = TEST_WINNER_CREDIT;
        userDAO.createUser(winner);
        winner.id = 1;

        Event event = new Event();
        event.name = TEST_EVENT_NAME;
        event.auctionTime = TEST_AUCTION_TIME;
        event.location = TEST_EVENT_LOCATION;
        event.auctionType = TEST_AUCTION_TYPE;
        event.category = TEST_EVENT_CATEGORY;
        eventDAO.createEvent(event);
        event.id = 1;

        Auction insertedAuction = new Auction();
        insertedAuction.name = TEST_NAME;
        insertedAuction.startingPrice = TEST_STARTING_PRICE;
        insertedAuction.startTime = TEST_START_TIME;
        insertedAuction.status = TEST_STATUS;
        insertedAuction.isValid = TEST_IS_VALID;
        insertedAuction.ownerId = 1;
        insertedAuction.winnerId = 2;
        insertedAuction.eventId = 4;
        auctionDAO.createAuction(insertedAuction);
        insertedAuction.id = 1;

        Auction retrievedAuction = auctionDAO.getAuctionById(1);

        assertEquals(insertedAuction, retrievedAuction);
    }

    @Test(expected = NotFoundException.class)
    public void test_get_inexistent_auction_throws_not_found_exception() throws NotFoundException, DAOException {
        AuctionDAO auctionDAO = AuctionDAOSQL.getInstance();
        auctionDAO.getAuctionById(34);
    }
}
