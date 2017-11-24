package main.java.servlets.user;

import com.google.gson.JsonSyntaxException;
import main.java.dao.DAOException;
import main.java.dao.UserDAO;
import main.java.dao.sql.UserDAOSQL;
import main.java.models.User;
import main.java.utils.*;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "UserAuthenticate", urlPatterns = {"/users/authenticate"})
public class UserAuthenticate extends HttpServlet {

    public static final String EMAIL_BLANK = "Email cannot be blank.";
    public static final String EMAIL_INVALID = "Email is invalid.";
    public static final String EMAIL_NOT_FOUND = "Email is not registered in the system";
    public static final String PASSWORD_BLANK = "Password cannot be blank.";
    public static final String PASSWORD_INVALID = "Invalid password.";

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        JsonResponse jsonResponse = new JsonResponse(response);
        UserDAO userDAO = UserDAOSQL.getInstance();

        // Retrieve body data.
        User unsafeUser;
        try {
            unsafeUser = new HttpRequest(request).extractPostRequestBody(User.class);
        } catch (IOException|JsonSyntaxException e) {
            jsonResponse.invalidRequest();
            return;
        }

        // Do a little validation (rest will be handled by comparing with user DAO).
        if (unsafeUser.email == null) {
            jsonResponse.error(EMAIL_BLANK);
            return;
        } else if (!Validator.isEmail(unsafeUser.email)) {
            jsonResponse.error(EMAIL_INVALID);
            return;
        } else if (unsafeUser.password == null) {
            jsonResponse.error(PASSWORD_BLANK);
            return;
        }

        // Try find the user on the database.
        User dbUser;
        try {
            dbUser = userDAO.getByEmail(unsafeUser.email);
        } catch (DAOException e) {
            Logger.error("Retrieve DB user", e.toString());
            return;
        }
        if (dbUser == null) {
            jsonResponse.error(EMAIL_NOT_FOUND);
            return;
        }

        // Compare password.
        boolean passwordMatches = new PasswordAuthentication().authenticate(unsafeUser.password.toCharArray(), dbUser.password);
        if (!passwordMatches) {
            jsonResponse.error(PASSWORD_INVALID);
            return;
        }

        // Finish the sign in. Store the user on the session storage.
        HttpSession httpSession = new HttpSession(request);
        httpSession.save(HttpSession.USER_ID_KEY, dbUser.id);

        jsonResponse.ok();
    }
}