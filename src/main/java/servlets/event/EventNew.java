package main.java.servlets.event;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import main.java.dao.DAOException;
import main.java.dao.EventDAO;
import main.java.dao.sql.EventDAOSQL;
import main.java.gson.BetterGson;
import main.java.models.Event;
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
import java.util.Arrays;

@WebServlet(name = "EventNew", urlPatterns = {"/events/new"})
public class EventNew extends HttpServlet {

    public static final String NAME_ERROR = "Name cannot be blank";
    public static final String AUCTION_TIME_ERROR = "Auction time must be greater than 10 seconds";
    public static final String LOCATION_ERROR = "Location cannot be blank";
    public static final String AUCTION_TYPE_ERROR = "Invalid auction type";
    public static final String CATEGORY_ERROR = "Category cannot be blank";

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpResponse httpResponse = new HttpResponse(response);
        DefaultHttpSession session = new DefaultHttpSession(request);
        EventDAO eventDAO = EventDAOSQL.getInstance();

        int userId = session.userId();
        if (userId == -1) {
            httpResponse.unauthorized();
            return;
        }

        Event unsafeEvent;
        try {
             unsafeEvent = new HttpRequest(request).extractPostRequestBody(Event.class);
        } catch (IOException|JsonSyntaxException e) {
            httpResponse.invalidRequest();
            return;
        }

        if (unsafeEvent == null) {
            httpResponse.invalidRequest();
            return;
        }

        if (unsafeEvent.name == null || unsafeEvent.name.trim().isEmpty()) {
            httpResponse.error(NAME_ERROR);
            return;
        }
        if (unsafeEvent.location == null || unsafeEvent.location.trim().isEmpty()) {
            httpResponse.error(LOCATION_ERROR);
            return;
        }
        if (!Arrays.asList(Event.AUCTION_TYPES).contains(unsafeEvent.auctionType)) {
            httpResponse.error(AUCTION_TYPE_ERROR);
            return;
        }
        if (unsafeEvent.category == null || unsafeEvent.category.trim().isEmpty()) {
            httpResponse.error(CATEGORY_ERROR);
            return;
        }

        Event newEvent = new Event();
        newEvent.name = unsafeEvent.name;
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
            httpResponse.internalServerError();
            return;
        }

        httpResponse.response(new BetterGson().newInstance().toJson(dbEvent));
    }
}
