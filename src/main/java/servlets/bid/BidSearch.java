package main.java.servlets.bid;

import com.google.gson.Gson;
import main.java.dao.DAOException;
import main.java.dao.BidDAO;
import main.java.dao.sql.BidDAOSQL;
import main.java.models.Bid;
import main.java.utils.HttpResponse;
import main.java.utils.Logger;
import main.java.utils.Validator;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "BidSearch", urlPatterns = {"/bids/search"})
public class BidSearch extends HttpServlet {

    public static final String BID_NOT_FOUND_ERROR = "Bid not found";
    public static final String ID_ERROR = "Event ID must be a number";
    public static final String ID_NONE_ERROR = "No ID parameter was sent";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpResponse httpResponse = new HttpResponse(response);
        BidDAO bidDAO = BidDAOSQL.getInstance();

        String param = request.getParameter("id");

        if (param == null || param.trim().isEmpty()) {
            httpResponse.error(ID_NONE_ERROR);
            return;
        }

        if (!Validator.isNumber(param)) {
            httpResponse.error(ID_ERROR);
            return;
        }

        int bidId = Integer.parseInt(param);
        Bid retrievedBid;
        try {
            retrievedBid = bidDAO.getById(bidId);
        } catch (DAOException e) {
            Logger.error("Get bid by ID " + bidId, e.toString());
            return;
        }

        if (retrievedBid == null) {
            httpResponse.error(BID_NOT_FOUND_ERROR);
            return;
        }

        httpResponse.response(new Gson().toJson(retrievedBid));
    }
}
