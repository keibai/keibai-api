package main.java.servlets.good;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import main.java.dao.GoodDAO;
import main.java.dao.DAOException;
import main.java.dao.sql.GoodDAOSQL;
import main.java.models.Good;
import main.java.utils.HttpRequest;
import main.java.utils.HttpSession;
import main.java.utils.JsonResponse;
import main.java.utils.Logger;


import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "GoodNew", urlPatterns = {"/goods/new"})
public class GoodNew extends HttpServlet {

    public static final String NAME_ERROR = "Good name cannot be blank";
    public static final String IMAGE_ERROR = "Good must have an image";

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        JsonResponse jsonResponse = new JsonResponse(response);
        HttpSession session = new HttpSession(request);
        GoodDAO goodDAO = GoodDAOSQL.getInstance();

        int userId = session.userId();
        if (userId == -1) {
            jsonResponse.unauthorized();
            return;
        }

        Good unsafeGood;
        try {
             unsafeGood = new HttpRequest(request).extractPostRequestBody(Good.class);
        } catch (IOException|JsonSyntaxException e) {
            jsonResponse.invalidRequest();
            return;
        }

        if (unsafeGood.name == null || unsafeGood.name.trim().isEmpty()) {
            jsonResponse.error(NAME_ERROR);
            return;
        }
        if (unsafeGood.image == null || unsafeGood.image.trim().isEmpty()) {
            jsonResponse.error(IMAGE_ERROR);
            return;
        }

        Good newGood = new Good();
        newGood.name = unsafeGood.name;
        newGood.image = unsafeGood.image;
        newGood.auctionId = unsafeGood.auctionId;

        Good dbGood;
        try {
            dbGood = goodDAO.create(newGood);
        } catch (DAOException e) {
            Logger.error("Create good", newGood.toString(), e.toString());
            jsonResponse.internalServerError();
            return;
        }

        jsonResponse.response(new Gson().toJson(dbGood));
    }
}
