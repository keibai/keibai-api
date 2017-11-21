package main.java.servlets.user;

import main.java.dao.DAOException;
import main.java.dao.UserDAO;
import main.java.dao.sql.UserDAOSQL;
import main.java.models.User;
import main.java.utils.HttpSession;
import main.java.utils.JsonResponse;
import main.java.utils.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "EventNew", urlPatterns = {"/events/new"})
public class EventNew extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = new HttpSession(request);
        UserDAO userDAO = UserDAOSQL.getInstance();

        int userId = session.userId();
        if (userId == -1) {
            new JsonResponse(response).unauthorized();
            return;
        }

        User user;
        try {
            user = userDAO.getById(session.userId());
        } catch(DAOException e) {
            Logger.error("Retrieve user by ID", String.valueOf(userId), e.toString());
            new JsonResponse(response).internalServerError();
            return;
        }

        new JsonResponse(response).msg(user.email);
    }
}
