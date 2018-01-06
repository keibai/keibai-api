package main.java.servlets.auction;

import main.java.dao.AuctionDAO;
import main.java.dao.DAOException;
import main.java.dao.EventDAO;
import main.java.dao.sql.AuctionDAOSQL;
import main.java.dao.sql.EventDAOSQL;
import main.java.models.Auction;
import main.java.models.Event;
import main.java.utils.HttpSession;
import main.java.utils.JsonResponse;
import main.java.utils.Logger;
import main.java.utils.Validator;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "AuctionClose", urlPatterns = { "/auctions/close" })
public class AuctionClose extends HttpServlet {

    public static final String ID_INVALID = "Invalid ID";
    public static final String ID_NONE = "Auction ID can not be empty";
    public static final String AUCTION_NOT_FOUND = "Auction not found";
    public static final String WRONG_AUCTION_STATUS = "Wrong auction status";
    public static final String EVENT_NOT_IN_PROGRESS = "Event not in progress";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        JsonResponse jsonResponse = new JsonResponse(response);
        HttpSession session = new HttpSession(request);
        AuctionDAO auctionDAO = AuctionDAOSQL.getInstance();
        EventDAO eventDAO = EventDAOSQL.getInstance();

        int userId = session.userId();
        if (userId == -1) {
            jsonResponse.unauthorized();
            return;
        }

        String param = request.getParameter("id");
        if (param == null) {
            jsonResponse.error(ID_NONE);
            return;
        }

        if (!Validator.isNumber(param)) {
            jsonResponse.error(ID_INVALID);
            return;
        }

        int auctionId = Integer.parseInt(param);

        // Retrieve auction
        Auction dbAuction;
        try {
            dbAuction = auctionDAO.getById(auctionId);
        } catch (DAOException e) {
            Logger.error("Retrieve auction on auction close", param, e.toString());
            jsonResponse.internalServerError();
            return;
        }

        if (dbAuction == null) {
            jsonResponse.error(AUCTION_NOT_FOUND);
            return;
        }

        if (!dbAuction.status.equals(Auction.IN_PROGRESS)) {
            jsonResponse.error(WRONG_AUCTION_STATUS);
            return;
        }

        // Retrieve event
        Event dbEvent;
        try {
            dbEvent = eventDAO.getById(dbAuction.eventId);
        } catch (DAOException e) {
            Logger.error("Retrieve event on auction close " + dbAuction.eventId, e.toString());
            jsonResponse.internalServerError();
            return;
        }

        if (dbEvent.ownerId != userId) {
            jsonResponse.unauthorized();
            return;
        }

        if (!dbEvent.status.equals(Event.IN_PROGRESS)) {
            jsonResponse.error(EVENT_NOT_IN_PROGRESS);
            return;
        }

        // All checks passed
        // Update the auction
        switch (dbEvent.auctionType) {
            case Event.ENGLISH:
                break;
            case Event.COMBINATORIAL:
                // TODO: Combinatorial auctions solver will be called here
                break;
        }

        // Close event if all the auctions are closed
    }
}
