package main.java.servlets.event;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import main.java.dao.DAOException;
import main.java.dao.EventDAO;
import main.java.dao.UserDAO;
import main.java.dao.sql.EventDAOSQL;
import main.java.dao.sql.UserDAOSQL;
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

@WebServlet(name = "EventNew", urlPatterns = {"/events/new"})
public class EventNew extends HttpServlet {

    public static final String NAME_ERROR = "Name cannot be blank";
    public static final String AUCTION_TIME_ERROR = "Auction time must be greater than 10 seconds";
    public static final String LOCATION_ERROR = "Location cannot be blank";
    public static final String AUCTION_TYPE_ERROR = "Invalid auction type";
    public static final String CATEGORY_ERROR = "Category cannot be blank";

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        JsonResponse jsonResponse = new JsonResponse(response);
        HttpSession session = new HttpSession(request);
        EventDAO eventDAO = EventDAOSQL.getInstance();
        UserDAO userDAO = UserDAOSQL.getInstance();

        int userId = session.userId();
        if (userId == -1) {
            jsonResponse.unauthorized();
            return;
        }

        Event unsafeEvent;
        try {
             unsafeEvent = new HttpRequest(request).extractPostRequestBody(Event.class);
        } catch (IOException|JsonSyntaxException e) {
            jsonResponse.invalidRequest();
            return;
        }

        if (unsafeEvent == null) {
            jsonResponse.invalidRequest();
            return;
        }

        if (unsafeEvent.name == null || unsafeEvent.name.trim().isEmpty()) {
            jsonResponse.error(NAME_ERROR);
            return;
        }
        if (unsafeEvent.auctionTime < 10) {
            jsonResponse.error(AUCTION_TIME_ERROR);
            return;
        }
        if (unsafeEvent.location == null || unsafeEvent.location.trim().isEmpty()) {
            jsonResponse.error(LOCATION_ERROR);
            return;
        }
        if (!Arrays.asList(Event.AUCTION_TYPES).contains(unsafeEvent.auctionType)) {
            jsonResponse.error(AUCTION_TYPE_ERROR);
            return;
        }
        if (unsafeEvent.category == null || unsafeEvent.category.trim().isEmpty()) {
            jsonResponse.error(CATEGORY_ERROR);
            return;
        }

        Event newEvent = new Event();
        newEvent.name = unsafeEvent.name;
        newEvent.auctionTime = unsafeEvent.auctionTime;
        newEvent.location = unsafeEvent.location;
        newEvent.auctionType = unsafeEvent.auctionType;
        newEvent.category = unsafeEvent.category;
        newEvent.ownerId = userId;
        newEvent.status = Event.OPENED;

        Event dbEvent;
        try {
            dbEvent = eventDAO.create(newEvent);
        } catch (DAOException e) {
            Logger.error("Create event", newEvent.toString(), e.toString());
            jsonResponse.internalServerError();
            return;
        }

        jsonResponse.response(new Gson().toJson(dbEvent));
    }
}
