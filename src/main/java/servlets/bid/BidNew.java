package main.java.servlets.bid;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import main.java.dao.AuctionDAO;
import main.java.dao.BidDAO;
import main.java.dao.DAOException;
import main.java.dao.UserDAO;
import main.java.dao.sql.AuctionDAOSQL;
import main.java.dao.sql.BidDAOSQL;
import main.java.dao.sql.UserDAOSQL;
import main.java.models.Auction;
import main.java.models.Bid;
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

@WebServlet(name = "BidNew", urlPatterns = {"/bids/new"})
public class BidNew extends HttpServlet {

    public static final String INVALID_AMOUNT_ERROR = "Invalid amount";
    public static final String AUCTION_ID_ERROR = "Missing auction ID";
    public static final String AUCTION_NOT_EXIST_ERROR = "Auction does not exists";
    public static final String OWNER_NOT_EXIST_ERROR = "Owner does not exist";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        JsonResponse jsonResponse = new JsonResponse(response);
        HttpSession session = new HttpSession(request);
        BidDAO bidDAO = BidDAOSQL.getInstance();
        AuctionDAO auctionDAO = AuctionDAOSQL.getInstance();
        UserDAO userDAO = UserDAOSQL.getInstance();

        int userId = session.userId();
        if (userId == -1) {
            jsonResponse.unauthorized();
            return;
        }

        Bid unsafeBid;
        try {
            unsafeBid = new HttpRequest(request).extractPostRequestBody(Bid.class);
        } catch (IOException|JsonSyntaxException e) {
            jsonResponse.invalidRequest();
            return;
        }

        if (unsafeBid == null) {
            jsonResponse.invalidRequest();
            return;
        }

        if (unsafeBid.amount <= 0.0) {
            jsonResponse.error(INVALID_AMOUNT_ERROR);
            return;
        }

        if (unsafeBid.auctionId == 0) {
            jsonResponse.error(AUCTION_ID_ERROR);
            return;
        }

        Auction auction;
        try {
            auction = auctionDAO.getById(unsafeBid.auctionId);
        } catch (DAOException e) {
            Logger.error("Get auction by ID " + unsafeBid.auctionId, e.toString());
            jsonResponse.internalServerError();
            return;
        }
        if (auction == null) {
            jsonResponse.error(AUCTION_NOT_EXIST_ERROR);
            return;
        }

        User owner;
        try {
            owner = userDAO.getById(unsafeBid.ownerId);
        } catch (DAOException e) {
            Logger.error("Get user by ID " + unsafeBid.ownerId, e.toString());
            jsonResponse.internalServerError();
            return;
        }

        if (owner == null) {
            jsonResponse.error(OWNER_NOT_EXIST_ERROR);
            return;
        }

        Bid newBid = new Bid();
        newBid.amount = unsafeBid.amount;
        newBid.auctionId = unsafeBid.auctionId;
        newBid.ownerId = userId;

        Bid dbBid;
        try {
            dbBid = bidDAO.create(newBid);
        } catch (DAOException e) {
            Logger.error("Create bid", newBid.toString(), e.toString());
            jsonResponse.internalServerError();
            return;
        }

        jsonResponse.response(new Gson().toJson(dbBid));
    }
}
