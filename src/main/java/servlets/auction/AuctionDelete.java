package main.java.servlets.auction;

import com.google.gson.Gson;
import main.java.dao.AuctionDAO;
import main.java.dao.DAOException;
import main.java.dao.EventDAO;
import main.java.dao.sql.AuctionDAOSQL;
import main.java.dao.sql.EventDAOSQL;
import main.java.models.Auction;
import main.java.models.Event;
import main.java.models.meta.Msg;
import main.java.utils.DefaultHttpSession;
import main.java.utils.HttpResponse;
import main.java.utils.Logger;
import main.java.utils.Validator;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "AuctionDelete", urlPatterns = {"/auctions/delete"})
public class AuctionDelete extends HttpServlet {

    public static final String AUCTION_NOT_FOUND = "Auction not found";
    public static final String ID_NONE = "Auction ID can not be empty";
    public static final String ID_INVALID = "Invalid ID";
    public static final String CAN_NOT_DELETE = "Can not delete auction";
    public static final String DELETED = "Deleted";
    public static final String WRONG_STATUS = "Can not delete a non-pending auction";

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

        Auction dbAucion;
        try {
            dbAucion = auctionDAO.getById(auctionId);
        } catch (DAOException e) {
            Logger.error("Retrieve auction in delete auction", param, e.toString());
            httpResponse.internalServerError();
            return;
        }

        if (dbAucion == null) {
            httpResponse.error(AUCTION_NOT_FOUND);
            return;
        }

        if (!dbAucion.status.equals(Auction.PENDING)) {
            httpResponse.error(WRONG_STATUS);
            return;
        }

        Event dbEvent;
        try {
            dbEvent = eventDAO.getById(dbAucion.eventId);
        } catch (DAOException e) {
            Logger.error("Retrieve event in delete auction", String.valueOf(dbAucion.eventId), e.toString());
            httpResponse.internalServerError();
            return;
        }

        if (dbEvent.ownerId != userId) {
            httpResponse.unauthorized();
            return;
        }

        boolean deleted;
        try {
            deleted = auctionDAO.delete(dbAucion.id);
        } catch (DAOException e) {
            Logger.error("Delete auction", String.valueOf(dbAucion.id), e.toString());
            httpResponse.internalServerError();
            return;
        }

        if (!deleted) {
            httpResponse.error(CAN_NOT_DELETE);
            return;
        }

        Msg msg = new Msg();
        msg.msg = DELETED;
        httpResponse.response(new Gson().toJson(msg));
    }
}
