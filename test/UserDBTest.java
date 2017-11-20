import main.java.dao.DAOException;
import main.java.dao.NotFoundException;
import main.java.dao.UserDAO;
import main.java.dao.sql.UserDAOSQL;
import main.java.models.User;
import org.junit.*;

import static org.junit.Assert.*;

public class UserDBTest extends AbstractDBTest {

    private static final String TEST_NAME = "TestName";
    private static final String TEST_LAST_NAME = "TestLastName";
    private static final String TEST_EMAIL = "TestEmail";
    private static final String TEST_PASSWORD = "TestPassword";

    private static final String TEST_NEW_NAME = "TestNewName";
    private static final String TEST_NEW_LAST_NAME = "TestNewLastName";
    private static final String TEST_NEW_EMAIL = "TestNewEmail";
    private static final String TEST_NEW_PASSWORD = "TestNewPassword";

    @Test
    public void test_user_is_inserted_and_retrieved_properly_by_id() throws DAOException, NotFoundException {
        UserDAO userDAO = UserDAOSQL.getInstance();
        User insertedUser = new User();
        insertedUser.name = TEST_NAME;
        insertedUser.lastName = TEST_LAST_NAME;
        insertedUser.email = TEST_EMAIL;
        insertedUser.password = TEST_PASSWORD;
        userDAO.create(insertedUser);

        User retrievedUser;
        retrievedUser = userDAO.getById(1);

        assertNotEquals(0, retrievedUser.id);
        assertEquals(insertedUser.name, retrievedUser.name);
        assertEquals(insertedUser.lastName, retrievedUser.lastName);
        assertEquals(insertedUser.email, retrievedUser.email);
        assertEquals(insertedUser.password, retrievedUser.password);
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
        User insertedUser = new User();
        insertedUser.name = TEST_NAME;
        insertedUser.lastName = TEST_LAST_NAME;
        insertedUser.email = TEST_EMAIL;
        insertedUser.password = TEST_PASSWORD;
        userDAO.create(insertedUser);

        User retrievedUser;
        retrievedUser = userDAO.getByEmail(TEST_EMAIL);

        assertNotEquals(0, retrievedUser.id);
        assertEquals(insertedUser.name, retrievedUser.name);
        assertEquals(insertedUser.lastName, retrievedUser.lastName);
        assertEquals(insertedUser.email, retrievedUser.email);
        assertEquals(insertedUser.password, retrievedUser.password);
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

        User insertedUser = new User();
        insertedUser.name = TEST_NAME;
        insertedUser.lastName = TEST_LAST_NAME;
        insertedUser.email = TEST_EMAIL;
        insertedUser.password = TEST_PASSWORD;
        userDAO.create(insertedUser);

        User updatedUser = new User();
        updatedUser.id = 1;
        updatedUser.name = TEST_NEW_NAME;
        updatedUser.lastName = TEST_LAST_NAME;
        updatedUser.email = TEST_EMAIL;
        updatedUser.password = TEST_PASSWORD;
        userDAO.update(updatedUser);

        User retrievedUser = userDAO.getByEmail(TEST_EMAIL);

        assertEquals(updatedUser.name, retrievedUser.name);
        assertEquals(updatedUser.lastName, retrievedUser.lastName);
        assertEquals(updatedUser.email, retrievedUser.email);
        assertEquals(updatedUser.password, retrievedUser.password);
        assertEquals(updatedUser.credit, 0.0, 0.00001);
        assertEquals(retrievedUser.credit, 0.0, 0.00001);
        assertNotEquals(retrievedUser.createdAt, retrievedUser.updatedAt);
    }

    @Test
    public void test_full_user_update() throws NotFoundException, DAOException {
        UserDAO userDAO = UserDAOSQL.getInstance();

        User insertedUser = new User();
        insertedUser.name = TEST_NAME;
        insertedUser.lastName = TEST_LAST_NAME;
        insertedUser.email = TEST_EMAIL;
        insertedUser.password = TEST_PASSWORD;
        userDAO.create(insertedUser);

        User updatedUser = new User();
        updatedUser.id = 1;
        updatedUser.name = TEST_NEW_NAME;
        updatedUser.lastName = TEST_NEW_LAST_NAME;
        updatedUser.email = TEST_NEW_EMAIL;
        updatedUser.password = TEST_NEW_PASSWORD;
        updatedUser.credit = 100.0;
        userDAO.update(updatedUser);

        User retrievedUser = userDAO.getById(1);

        assertEquals(updatedUser.id, retrievedUser.id);
        assertEquals(updatedUser.name, retrievedUser.name);
        assertEquals(updatedUser.lastName, retrievedUser.lastName);
        assertEquals(updatedUser.email, retrievedUser.email);
        assertEquals(updatedUser.password, retrievedUser.password);
        assertEquals(updatedUser.credit, retrievedUser.credit, 0.0000000001);
        assertNotEquals(retrievedUser.createdAt, retrievedUser.updatedAt);
        assertNotEquals(updatedUser.updatedAt, retrievedUser.updatedAt);
    }

    @Test
    public void test_update_in_non_existing_user() throws DAOException {
        UserDAO userDAO = UserDAOSQL.getInstance();

        User updatedUser = new User();
        updatedUser.id = 1;
        updatedUser.name = TEST_NEW_NAME;
        updatedUser.lastName = TEST_LAST_NAME;
        updatedUser.email = TEST_EMAIL;
        updatedUser.password = TEST_PASSWORD;
        User modifiedUser = userDAO.update(updatedUser);
        assertNull(modifiedUser);
    }

    @Test
    public void test_delete_existent_user() throws DAOException {
        UserDAO userDAO = UserDAOSQL.getInstance();

        User insertedUser = new User();
        insertedUser.name = TEST_NAME;
        insertedUser.lastName = TEST_LAST_NAME;
        insertedUser.email = TEST_EMAIL;
        insertedUser.password = TEST_PASSWORD;
        userDAO.create(insertedUser);

        boolean deleted = userDAO.delete(1);
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
