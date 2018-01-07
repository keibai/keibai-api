package main.java.servlets.auction;

import com.google.gson.Gson;
import main.java.dao.AuctionDAO;
import main.java.dao.DAOException;
import main.java.dao.EventDAO;
import main.java.dao.sql.AuctionDAOSQL;
import main.java.dao.sql.EventDAOSQL;
import main.java.models.Auction;
import main.java.models.Event;
import main.java.utils.*;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "AuctionAccept", urlPatterns = "/auctions/accept")
public class AuctionAccept extends HttpServlet {

    public static final String ID_NONE = "Auction ID can not be empty";
    public static final String ID_INVALID = "Invalid ID";
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

        String param = request.getParameter("id");
        if (param == null) {
            httpResponse.error(ID_NONE);
            return;
        }

        if (!Validator.isNumber(param)) {
            httpResponse.error(ID_INVALID);
            return;
        }

        int auctionId = Integer.parseInt(param);

        // Retrieve stored auction
        Auction storedAuction;
        try {
            storedAuction = auctionDAO.getById(auctionId);
        } catch (DAOException e) {
            Logger.error("Get auction by ID in update auction status: AuctionID " + auctionId, e.toString());
            httpResponse.internalServerError();
            return;
        }

        if (storedAuction == null) {
            httpResponse.error(AUCTION_NOT_EXIST);
            return;
        }

        if (!storedAuction.status.equals(Auction.PENDING)) {
            httpResponse.error(INVALID_STATUS);
            return;
        }

        // Retrieve event to check owner
        Event storedEvent;
        try {
            storedEvent = eventDAO.getById(storedAuction.eventId);
        } catch (DAOException e) {
            Logger.error("Get event by ID in update auction status: EventID " + storedAuction.eventId, e.toString());
            httpResponse.internalServerError();
            return;
        }

        if (storedEvent.ownerId != userId) {
            httpResponse.unauthorized();
            return;
        }

        // Everything OK, we can update auction status
        storedAuction.status = Auction.ACCEPTED;

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
