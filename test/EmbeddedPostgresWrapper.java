import ru.yandex.qatools.embed.postgresql.EmbeddedPostgres;

import java.io.IOException;
import java.net.ServerSocket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class EmbeddedPostgresWrapper {

    private EmbeddedPostgres embeddedPostgres;
    private String connectionUrl;

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
        return DriverManager.getConnection(this.connectionUrl);
    }

    private static int getFreePort() throws IOException {
        ServerSocket s = new ServerSocket(0);
        return s.getLocalPort();
    }
}
