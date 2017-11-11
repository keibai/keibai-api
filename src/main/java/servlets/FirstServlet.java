package main.java.servlets;

import main.java.db.Source;
import org.postgresql.jdbc2.optional.ConnectionPool;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

@WebServlet(name = "FirstServlet", urlPatterns = {"/"})
public class FirstServlet extends HttpServlet {
//    @Resource() private DataSource dataSource;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String msg = "";
        try {
            Source source = new Source();
            Connection connection = source.getConnection();
            String sql = "CREATE TABLE IF NOT EXISTS abc (\n"
                    + "	id integer PRIMARY KEY,\n"
                    + "	name text NOT NULL,\n"
                    + "	capacity real\n"
                    + ");";


            Statement stmt = connection.createStatement();
            stmt.execute(sql);
            msg = "OK";
        } catch (Exception e) {
            throw new IllegalStateException(e);
//            System.out.println("Catch me if you can!");
//            System.out.println(e);
//            msg = e.toString();
        }

        String json = "{ \"hello\": \"" + msg + "\"}";
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        out.print(json);
        out.flush();
    }
}