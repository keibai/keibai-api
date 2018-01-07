package main.java.servlets.auction;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import main.java.dao.AuctionDAO;
import main.java.dao.DAOException;
import main.java.dao.EventDAO;
import main.java.dao.sql.AuctionDAOSQL;
import main.java.dao.sql.EventDAOSQL;
import main.java.models.Auction;
import main.java.models.Event;
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
import java.util.Arrays;

@WebServlet(name = "AuctionUpdateStatus", urlPatterns = "/auctions/update/status")
public class AuctionUpdateStatus extends HttpServlet {

    public static final String INVALID_STATUS = "Invalid auction status";
    public static final String AUCTION_NOT_EXIST = "Auction does not exist";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpResponse httpResponse = new HttpResponse(response);
        DefaultHttpSession session = new DefaultHttpSession(request);
        AuctionDAO auctionDAO = AuctionDAOSQL.getInstance();
        EventDAO eventDAO = EventDAOSQL.getInstance();

        int userId = session.userId();
        if (userId == -1) {
            httpResponse.unauthorized();
            return;
        }

        // Retrieve body data
        Auction unsafeAuction;
        try {
            unsafeAuction = new HttpRequest(request).extractPostRequestBody(Auction.class);
        } catch (IOException|JsonSyntaxException e) {
            httpResponse.invalidRequest();
            return;
        }

        if (unsafeAuction == null || unsafeAuction.id == 0) {
            httpResponse.invalidRequest();
            return;
        }

        if (!Arrays.asList(Auction.AUCTION_STATUSES).contains(unsafeAuction.status)) {
            httpResponse.error(INVALID_STATUS);
            return;
        }

        // Retrieve stored auction
        Auction storedAuction;
        try {
            storedAuction = auctionDAO.getById(unsafeAuction.id);
        } catch (DAOException e) {
            Logger.error("Get auction by ID in update auction status: AuctionID " + unsafeAuction.id, e.toString());
            httpResponse.internalServerError();
            return;
        }

        if (storedAuction == null) {
            httpResponse.error(AUCTION_NOT_EXIST);
            return;
        }

        // Retrieve event to check owner
        Event storedEvent;
        try {
            storedEvent = eventDAO.getById(storedAuction.eventId);
        } catch (DAOException e) {
            Logger.error("Get event by ID in update auction status: EventID " + unsafeAuction.eventId, e.toString());
            httpResponse.internalServerError();
            return;
        }

        if (storedEvent.ownerId != userId) {
            httpResponse.unauthorized();
            return;
        }

        // Everything OK, we can update auction status
        storedAuction.status = unsafeAuction.status;

        Auction dbAuction;
        try {
            dbAuction = auctionDAO.update(storedAuction);
        } catch (DAOException e) {
            Logger.error("Create auction in update auctionStatus", storedAuction.toString(), e.toString());
            httpResponse.internalServerError();
            return;
        }

        httpResponse.response(new Gson().toJson(dbAuction));
    }
}
