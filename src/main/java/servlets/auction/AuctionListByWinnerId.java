package main.java.servlets.auction;

import com.google.gson.Gson;
import main.java.dao.AuctionDAO;
import main.java.dao.DAOException;
import main.java.dao.sql.AuctionDAOSQL;
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
import java.util.List;

@WebServlet(name = "AuctionListByWinnerId", urlPatterns = {"/auctions/winnerlist"})
public class AuctionListByWinnerId extends HttpServlet {

    public static final String ID_NONE = "Winner ID can not be empty";
    public static final String ID_INVALID = "Invalid winner ID";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpResponse httpResponse = new HttpResponse(response);
        AuctionDAO auctionDAO = AuctionDAOSQL.getInstance();

        String param = request.getParameter("winnerid");
        if (param == null) {
            httpResponse.error(ID_NONE);
            return;
        }

        if (!Validator.isNumber(param)) {
            httpResponse.error(ID_INVALID);
            return;
        }

        int winnerId = Integer.parseInt(param);

        List<Auction> dbAuctions;
        try {
            dbAuctions = auctionDAO.getListByWinnerId(winnerId);
        } catch (DAOException e) {
            Logger.error("Retrieve auction list by winner ID: " + winnerId, e.toString());
            httpResponse.internalServerError();
            return;
        }

        httpResponse.response(new Gson().toJson(dbAuctions.toArray()));
    }
}
