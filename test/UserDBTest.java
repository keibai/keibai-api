import main.java.dao.DAOException;
import main.java.dao.NotFoundException;
import main.java.dao.UserDAO;
import main.java.dao.sql.UserDAOSQL;
import main.java.db.Source;
import main.java.models.User;
import main.java.utils.SQLFileExecutor;
import org.junit.*;

import javax.naming.NamingException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

public class UserDBTest {

    private static final String TEST_NAME = "TestName";
    private static final String TEST_LAST_NAME = "TestLastName";
    private static final String TEST_EMAIL = "TestEmail";
    private static final String TEST_PASSWORD = "TestPassword";

    private static final String TEST_NEW_NAME = "TestNewName";
    private static final String TEST_NEW_LAST_NAME = "TestNewLastName";
    private static final String TEST_NEW_EMAIL = "TestNewEmail";
    private static final String TEST_NEW_PASSWORD = "TestNewPassword";

    private static EmbeddedPostgresWrapper embeddedDb;

    @BeforeClass
    public static void changeDBConnection() throws IOException, SQLException {
        embeddedDb = new EmbeddedPostgresWrapper();
        embeddedDb.start();
        Source.getInstance().setConnection(embeddedDb.getConnection());
    }

    @Before
    public void createAllTables() throws IOException, SQLException, NamingException {
        SQLFileExecutor.executeSQLFile(Source.getInstance().getConnection(),
                new FileInputStream("db/v1.0.sql"));
    }

    @Test
    public void test_user_is_inserted_and_retrieved_properly_by_id() throws DAOException, NotFoundException {
        UserDAO userDAO = UserDAOSQL.getInstance();
        User insertedUser = new User();
        insertedUser.setName(TEST_NAME);
        insertedUser.setLastName(TEST_LAST_NAME);
        insertedUser.setEmail(TEST_EMAIL);
        insertedUser.setPassword(TEST_PASSWORD);
        userDAO.createUser(insertedUser);

        User retrievedUser;
        retrievedUser = userDAO.getUserById(1);

        assertEquals(retrievedUser.getId(), 1);
        assertEquals(retrievedUser.getName(), insertedUser.getName());
        assertEquals(retrievedUser.getLastName(), insertedUser.getLastName());
        assertEquals(retrievedUser.getEmail(), insertedUser.getEmail());
        assertEquals(retrievedUser.getPassword(), insertedUser.getPassword());
        assertEquals(retrievedUser.getCredit(), 0.0, 0.00001);
        assertEquals(insertedUser.getCredit(), 0.0, 0.00001);
        assertNotNull(retrievedUser.getCreatedAt());
        assertNotNull(retrievedUser.getUpdatedAt());
        assertEquals(retrievedUser.getCreatedAt(), retrievedUser.getUpdatedAt());
    }

    @Test(expected = NotFoundException.class)
    public void test_throws_not_found_exception_when_user_not_found_by_id() throws NotFoundException, DAOException {
        UserDAO userDAO = UserDAOSQL.getInstance();
        userDAO.getUserById(24);
    }

    @Test
    public void test_user_is_properly_retrieved_by_email() throws DAOException, NotFoundException {
        UserDAO userDAO = UserDAOSQL.getInstance();
        User insertedUser = new User();
        insertedUser.setName(TEST_NAME);
        insertedUser.setLastName(TEST_LAST_NAME);
        insertedUser.setEmail(TEST_EMAIL);
        insertedUser.setPassword(TEST_PASSWORD);
        userDAO.createUser(insertedUser);

        User retrievedUser;
        retrievedUser = userDAO.getUserByEmail(TEST_EMAIL);

        assertEquals(retrievedUser.getName(), insertedUser.getName());
        assertEquals(retrievedUser.getLastName(), insertedUser.getLastName());
        assertEquals(retrievedUser.getEmail(), insertedUser.getEmail());
        assertEquals(retrievedUser.getPassword(), insertedUser.getPassword());
        assertEquals(retrievedUser.getCredit(), 0.0, 0.00001);
        assertEquals(insertedUser.getCredit(), 0.0, 0.00001);
        assertNotNull(retrievedUser.getCreatedAt());
        assertNotNull(retrievedUser.getUpdatedAt());
    }

    @Test(expected = NotFoundException.class)
    public void test_throws_not_found_exception_when_user_not_found_by_email() throws NotFoundException, DAOException {
        UserDAO userDAO = UserDAOSQL.getInstance();
        userDAO.getUserByEmail(TEST_EMAIL);
    }

    @Test
    public void test_user_update_name() throws DAOException, NotFoundException {
        UserDAO userDAO = UserDAOSQL.getInstance();

        User insertedUser = new User();
        insertedUser.setName(TEST_NAME);
        insertedUser.setLastName(TEST_LAST_NAME);
        insertedUser.setEmail(TEST_EMAIL);
        insertedUser.setPassword(TEST_PASSWORD);
        userDAO.createUser(insertedUser);

        User updatedUser = new User();
        updatedUser.setId(1);
        updatedUser.setName(TEST_NEW_NAME);
        updatedUser.setLastName(TEST_LAST_NAME);
        updatedUser.setEmail(TEST_EMAIL);
        updatedUser.setPassword(TEST_PASSWORD);
        userDAO.updateUser(updatedUser);

        User retrievedUser = userDAO.getUserByEmail(TEST_EMAIL);

        assertEquals(updatedUser.getName(), retrievedUser.getName());
        assertEquals(updatedUser.getLastName(), retrievedUser.getLastName());
        assertEquals(updatedUser.getEmail(), retrievedUser.getEmail());
        assertEquals(updatedUser.getPassword(), retrievedUser.getPassword());
        assertEquals(updatedUser.getCredit(), 0.0, 0.00001);
        assertEquals(retrievedUser.getCredit(), 0.0, 0.00001);
        assertNotEquals(retrievedUser.getCreatedAt(), retrievedUser.getUpdatedAt());
    }

    @Test
    public void test_full_user_update() throws NotFoundException, DAOException {
        UserDAO userDAO = UserDAOSQL.getInstance();

        User insertedUser = new User();
        insertedUser.setName(TEST_NAME);
        insertedUser.setLastName(TEST_LAST_NAME);
        insertedUser.setEmail(TEST_EMAIL);
        insertedUser.setPassword(TEST_PASSWORD);
        userDAO.createUser(insertedUser);

        User updatedUser = new User();
        updatedUser.setId(1);
        updatedUser.setName(TEST_NEW_NAME);
        updatedUser.setLastName(TEST_NEW_LAST_NAME);
        updatedUser.setEmail(TEST_NEW_EMAIL);
        updatedUser.setPassword(TEST_NEW_PASSWORD);
        updatedUser.setCredit(100.0);
        userDAO.updateUser(updatedUser);

        User retrievedUser = userDAO.getUserById(1);

        assertEquals(updatedUser.getId(), retrievedUser.getId());
        assertEquals(updatedUser.getName(), retrievedUser.getName());
        assertEquals(updatedUser.getLastName(), retrievedUser.getLastName());
        assertEquals(updatedUser.getEmail(), retrievedUser.getEmail());
        assertEquals(updatedUser.getPassword(), retrievedUser.getPassword());
        assertEquals(updatedUser.getCredit(), retrievedUser.getCredit(), 0.0000000001);
        assertNotEquals(retrievedUser.getCreatedAt(), retrievedUser.getUpdatedAt());
        assertNotEquals(updatedUser.getUpdatedAt(), retrievedUser.getUpdatedAt());
    }

    @Test(expected = NotFoundException.class)
    public void test_update_in_non_existing_user_throws_not_found_exception() throws NotFoundException, DAOException {
        UserDAO userDAO = UserDAOSQL.getInstance();

        User updatedUser = new User();
        updatedUser.setId(1);
        updatedUser.setName(TEST_NEW_NAME);
        updatedUser.setLastName(TEST_LAST_NAME);
        updatedUser.setEmail(TEST_EMAIL);
        updatedUser.setPassword(TEST_PASSWORD);
        userDAO.updateUser(updatedUser);
    }

    @After
    public void deleteAllTables() throws FileNotFoundException, SQLException, NamingException {
        SQLFileExecutor.executeSQLFile(Source.getInstance().getConnection(),
                new FileInputStream("db/__reset__/reset-db.sql"));
    }

    @AfterClass
    public static void reestablishDBConnection() {
        embeddedDb.stop();
        Source.getInstance().setConnection(null);
    }
}
