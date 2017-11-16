package main.java.utils;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

public class SQLFileExecutor {

    public static void executeSQLFile(Connection conn, InputStream in) throws SQLException {
        Scanner s = new Scanner(in);
        s.useDelimiter("(;(\\r)?\\n)|((\\r)?\\n)?(--)?.*(--(\\r)?\\n)");
        Statement st = null;

        try {
            StringBuilder query = new StringBuilder();
            st = conn.createStatement();
            while (s.hasNext()) {
                String line = s.next();
                if (!line.startsWith("--") && line.trim().length() > 0) {
                    if (!line.endsWith(";")) {
                        line += ";\n";
                    }
                    query.append(line);
                }
            }
            st.execute(query.toString());
        } finally {
            if (st != null) {
                st.close();
            }
        }
    }
}
