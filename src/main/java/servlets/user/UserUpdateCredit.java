package main.java.servlets.user;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import main.java.dao.DAOException;
import main.java.dao.UserDAO;
import main.java.dao.sql.UserDAOSQL;
import main.java.models.User;
import main.java.utils.HttpRequest;
import main.java.utils.DefaultHttpSession;
import main.java.utils.HttpResponse;
import main.java.utils.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "UserUpdateCredit", urlPatterns = {"/users/update/credit"})
public class UserUpdateCredit extends HttpServlet {

    public static final String INVALID_CREDIT_AMOUNT = "Invalid credit amount";
    public static final String USER_NOT_EXIST = "User does not exist";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpResponse httpResponse = new HttpResponse(response);
        DefaultHttpSession session = new DefaultHttpSession(request);
        UserDAO userDAO = UserDAOSQL.getInstance();

        int userId = session.userId();
        if (userId == -1) {
            httpResponse.unauthorized();
            return;
        }

        // Retrieve body data
        User unsafeUser;
        try {
            unsafeUser = new HttpRequest(request).extractPostRequestBody(User.class);
        } catch (IOException|JsonSyntaxException e) {
            httpResponse.invalidRequest();
            return;
        }

        if (unsafeUser == null) {
            httpResponse.invalidRequest();
            return;
        }

        double creditDiff = unsafeUser.credit;

        User user;
        try {
            user = userDAO.getById(userId);
        } catch (DAOException e) {
            Logger.error("Get user by ID in update user credit: UserID " + userId, e.toString());
            httpResponse.internalServerError();
            return;
        }

        if (user == null) {
            httpResponse.error(USER_NOT_EXIST);
            return;
        }

        if (user.credit + creditDiff < 0) {
            httpResponse.error(INVALID_CREDIT_AMOUNT);
            return;
        }

        user.credit += creditDiff;

        User dbUser;
        try {
            dbUser = userDAO.update(user);
        } catch(DAOException e) {
            Logger.error("Create user in update user credit", user.toString(), e.toString());
            httpResponse.internalServerError();
            return;
        }

        // Hide password from output
        dbUser.password = null;

        httpResponse.response(new Gson().toJson(dbUser));
    }
}
