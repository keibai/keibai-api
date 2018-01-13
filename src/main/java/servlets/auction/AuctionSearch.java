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

@WebServlet(name = "AuctionSearch", urlPatterns = {"/auctions/search"})
public class AuctionSearch extends HttpServlet {

    public static final String AUCTION_NOT_FOUND = "Auction not found";
    public static final String ID_INVALID = "Invalid ID";
    public static final String ID_NONE = "Auction ID can not be empty";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpResponse httpResponse = new HttpResponse(response);
        AuctionDAO auctionDAO = AuctionDAOSQL.getInstance();

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

        Auction dbAucion;
        try {
            dbAucion = auctionDAO.getById(auctionId);
        } catch (DAOException e) {
            Logger.error("Retrieve auction", param, e.toString());
            httpResponse.internalServerError();
            return;
        }

        if (dbAucion == null) {
            httpResponse.error(AUCTION_NOT_FOUND);
            return;
        }

        httpResponse.response(new BetterGson().newInstance().toJson(dbAucion));
    }
}
