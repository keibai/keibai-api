package main.java.servlets.user;

import main.java.dao.UserDAO;
import main.java.dao.sql.UserDAOSQL;
import main.java.models.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;

@WebServlet(name = "UserNew", urlPatterns = {"/users/new"})
public class UserNew extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String msg;
        try {
            User user = new User();
            user.setName("Gerard");
            user.setLastName("Rovira");
            user.setPassword("1234");
            user.setEmail("example@example.example");
            user.setCountry("Spain");
            user.setCity("Lleida");
            user.setAddress("Fake address");
            user.setZipCode("1234556");
            user.setCredit(25.0);
            user.setCreatedAt(Calendar.getInstance());
            user.setUpdatedAt(Calendar.getInstance());
            UserDAO userDAO = UserDAOSQL.getInstance();
            userDAO.createUser(user);
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