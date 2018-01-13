package main.java.servlets.auction;

import com.google.gson.Gson;
import main.java.dao.AuctionDAO;
import main.java.dao.DAOException;
import main.java.dao.sql.AuctionDAOSQL;
import main.java.gson.BetterGson;
import main.java.models.Auction;
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

@WebServlet(name = "AuctionListByEventId", urlPatterns = {"/auctions/list"})
public class AuctionListByEventId extends HttpServlet {

    public static final String ID_NONE = "Event ID can not be empty";
    public static final String ID_INVALID = "Invalid event ID";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpResponse httpResponse = new HttpResponse(response);
        AuctionDAO auctionDAO = AuctionDAOSQL.getInstance();
        Gson gson = new BetterGson().newInstance();

        String param = request.getParameter("eventid");
        if (param == null) {
            httpResponse.error(ID_NONE);
            return;
        }

        if (!Validator.isNumber(param)) {
            httpResponse.error(ID_INVALID);
            return;
        }

        int eventId = Integer.parseInt(param);

        List<Auction> dbAuctions;
        try {
            dbAuctions = auctionDAO.getListByEventId(eventId);
        } catch (DAOException e) {
            Logger.error("Retrieve auction list by event ID: " + eventId, e.toString());
            httpResponse.internalServerError();
            return;
        }

        httpResponse.response(gson.toJson(dbAuctions.toArray()));
    }
}
