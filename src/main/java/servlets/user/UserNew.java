package main.java.servlets.user;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import main.java.dao.DAOException;
import main.java.dao.NotFoundException;
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

@WebServlet(name = "UserNew", urlPatterns = {"/users/new"})
public class UserNew extends HttpServlet {

    public static final String EMAIL_BLANK = "Email cannot be blank.";
    public static final String EMAIL_INVALID = "Invalid email.";
    public static final String EMAIL_TAKEN = "Email is already taken";
    public static final String PASSWORD_BLANK = "Password cannot be blank";
    public static final String PASSWORD_LENGTH = "Password should be longer than 4 characters";
    public static final String NAME_BLANK = "Name cannot be blank";

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

        // Validate fields.
        if (unsafeUser.email == null) {
            jsonResponse.error(EMAIL_BLANK);
            return;
        }
        if (!Validator.isEmail(unsafeUser.email)) {
            jsonResponse.error(EMAIL_INVALID);
            return;
        }
        if (unsafeUser.password == null) {
            jsonResponse.error(PASSWORD_BLANK);
            return;
        }
        if (!Validator.isLength(unsafeUser.password, 4)) {
            jsonResponse.error(PASSWORD_LENGTH);
            return;
        }
        if (unsafeUser.name == null) {
            jsonResponse.error(NAME_BLANK);
            return;
        }

        // Extract valid information.
        User newUser = new User();
        newUser.email = unsafeUser.email;
        newUser.password = unsafeUser.password;
        newUser.name = unsafeUser.name;
        newUser.lastName = unsafeUser.lastName;

        // Validate email against the DB.
        try {
            User user = userDAO.getByEmail(newUser.email);
            if (user != null) {
                jsonResponse.error(EMAIL_TAKEN);
                return;
            }
        } catch (DAOException e) {
            Logger.error("Validate email against the DB", newUser.email, e.toString());
            jsonResponse.internalServerError();
            return;
        }

        // Hash password
        newUser.password = new PasswordAuthentication().hash(newUser.password.toCharArray());

        User dbUser;
        try {
            dbUser = userDAO.create(newUser);
        } catch(DAOException e) {
            Logger.error("Create user", newUser.toString(), e.toString());
            jsonResponse.internalServerError();
            return;
        }

        // Hide password from output
        dbUser.password = null;

        jsonResponse.response(new Gson().toJson(dbUser));
    }

}