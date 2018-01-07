package main.java.servlets.user;

import com.google.gson.Gson;
import main.java.dao.DAOException;
import main.java.dao.UserDAO;
import main.java.dao.sql.UserDAOSQL;
import main.java.models.User;
import main.java.utils.DefaultHttpSession;
import main.java.utils.HttpResponse;
import main.java.utils.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "UserWhoami", urlPatterns = {"/users/whoami" })
public class UserWhoami extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpResponse httpResponse = new HttpResponse(response);
        UserDAO userDAO = UserDAOSQL.getInstance();
        DefaultHttpSession session = new DefaultHttpSession(request);

        if (session.userId() == -1) {
            new HttpResponse(response).empty();
            return;
        }

        User dbUser;
        try {
            dbUser = userDAO.getById(session.userId());
        } catch(DAOException e) {
            Logger.error("Retrieve user", String.valueOf(session.userId()), e.toString());
            return;
        }

        dbUser.password = null;
        httpResponse.response(new Gson().toJson(dbUser));
    }
}
