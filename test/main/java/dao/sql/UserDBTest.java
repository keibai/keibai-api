package main.java.dao.sql;

import main.java.dao.DAOException;
import main.java.dao.NotFoundException;
import main.java.dao.UserDAO;
import main.java.dao.sql.AbstractDBTest;
import main.java.dao.sql.DummyGenerator;
import main.java.dao.sql.UserDAOSQL;
import main.java.models.User;
import org.junit.*;

import static org.junit.Assert.*;

public class UserDBTest extends AbstractDBTest {

    private static final String TEST_EMAIL = "TestEmail";

    private static final String TEST_NEW_NAME = "TestNewName";

    @Test
    public void test_user_is_inserted_and_retrieved_properly_by_id() throws DAOException, NotFoundException {
        UserDAO userDAO = UserDAOSQL.getInstance();
        User user = DummyGenerator.getDummyUser();
        User insertedUser = userDAO.create(user);

        User retrievedUser = userDAO.getById(insertedUser.id);

        assertEquals(insertedUser, retrievedUser);
        assertNotNull(retrievedUser.createdAt);
        assertNotNull(retrievedUser.updatedAt);
        assertEquals(retrievedUser.createdAt, retrievedUser.updatedAt);
    }

    @Test
    public void test_when_user_not_found_by_id() throws NotFoundException, DAOException {
        UserDAO userDAO = UserDAOSQL.getInstance();
        User user = userDAO.getById(24);
        assertNull(user);
    }

    @Test
    public void test_user_is_properly_retrieved_by_email() throws DAOException, NotFoundException {
        UserDAO userDAO = UserDAOSQL.getInstance();
        User user = DummyGenerator.getDummyUser();
        User insertedUser = userDAO.create(user);

        User retrievedUser = userDAO.getByEmail(insertedUser.email);

        assertEquals(insertedUser, retrievedUser);
        assertNotNull(retrievedUser.createdAt);
        assertNotNull(retrievedUser.updatedAt);
        assertEquals(retrievedUser.createdAt, retrievedUser.updatedAt);
    }

    @Test
    public void test_user_not_found_by_email() throws NotFoundException, DAOException {
        UserDAO userDAO = UserDAOSQL.getInstance();
        User user = userDAO.getByEmail(TEST_EMAIL);
        assertNull(user);
    }

    @Test
    public void test_user_update_name() throws DAOException, NotFoundException {
        UserDAO userDAO = UserDAOSQL.getInstance();

        User user = DummyGenerator.getDummyUser();
        User insertedUser = userDAO.create(user);

        insertedUser.name = TEST_NEW_NAME;
        User updatedUser = userDAO.update(insertedUser);

        assertNotNull(updatedUser);

        User retrievedUser = userDAO.getById(updatedUser.id);

        assertEquals(updatedUser, retrievedUser);
        assertNotEquals(retrievedUser.createdAt, retrievedUser.updatedAt);
    }

    @Test
    public void test_full_user_update() throws NotFoundException, DAOException {
        UserDAO userDAO = UserDAOSQL.getInstance();

        User user = DummyGenerator.getDummyUser();
        User insertedUser = userDAO.create(user);

        User user2 = DummyGenerator.getOtherDummyUser();
        user2.id = insertedUser.id;
        user2.createdAt = insertedUser.createdAt;
        user2.updatedAt = insertedUser.updatedAt;
        User updatedUser = userDAO.update(user2);

        User retrievedUser = userDAO.getById(updatedUser.id);

        assertEquals(updatedUser, retrievedUser);
        assertNotEquals(insertedUser, retrievedUser);
        assertNotEquals(retrievedUser.createdAt, retrievedUser.updatedAt);
        assertNotEquals(insertedUser.updatedAt, retrievedUser.updatedAt);
    }

    @Test
    public void test_update_in_non_existing_user() throws DAOException {
        UserDAO userDAO = UserDAOSQL.getInstance();

        User updatedUser = DummyGenerator.getDummyUser();
        updatedUser.id = 1;
        User modifiedUser = userDAO.update(updatedUser);
        assertNull(modifiedUser);
    }

    @Test
    public void test_delete_existent_user() throws DAOException {
        UserDAO userDAO = UserDAOSQL.getInstance();

        User user = DummyGenerator.getDummyUser();
        User insertedUser = userDAO.create(user);

        boolean deleted = userDAO.delete(insertedUser.id);
        assertTrue(deleted);
        
        assertNull(userDAO.getById(1));
    }

    @Test
    public void test_delete_inexistent_user() throws DAOException {
        UserDAO userDAO = UserDAOSQL.getInstance();
        boolean deleted = userDAO.delete(24);
        assertFalse(deleted);
    }
}
