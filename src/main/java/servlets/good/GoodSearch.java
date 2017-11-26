package main.java.servlets.good;

import com.google.gson.Gson;
import main.java.dao.DAOException;
import main.java.dao.GoodDAO;
import main.java.dao.sql.GoodDAOSQL;
import main.java.models.Good;
import main.java.utils.JsonResponse;
import main.java.utils.Logger;
import main.java.utils.Validator;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "GoodSearch", urlPatterns = {"/goods/search"})
public class GoodSearch extends HttpServlet {

    public static final String GOOD_NOT_FOUND_ERROR = "Good not found";
    public static final String ID_ERROR = "Event ID must be a number";
    public static final String ID_NONE_ERROR = "No ID parameter was sent";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        JsonResponse jsonResponse = new JsonResponse(response);
        GoodDAO goodDAO = GoodDAOSQL.getInstance();

        String param = request.getParameter("id");

        if (param == null || param.trim().isEmpty()) {
            jsonResponse.error(ID_NONE_ERROR);
            return;
        }

        if (!Validator.isNumber(param)) {
            jsonResponse.error(ID_ERROR);
            return;
        }

        int goodId = Integer.parseInt(param);
        Good retrievedGood;
        try {
            retrievedGood = goodDAO.getById(goodId);
        } catch (DAOException e) {
            Logger.error("Get good by ID " + goodId, e.toString());
            return;
        }

        if (retrievedGood == null) {
            jsonResponse.error(GOOD_NOT_FOUND_ERROR);
            return;
        }

        jsonResponse.response(new Gson().toJson(retrievedGood));
    }
}
