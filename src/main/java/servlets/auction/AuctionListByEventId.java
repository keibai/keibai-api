package main.java.servlets.auction;

import com.google.gson.Gson;
import main.java.dao.AuctionDAO;
import main.java.dao.DAOException;
import main.java.dao.sql.AuctionDAOSQL;
import main.java.models.Auction;
import main.java.utils.JsonResponse;
import main.java.utils.Logger;
import main.java.utils.Validator;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet(name = "AuctionListByEventId", urlPatterns = {"/auctions/search"})
public class AuctionListByEventId extends HttpServlet {

    public static final String ID_NONE = "Event ID can not be empty";
    public static final String ID_INVALID = "Invalid event ID";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        JsonResponse jsonResponse = new JsonResponse(response);
        AuctionDAO auctionDAO = AuctionDAOSQL.getInstance();

        String param = request.getParameter("event");
        if (param == null) {
            jsonResponse.error(ID_NONE);
            return;
        }

        if (!Validator.isNumber(param)) {
            jsonResponse.error(ID_INVALID);
            return;
        }

        int eventId = Integer.parseInt(param);

        List<Auction> dbAuctions;
        try {
            dbAuctions = auctionDAO.getListByEventId(eventId);
        } catch (DAOException e) {
            Logger.error("Retrieve auction list by event ID: " + eventId, e.toString());
            jsonResponse.internalServerError();
            return;
        }

        jsonResponse.response(new Gson().toJson(dbAuctions.toArray()));
    }
}
