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
import main.java.utils.HttpSession;
import main.java.utils.JsonResponse;
import main.java.utils.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "AuctionNew", urlPatterns = {"/auctions/new"})
public class AuctionNew extends HttpServlet {

    public static final String NAME_ERROR = "Auction name cannot be blank";
    public static final String AUCTION_STARTING_PRICE_ERROR = "Auction starting price must be a positive number";
    public static final String EVENT_NOT_EXIST_ERROR = "Event does not exist";
    public static final String EVENT_NOT_ACTIVE = "Event not active. Can not create an auction";

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        JsonResponse jsonResponse = new JsonResponse(response);
        HttpSession session = new HttpSession(request);
        EventDAO eventDAO = EventDAOSQL.getInstance();
        AuctionDAO auctionDAO = AuctionDAOSQL.getInstance();

        int userId = session.userId();
        if (userId == -1) {
            jsonResponse.unauthorized();
            return;
        }

        Auction unsafeAuction;
        try {
             unsafeAuction = new HttpRequest(request).extractPostRequestBody(Auction.class);
        } catch (IOException|JsonSyntaxException e) {
            jsonResponse.invalidRequest();
            return;
        }

        if (unsafeAuction == null) {
            jsonResponse.invalidRequest();
            return;
        }

        if (unsafeAuction.name == null || unsafeAuction.name.trim().isEmpty()) {
            jsonResponse.error(NAME_ERROR);
            return;
        }

        if (unsafeAuction.startingPrice <= 0) {
            jsonResponse.error(AUCTION_STARTING_PRICE_ERROR);
            return;
        }

        Event event;
        try {
            event = eventDAO.getById(unsafeAuction.eventId);
        } catch (DAOException e) {
            Logger.error("Get event by ID", String.valueOf(unsafeAuction.eventId), e.toString());
            jsonResponse.internalServerError();
            return;
        }

        if (event == null) {
            jsonResponse.error(EVENT_NOT_EXIST_ERROR);
            return;
        }

        if (!event.status.equals(Event.ACTIVE)) {
            jsonResponse.error(EVENT_NOT_ACTIVE);
            return;
        }

        Auction newAuction = new Auction();
        newAuction.name = unsafeAuction.name;
        newAuction.startingPrice = unsafeAuction.startingPrice;
        newAuction.eventId = unsafeAuction.eventId;
        newAuction.ownerId = userId;
        newAuction.status = Auction.OPENED;
        newAuction.winnerId = 0;
        newAuction.valid = Auction.PENDING;

        Auction dbAuction;
        try {
            dbAuction = auctionDAO.create(newAuction);
        } catch (DAOException e) {
            Logger.error("Create auction", newAuction.toString(), e.toString());
            jsonResponse.internalServerError();
            return;
        }

        jsonResponse.response(new Gson().toJson(dbAuction));
    }
}
