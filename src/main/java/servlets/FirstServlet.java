package main.java.servlets;

import main.java.db.Source;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(name = "FirstServlet", urlPatterns = {"/hello"})
public class FirstServlet extends HttpServlet {
//    @Resource() private DataSource dataSource;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String msg = "";
        try {
            Source source = new Source();
            source.getConnection();
            msg = "OK";
        } catch (Exception e) {
            System.out.println("Catch me if you can!");
            System.out.println(e);
            msg = e.toString();
        }

        String json = "{ hello: \"" + msg + "\"}";
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        out.print(json);
        out.flush();
    }
}