package main.java.servlets.event;

import com.google.gson.Gson;
import main.java.dao.DAOException;
import main.java.dao.EventDAO;
import main.java.dao.sql.EventDAOSQL;
import main.java.models.Event;
import main.java.utils.HttpResponse;
import main.java.utils.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet(name = "EventList", urlPatterns = {"/events/list"})
public class EventList extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpResponse httpResponse = new HttpResponse(response);
        EventDAO eventDAO = EventDAOSQL.getInstance();

        List<Event> dbEvents;
        try {
            dbEvents = eventDAO.getList();
        } catch (DAOException e) {
            Logger.error("Get event list", e.toString());
            return;
        }

        httpResponse.response(new Gson().toJson(dbEvents.toArray()));
    }
}
