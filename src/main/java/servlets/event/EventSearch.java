package main.java.servlets.event;

import com.google.gson.Gson;
import main.java.dao.DAOException;
import main.java.dao.EventDAO;
import main.java.dao.sql.EventDAOSQL;
import main.java.gson.BetterGson;
import main.java.models.Event;
import main.java.utils.HttpResponse;
import main.java.utils.Logger;
import main.java.utils.Validator;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "EventSearch", urlPatterns = {"/events/search"})
public class EventSearch extends HttpServlet {

    public static final String ID_ERROR = "Event ID must be a number";
    public static final String EVENT_NOT_FOUND_ERROR = "Event not found";
    public static final String ID_NONE_ERROR = "No ID parameter was sent";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpResponse httpResponse = new HttpResponse(response);
        EventDAO eventDAO = EventDAOSQL.getInstance();

        String param = request.getParameter("id");

        if (param == null || param.trim().isEmpty()) {
            httpResponse.error(ID_NONE_ERROR);
            return;
        }

        if (!Validator.isNumber(param)) {
            httpResponse.error(ID_ERROR);
            return;
        }

        int eventId = Integer.parseInt(param);
        Event retrievedEvent;
        try {
            retrievedEvent = eventDAO.getById(eventId);
        } catch (DAOException e) {
            Logger.error("Get event by ID " + eventId, e.toString());
            return;
        }

        if (retrievedEvent == null) {
            httpResponse.error(EVENT_NOT_FOUND_ERROR);
            return;
        }

        httpResponse.response(new BetterGson().newInstance().toJson(retrievedEvent));
    }
}
