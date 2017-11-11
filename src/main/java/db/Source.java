package main.java.db;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class Source {
    private DataSource dataSource;

    public Source() {
        try {
            InitialContext cxt = new InitialContext();
            if ( cxt == null ) {
                throw new Exception("Uh oh -- no context!");
            }
            dataSource = (DataSource) new InitialContext().lookup("java:/comp/env/jdbc/db");
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public Connection getConnection() {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }
}
