package main.java.dao.sql;

import main.java.dao.DAOException;
import main.java.dao.EventDAO;
import main.java.dao.NotFoundException;
import main.java.dao.UserDAO;
import main.java.utils.DummyGenerator;
import main.java.models.Event;
import main.java.models.User;
import org.junit.Test;

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
    public void test_when_user_not_found_by_id() throws DAOException {
        EventDAO eventDAO = EventDAOSQL.getInstance();
        Event event = eventDAO.getById(24);
        assertNull(event);
    }


    @Test
    public void test_event_update_name() throws DAOException, NotFoundException {
        EventDAO eventDAO = EventDAOSQL.getInstance();
        UserDAO userDAO = UserDAOSQL.getInstance();

        User owner = DummyGenerator.getDummyUser();
        User insertedOwner = userDAO.create(owner);

        Event event = DummyGenerator.getDummyEvent();
        event.ownerId = insertedOwner.id;
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
    public void test_full_event_update() throws DAOException {
        EventDAO eventDAO = EventDAOSQL.getInstance();
        User user = DummyGenerator.getDummyUser();
        UserDAO userDAO = UserDAOSQL.getInstance();
        User insertUser = userDAO.create(user);
        Event event = DummyGenerator.getDummyEvent();
        event.ownerId = insertUser.id;
        Event insertedEvent = eventDAO.create(event);

        Event event2 = DummyGenerator.getOtherDummyEvent();
        event2.id = insertedEvent.id;
        event2.createdAt = insertedEvent.createdAt;
        event2.updatedAt = insertedEvent.updatedAt;
        event2.ownerId = insertedEvent.ownerId;
        Event updatedEvent = eventDAO.update(event2);

        Event retrievedEvent = eventDAO.getById(updatedEvent.id);

        assertEquals(updatedEvent, retrievedEvent);
        assertNotEquals(insertedEvent, retrievedEvent);
        assertNotEquals(retrievedEvent.createdAt, retrievedEvent.updatedAt);
        assertNotEquals(insertedEvent.updatedAt, retrievedEvent.updatedAt);
    }

    @Test
    public void test_update_in_non_existent_event() throws DAOException {
        EventDAO eventDAO = EventDAOSQL.getInstance();

        Event updatedEvent = DummyGenerator.getDummyEvent();
        updatedEvent.id = 1;
        Event modifiedEvent = eventDAO.update(updatedEvent);
        assertNull(modifiedEvent);
    }

    @Test
    public void test_delete_existent_event() throws DAOException {
        EventDAO eventDAO = EventDAOSQL.getInstance();
        User user = DummyGenerator.getDummyUser();
        UserDAO userDAO = UserDAOSQL.getInstance();
        User insertedUser = userDAO.create(user);

        Event event = DummyGenerator.getDummyEvent();
        event.ownerId = insertedUser.id;
        Event insertedEvent = eventDAO.create(event);
        boolean deleted = eventDAO.delete(insertedEvent.id);
        assertTrue(deleted);

        assertNull(eventDAO.getById(1));
    }

    @Test
    public void test_delete_inexistent_event() throws DAOException {
        EventDAO eventDAO = EventDAOSQL.getInstance();
        boolean deleted = eventDAO.delete(24);
        assertFalse(deleted);
    }

}
