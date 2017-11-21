import main.java.dao.DAOException;
import main.java.dao.EventDAO;
import main.java.dao.NotFoundException;
import main.java.dao.UserDAO;
import main.java.dao.sql.EventDAOSQL;
import main.java.dao.sql.UserDAOSQL;
import main.java.models.Event;
import main.java.models.User;
import org.junit.Test;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import static org.junit.Assert.*;

public class EventDBTest extends AbstractDBTest {

    private static final String TEST_NEW_NAME = "TestNewName";

    @Test(expected = DAOException.class)
    public void test_insertion_of_event_with_missing_user_throws_DAO_exception() throws DAOException {
        EventDAO eventDAO = EventDAOSQL.getInstance();

        Event event = DummyGenerator.getDummyEvent();
        eventDAO.create(event);
    }

    @Test
    public void test_insertion_and_retrieval_of_event_into_db() throws DAOException {
        EventDAO eventDAO = EventDAOSQL.getInstance();
        UserDAO userDAO = UserDAOSQL.getInstance();

        User user = DummyGenerator.getDummyUser();

        User insertedUser = userDAO.create(user);

        Event event = DummyGenerator.getDummyEvent();
        event.ownerId = insertedUser.id;
        Event insertedEvent = eventDAO.create(event);

        Event retrievedEvent = eventDAO.getById(insertedEvent.id);

        assertEquals(insertedEvent, retrievedEvent);
        assertNotNull(insertedEvent.createdAt);
        assertNotNull(insertedEvent.updatedAt);
        assertEquals(insertedEvent.createdAt, insertedEvent.updatedAt);
    }

    @Test
    public void test_when_user_not_found_by_id() {
        throw new NotImplementedException();
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

    @Test
    public void test_full_event_update() {
        throw new NotImplementedException();
    }

    @Test
    public void test_update_in_non_existent_event() {
        throw new NotImplementedException();
    }

    @Test
    public void test_delete_existent_event()  {
        throw new NotImplementedException();
    }

    @Test
    public void test_delete_inexistent_event() {
        throw new NotImplementedException();
    }

}
