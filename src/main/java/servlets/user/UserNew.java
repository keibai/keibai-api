package main.java.servlets.user;

import main.java.dao.DAOException;
import main.java.dao.NotFoundException;
import main.java.dao.UserDAO;
import main.java.dao.sql.UserDAOSQL;
import main.java.models.User;
import main.java.utils.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(name = "UserNew", urlPatterns = {"/users/new"})
public class UserNew extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
//            User user = new User() {{
//                setName("Erik");
//                setLastName("Green");
//                setPassword("1234");
//                setEmail("hi" + Math.random() + "@example.com");
//            }};
            UserDAO userDAO = UserDAOSQL.getInstance();
            User user = userDAO.getUserById(1);
            System.out.println(user);
        } catch (DAOException|NotFoundException e) {
            Logger.error(e.toString());
            String json = "{ \"error\": \"Internal server error.\" }";
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            PrintWriter out = response.getWriter();
            out.print(json);
            out.flush();
            return;
        }

        String json = "{ \"msg\": \"OK\"}";
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        out.print(json);
        out.flush();
    }
}