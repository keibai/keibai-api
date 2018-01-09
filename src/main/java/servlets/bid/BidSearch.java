package main.java.servlets.bid;

import com.google.gson.Gson;
import main.java.dao.AuctionDAO;
import main.java.dao.DAOException;
import main.java.dao.BidDAO;
import main.java.dao.EventDAO;
import main.java.dao.sql.AuctionDAOSQL;
import main.java.dao.sql.BidDAOSQL;
import main.java.dao.sql.EventDAOSQL;
import main.java.models.Auction;
import main.java.models.Bid;
import main.java.models.Event;
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

@WebServlet(name = "BidSearch", urlPatterns = {"/bids/search"})
public class BidSearch extends HttpServlet {

    public static final String BID_NOT_FOUND_ERROR = "Bid not found";
    public static final String ID_ERROR = "Event ID must be a number";
    public static final String ID_NONE_ERROR = "No ID parameter was sent";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpResponse httpResponse = new HttpResponse(response);
        DefaultHttpSession session = new DefaultHttpSession(request);
        BidDAO bidDAO = BidDAOSQL.getInstance();
        AuctionDAO auctionDAO = AuctionDAOSQL.getInstance();
        EventDAO eventDAO = EventDAOSQL.getInstance();

        String param = request.getParameter("id");

        if (param == null || param.trim().isEmpty()) {
            httpResponse.error(ID_NONE_ERROR);
            return;
        }

        if (!Validator.isNumber(param)) {
            httpResponse.error(ID_ERROR);
            return;
        }

        int bidId = Integer.parseInt(param);
        Bid dbBid;
        try {
            dbBid = bidDAO.getById(bidId);
        } catch (DAOException e) {
            Logger.error("Get bid by ID", String.valueOf(bidId), e.toString());
            return;
        }

        if (dbBid == null) {
            httpResponse.error(BID_NOT_FOUND_ERROR);
            return;
        }

        // Combinatorial bids require hiding amount.
        Auction dbAuction;
        try {
            dbAuction = auctionDAO.getById(dbBid.auctionId);
        } catch (DAOException e) {
            Logger.error("Get auction by bid ID", String.valueOf(dbBid.id), e.toString());
            return;
        }

        Event dbEvent;
        try {
            dbEvent = eventDAO.getById(dbAuction.eventId);
        } catch (DAOException e) {
            Logger.error("Get event by auction ID", String.valueOf(dbBid.id), String.valueOf(dbBid.auctionId), String.valueOf(dbAuction.eventId), e.toString());
            return;
        }

        if (dbEvent.auctionType.equals(Event.COMBINATORIAL) && dbBid.ownerId != session.userId()) {
            dbBid.amount = 0.0;
        }

        httpResponse.response(new Gson().toJson(dbBid));
    }
}
