package main.java.dao.sql;

import main.java.dao.DAOException;
import main.java.dao.EventDAO;
import main.java.db.Source;
import main.java.models.Event;

import javax.naming.NamingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class EventDAOSQL implements EventDAO {

    private static EventDAO instance;

    private EventDAOSQL() {

    }

    public static EventDAO getInstance() {
        if (instance == null) {
            instance = new EventDAOSQL();
        }
        return instance;
    }

    public void createEvent(Event event) throws DAOException {
        try {
            Connection connection = Source.getInstance().getConnection();
            String query = "INSERT INTO public.event (name, auction_time, location, auction_type, category, owner) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";

            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, event.getName());
            statement.setInt(2, event.getAuctionTime());
            statement.setString(3, event.getLocation());
            statement.setString(4, event.getAuctionType());
            statement.setString(5, event.getCategory());
            statement.setInt(6, event.getOwner().getId());
            statement.execute();
        } catch (NamingException|SQLException e) {
            throw new DAOException(e);
        }
    }

    public Event getEventById(int id) {
        return null;
    }

    public void updateEvent(Event Event) {

    }

    public void deleteEvent(int id) {

    }
}
