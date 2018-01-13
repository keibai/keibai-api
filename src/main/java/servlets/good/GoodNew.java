package main.java.servlets.good;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import main.java.dao.AuctionDAO;
import main.java.dao.EventDAO;
import main.java.dao.GoodDAO;
import main.java.dao.DAOException;
import main.java.dao.sql.AuctionDAOSQL;
import main.java.dao.sql.EventDAOSQL;
import main.java.dao.sql.GoodDAOSQL;
import main.java.gson.BetterGson;
import main.java.models.Auction;
import main.java.models.Event;
import main.java.models.Good;
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
import java.util.List;

@WebServlet(name = "GoodNew", urlPatterns = {"/goods/new"})
public class GoodNew extends HttpServlet {

    public static final String NAME_ERROR = "Good name cannot be blank";
    public static final String IMAGE_ERROR = "Good must have an image";
    public static final String AUCTION_NOT_EXIST_ERROR = "Auction does not exists";
    public static final String WRONG_NUMBER_OF_GOODS = "English auction can only have 1 good";

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpResponse httpResponse = new HttpResponse(response);
        DefaultHttpSession session = new DefaultHttpSession(request);
        GoodDAO goodDAO = GoodDAOSQL.getInstance();
        AuctionDAO auctionDAO = AuctionDAOSQL.getInstance();
        EventDAO eventDAO = EventDAOSQL.getInstance();

        int userId = session.userId();
        if (userId == -1) {
            httpResponse.unauthorized();
            return;
        }

        Good unsafeGood;
        try {
             unsafeGood = new HttpRequest(request).extractPostRequestBody(Good.class);
        } catch (IOException|JsonSyntaxException e) {
            httpResponse.invalidRequest();
            return;
        }

        if (unsafeGood == null) {
            httpResponse.invalidRequest();
            return;
        }

        if (unsafeGood.name == null || unsafeGood.name.trim().isEmpty()) {
            httpResponse.error(NAME_ERROR);
            return;
        }
        if (unsafeGood.image == null || unsafeGood.image.trim().isEmpty()) {
            httpResponse.error(IMAGE_ERROR);
            return;
        }

        Auction auction;
        try {
            auction = auctionDAO.getById(unsafeGood.auctionId);
        } catch (DAOException e) {
            Logger.error("Get auction by ID " + unsafeGood.auctionId, e.toString());
            httpResponse.internalServerError();
            return;
        }
        if (auction == null) {
            httpResponse.error(AUCTION_NOT_EXIST_ERROR);
            return;
        }
        if (auction.ownerId != userId) {
            httpResponse.unauthorized();
            return;
        }

        Event event;
        try {
            event = eventDAO.getById(auction.eventId);
        } catch (DAOException e) {
            Logger.error("Get event by ID " + auction.eventId, e.toString());
            httpResponse.internalServerError();
            return;
        }

        if (event.auctionType.equals(Event.ENGLISH)) {
            // English auction can only have 1 good
            List<Good> auctionGoods;
            try {
                auctionGoods = goodDAO.getListByAuctionId(auction.id);
            } catch (DAOException e) {
                Logger.error("Get list of goods by auction ID " + auction.id, e.toString());
                httpResponse.internalServerError();
                return;
            }
            if (auctionGoods.size() != 0) {
                httpResponse.error(WRONG_NUMBER_OF_GOODS);
                return;
            }
        }

        Good newGood = new Good();
        newGood.name = unsafeGood.name;
        newGood.image = unsafeGood.image;
        newGood.auctionId = unsafeGood.auctionId;

        Good dbGood;
        try {
            dbGood = goodDAO.create(newGood);
        } catch (DAOException e) {
            Logger.error("Create good", newGood.toString(), e.toString());
            httpResponse.internalServerError();
            return;
        }

        httpResponse.response(new BetterGson().newInstance().toJson(dbGood));
    }
}
