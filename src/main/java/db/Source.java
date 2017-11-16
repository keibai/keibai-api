package main.java.db;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Source singleton.
 * - DB connection.
 */
public class Source {
    private static Source instance;

    private DataSource dataSource;
    private Connection connection;

    public static Source getInstance() {
        if (instance == null) {
            instance = new Source();
        }
        return instance;
    }

    private Source() {}

    private DataSource getDataSource() throws NamingException {
        if (dataSource == null) {
            dataSource = (DataSource) new InitialContext().lookup("java:/comp/env/jdbc/db");
        }
        return dataSource;
    }

    public Connection getConnection() throws NamingException, SQLException {
        if (connection == null) {
            connection = getDataSource().getConnection();
        }
        return connection;
    }

    // For DB integration tests
    public void setConnection(Connection connection) {
        this.connection = connection;
    }
}
