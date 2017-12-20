package main.java.dao.sql;

import main.java.EmbeddedPostgresWrapper;
import main.java.db.Source;
import main.java.utils.SQLFileExecutor;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.mockito.Mockito;

import javax.naming.NamingException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;

public abstract class AbstractDBTest {

    private static EmbeddedPostgresWrapper embeddedDb;

    @BeforeClass
    public static void changeDBConnection() throws IOException, SQLException {
        embeddedDb = EmbeddedPostgresWrapper.getInstance();
        embeddedDb.start();
        Source.getInstance().setConnection(embeddedDb.getConnection());
    }

    @Before
    public void createAllTables() throws IOException, SQLException, NamingException {
        SQLFileExecutor.executeSQLFile(Source.getInstance().getConnection(),
                new FileInputStream("db/v1.0.sql"));
        SQLFileExecutor.executeSQLFile(Source.getInstance().getConnection(),
                new FileInputStream("db/v1.1.sql"));
        SQLFileExecutor.executeSQLFile(Source.getInstance().getConnection(),
                new FileInputStream("db/v1.2.sql"));
    }

    @After
    public void deleteAllTables() throws FileNotFoundException, SQLException, NamingException {
        SQLFileExecutor.executeSQLFile(Source.getInstance().getConnection(),
                new FileInputStream("db/__reset__/reset-db.sql"));
    }

    @AfterClass
    public static void reestablishDBConnection() {
        Source.getInstance().setConnection(null);
    }
}
