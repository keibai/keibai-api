package main.java.servlets.user;

import main.java.utils.HttpSession;
import main.java.utils.JsonResponse;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "UserDeauthenticate", urlPatterns = {"/users/deauthenticate" })
public class UserDeauthenticate {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = new HttpSession(request);
        session.save(HttpSession.USER_ID_KEY, -1);

        new JsonResponse(response).ok();
    }
}
