package main.java.servlets.bid;

import com.google.gson.Gson;
import main.java.dao.BidDAO;
import main.java.dao.DAOException;
import main.java.dao.sql.BidDAOSQL;
import main.java.models.Bid;
import main.java.utils.DefaultHttpSession;
import main.java.utils.HttpResponse;
import main.java.utils.Logger;
import main.java.utils.Validator;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet(name = "BidListByOwnerId", urlPatterns = {"/bids/list"})
public class BidListByOwnerId extends HttpServlet {

    public static final String ID_NONE = "Owner ID can not be empty";
    public static final String ID_INVALID = "Invalid owner ID";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpResponse httpResponse = new HttpResponse(response);
        DefaultHttpSession session = new DefaultHttpSession(request);
        BidDAO bidDAO = BidDAOSQL.getInstance();

        int userId = session.userId();
        if (userId == -1) {
            httpResponse.unauthorized();
            return;
        }

        String param = request.getParameter("ownerid");
        if (param == null) {
            httpResponse.error(ID_NONE);
            return;
        }

        if (!Validator.isNumber(param)) {
            httpResponse.error(ID_INVALID);
            return;
        }

        int ownerId = Integer.parseInt(param);

        if (ownerId != userId) {
            httpResponse.unauthorized();
            return;
        }

        List<Bid> dbBids;
        try {
            dbBids = bidDAO.getListByOwnerId(ownerId);
        } catch (DAOException e) {
            Logger.error("Retrieve bid list by owner ID: " + ownerId, e.toString());
            httpResponse.internalServerError();
            return;
        }

        httpResponse.response(new Gson().toJson(dbBids.toArray()));
    }
}
