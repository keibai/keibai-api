package main.java.servlets.user;

import main.java.utils.DefaultHttpSession;
import main.java.utils.HttpResponse;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "UserDeauthenticate", urlPatterns = {"/users/deauthenticate" })
public class UserDeauthenticate extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        DefaultHttpSession session = new DefaultHttpSession(request);
        session.save(DefaultHttpSession.USER_ID_KEY, -1);

        new HttpResponse(response).ok();
    }
}
