package main.java.servlets.auction;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import main.java.dao.AuctionDAO;
import main.java.dao.DAOException;
import main.java.dao.EventDAO;
import main.java.dao.UserDAO;
import main.java.dao.sql.AuctionDAOSQL;
import main.java.dao.sql.EventDAOSQL;
import main.java.dao.sql.UserDAOSQL;
import main.java.models.Auction;
import main.java.models.Event;
import main.java.models.User;
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
import java.util.Arrays;

@WebServlet(name = "AuctionNew", urlPatterns = {"/auctions/new"})
public class AuctionNew extends HttpServlet {

    public static final String NAME_ERROR = "Auction name cannot be blank";
    public static final String AUCTION_STARTING_PRICE_ERROR = "Auction starting price must be a positive number";
    public static final String AUCTION_START_TIME_ERROR = "Auction must have a start time";
    public static final String AUCTION_STATUS_ERROR = "Status must be valid";
    public static final String AUCTION_IS_NOT_VALID_ERROR = "Auction is not valid";
    public static final String OWNER_NOT_EXIST_ERROR = "Owner does not exist";
    public static final String EVENT_NOT_EXIST_ERROR = "Event does not exist";

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        JsonResponse jsonResponse = new JsonResponse(response);
        HttpSession session = new HttpSession(request);
        AuctionDAO auctionDAO = AuctionDAOSQL.getInstance();
        UserDAO userDAO = UserDAOSQL.getInstance();
        EventDAO eventDAO = EventDAOSQL.getInstance();

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
        if (!Arrays.asList(Auction.AUCTION_STATUSES).contains(unsafeAuction.status)) {
            jsonResponse.error(AUCTION_STATUS_ERROR);
            return;
        }

        User owner;
        try {
            owner = userDAO.getById(unsafeAuction.ownerId);
        } catch (DAOException e) {
            Logger.error("Get user by ID " + unsafeAuction.ownerId, e.toString());
            jsonResponse.internalServerError();
            return;
        }
        if (owner == null) {
            jsonResponse.error(OWNER_NOT_EXIST_ERROR);
            return;
        }

        Event event;
        try {
            event = eventDAO.getById(unsafeAuction.eventId);
        } catch (DAOException e) {
            Logger.error("Get event by ID " + unsafeAuction.eventId, e.toString());
            jsonResponse.internalServerError();
            return;
        }
        if (event == null) {
            jsonResponse.error(OWNER_NOT_EXIST_ERROR);
            return;
        }

        Auction newAuction = new Auction();
        newAuction.name = unsafeAuction.name;
        newAuction.startingPrice = unsafeAuction.startingPrice;
        newAuction.startTime = unsafeAuction.startTime;
        newAuction.eventId = unsafeAuction.eventId;
        newAuction.ownerId = userId;
        newAuction.status = unsafeAuction.status;
        newAuction.winnerId = 0;
        newAuction.isValid = unsafeAuction.isValid;

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
