package main.java;

import ru.yandex.qatools.embed.postgresql.EmbeddedPostgres;

import java.io.IOException;
import java.net.ServerSocket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class EmbeddedPostgresWrapper {

    private static EmbeddedPostgresWrapper instance;

    private EmbeddedPostgres embeddedPostgres;
    private String connectionUrl;
    private Connection connection;

    private EmbeddedPostgresWrapper() {

    }

    public static EmbeddedPostgresWrapper getInstance() {
        if (instance == null) {
            instance = new EmbeddedPostgresWrapper();
        }
        return instance;
    }

    public void start() throws IOException {
        if (embeddedPostgres == null) {
            int port = getFreePort();
            embeddedPostgres = new EmbeddedPostgres();
            connectionUrl = embeddedPostgres.start("localhost", port, "keibai", "admin", "keibai");
        }
    }

    public void stop() {
        if (embeddedPostgres != null) {
            embeddedPostgres.stop();
            embeddedPostgres = null;
        }
    }

    public String getConnectionUrl() {
        return connectionUrl;
    }

    public Connection getConnection() throws SQLException {
        if (connection == null) {
            connection = DriverManager.getConnection(this.connectionUrl);
        }
        return connection;
    }

    private static int getFreePort() throws IOException {
        ServerSocket s = new ServerSocket(0);
        return s.getLocalPort();
    }
}
