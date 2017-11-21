import main.java.dao.DAOException;
import main.java.dao.EventDAO;
import main.java.dao.NotFoundException;
import main.java.dao.UserDAO;
import main.java.dao.sql.EventDAOSQL;
import main.java.dao.sql.UserDAOSQL;
import main.java.models.Event;
import main.java.models.User;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

public class EventDBTest extends AbstractDBTest {

    private static final String TEST_NAME = "TestName";
    private static final String TEST_NEW_NAME = "TestNewName";
    private static final int TEST_AUCTION_TIME = 100;
    private static final String TEST_LOCATION = "TestLocation";
    private static final String TEST_AUCTION_TYPE = "TestAuctionType";
    private static final String TEST_CATEGORY = "TestCategory";

    private static final String TEST_USER_NAME = "TestName";
    private static final String TEST_USER_LAST_NAME = "TestLastName";
    private static final String TEST_USER_EMAIL = "TestEmail";
    private static final String TEST_USER_PASSWORD = "TestPassword";


    @Test(expected = DAOException.class)
    public void test_insertion_of_event_with_missing_user_throws_DAO_exception() throws DAOException {
        EventDAO eventDAO = EventDAOSQL.getInstance();

        Event event = new Event();
        event.name = TEST_NAME;
        event.auctionTime = TEST_AUCTION_TIME;
        event.location = TEST_LOCATION;
        event.auctionType = TEST_AUCTION_TYPE;
        event.category = TEST_CATEGORY;
        event.ownerId = 1;
        eventDAO.create(event);
    }

    @Test
    public void test_insertion_of_event_into_db() throws DAOException {
        EventDAO eventDAO = EventDAOSQL.getInstance();
        UserDAO userDAO = UserDAOSQL.getInstance();

        User user = new User();
        user.name =TEST_USER_NAME;
        user.lastName = TEST_USER_LAST_NAME;
        user.email = TEST_USER_EMAIL;
        user.password = TEST_USER_PASSWORD;

        userDAO.create(user);

        // TODO: createUser should return the created user
        user.id = 1;

        Event event = new Event();
        event.name = TEST_NAME;
        event.auctionTime = TEST_AUCTION_TIME;
        event.location = TEST_LOCATION;
        event.auctionType = TEST_AUCTION_TYPE;
        event.category = TEST_CATEGORY;
        event.ownerId = 1;
        eventDAO.create(event);
    }
    @Test
    public void test_event_update_name() throws DAOException, NotFoundException {
        EventDAO eventDAO = EventDAOSQL.getInstance();
        UserDAO userDAO = UserDAOSQL.getInstance();

        User owner = DummyGenerator.getDummyUser();
        User insertedOwner = userDAO.create(owner);

        Event event = DummyGenerator.getDummyEvent();
        Event insertedEvent = eventDAO.create(event);

        insertedEvent.name = TEST_NEW_NAME;
        Event updatedEvent = eventDAO.update(insertedEvent);

        Event retrievedEvent = eventDAO.getById(insertedEvent.id);

        assertEquals(updatedEvent, retrievedEvent);
        assertNotNull(retrievedEvent.updatedAt);
        assertNotNull(retrievedEvent.createdAt);
        assertNotEquals(retrievedEvent.updatedAt, retrievedEvent.createdAt);

    }
}
