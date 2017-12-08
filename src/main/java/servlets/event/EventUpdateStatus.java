package main.java.servlets.event;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import main.java.dao.DAOException;
import main.java.dao.EventDAO;
import main.java.dao.sql.EventDAOSQL;
import main.java.models.Event;
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

@WebServlet(name = "EventUpdateStatus", urlPatterns = {"/events/update/status"})
public class EventUpdateStatus extends HttpServlet {

    public static final String EVENT_NOT_EXIST = "Event does not exist";
    public static final String INVALID_STATUS = "Invalid event status";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        JsonResponse jsonResponse = new JsonResponse(response);
        HttpSession session = new HttpSession(request);
        EventDAO eventDAO = EventDAOSQL.getInstance();

        int userId = session.userId();
        if (userId == -1) {
            jsonResponse.unauthorized();
            return;
        }

        // Retrieve body data
        Event unsafeEvent;
        try {
            unsafeEvent = new HttpRequest(request).extractPostRequestBody(Event.class);
        } catch (IOException|JsonSyntaxException e) {
            jsonResponse.invalidRequest();
            return;
        }

        if (unsafeEvent == null || unsafeEvent.id == 0) {
            jsonResponse.invalidRequest();
            return;
        }

        if (!Arrays.asList(Event.EVENT_STATUS).contains(unsafeEvent.status)) {
            jsonResponse.error(INVALID_STATUS);
            return;
        }

        // Retrieve stored event
        Event storedEvent;
        try {
            storedEvent = eventDAO.getById(unsafeEvent.id);
        } catch (DAOException e) {
            Logger.error("Get event by ID in update event status: EventID " + unsafeEvent.id, e.toString());
            jsonResponse.internalServerError();
            return;
        }

        if (storedEvent == null) {
            jsonResponse.error(EVENT_NOT_EXIST);
            return;
        }

        if (storedEvent.ownerId != userId) {
            jsonResponse.unauthorized();
            return;
        }

        storedEvent.status = unsafeEvent.status;

        Event dbEvent;
        try {
            dbEvent = eventDAO.update(storedEvent);
        } catch (DAOException e) {
            Logger.error("Create event in update eventStatus", storedEvent.toString(), e.toString());
            jsonResponse.internalServerError();
            return;
        }

        jsonResponse.response(new Gson().toJson(dbEvent));
    }
}
