package main.java.servlets.user;

import com.google.gson.Gson;
import main.java.dao.DAOException;
import main.java.dao.UserDAO;
import main.java.dao.sql.UserDAOSQL;
import main.java.models.User;
import main.java.utils.JsonResponse;
import main.java.utils.Logger;
import main.java.utils.Validator;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "UserSearch", urlPatterns = {"/users/search"})
public class UserSearch extends HttpServlet {

    public static final String ID_NONE = "No ID parameter was sent.";
    public static final String ID_INVALID = "Invalid ID parameter.";
    public static final String USER_NOT_FOUND = "User does not exist.";

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        JsonResponse jsonResponse = new JsonResponse(response);
        UserDAO userDAO = UserDAOSQL.getInstance();

        // Retrieve & validate params
        String unsafeIdParam = request.getParameter("id");
        if (unsafeIdParam == null) {
            jsonResponse.error(ID_NONE);
            return;
        }
        if (!Validator.isNumber(unsafeIdParam)) {
            jsonResponse.error(ID_INVALID);
            return;
        }
        int id = Integer.parseInt(unsafeIdParam);

        User dbUser;
        try {
            dbUser = userDAO.getById(id);
            if (dbUser == null) {
                jsonResponse.error(USER_NOT_FOUND);
                return;
            }
        } catch (DAOException e) {
            Logger.error("Retrieve user", unsafeIdParam, e.toString());
            jsonResponse.internalServerError();
            return;
        }

        // Hide password from output
        dbUser.password = null;

        jsonResponse.response(new Gson().toJson(dbUser));
    }
}
