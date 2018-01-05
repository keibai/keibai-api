package main.java.servlets.good;

import com.google.gson.Gson;
import main.java.dao.AuctionDAO;
import main.java.dao.DAOException;
import main.java.dao.GoodDAO;
import main.java.dao.sql.AuctionDAOSQL;
import main.java.dao.sql.GoodDAOSQL;
import main.java.models.Auction;
import main.java.models.Good;
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

@WebServlet(name = "GoodListByAuctionId", urlPatterns = {"/goods/list"})
public class GoodListByAuctionId extends HttpServlet {

    public static final String ID_NONE = "Auction ID can not be empty";
    public static final String ID_INVALID = "Invalid event ID";
    public static final String AUCTION_NOT_EXIST = "Auction does not exist";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        JsonResponse jsonResponse = new JsonResponse(response);
        AuctionDAO auctionDAO = AuctionDAOSQL.getInstance();
        GoodDAO goodDAO = GoodDAOSQL.getInstance();

        String param = request.getParameter("auctionid");
        if (param == null) {
            jsonResponse.error(ID_NONE);
            return;
        }

        if (!Validator.isNumber(param)) {
            jsonResponse.error(ID_INVALID);
            return;
        }

        int auctionId = Integer.parseInt(param);

        Auction dbAuction;
        try {
            dbAuction = auctionDAO.getById(auctionId);
        } catch (DAOException e) {
            Logger.error("Retrieve auction by ID on GoodListByAuctionId:", param, e.toString());
            jsonResponse.internalServerError();
            return;
        }

        if (dbAuction == null) {
            jsonResponse.error(AUCTION_NOT_EXIST);
            return;
        }

        List<Good> dbGoods;
        try {
            dbGoods = goodDAO.getListByAuctionId(auctionId);
        } catch (DAOException e) {
            Logger.error("Retrieve event list by auction ID on GoodListByAuctionId:", param, e.toString());
            jsonResponse.internalServerError();
            return;
        }

        jsonResponse.response(new Gson().toJson(dbGoods.toArray()));
    }
}
