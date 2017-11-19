package main.java.dao.sql;

import main.java.dao.DAOException;
import main.java.dao.EventDAO;
import main.java.dao.NotFoundException;
import main.java.dao.sql.models.EventSQL;
import main.java.dao.sql.models.UserSQL;
import main.java.db.Source;
import main.java.models.Event;
import main.java.models.User;

import javax.naming.NamingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class EventDAOSQL implements EventDAO {

    public static final String DB_ID = "id";
    public static final String DB_NAME = "name";
    public static final Integer DB_AUCTION_TIME = 100;
    public static final String DB_LOCATION = "location";
    public static final String DB_AUCTION_TYPE = "auction_type";
    public static final String DB_CATEGORY = "caegory";
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

    public Event getEventById(int id) throws DAOException, NotFoundException {
        try {
            Connection connection = Source.getInstance().getConnection();
            String query = "SELECT * FROM public.event WHERE \"event\".id = ?";

            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();

            if (!resultSet.next()) {
                throw new NotFoundException("Event not found");
            }

            return createEventFromResultSet(resultSet);
        } catch (NamingException|SQLException e) {
            throw new DAOException(e);
        }
    }

    public void updateEvent(Event event) throws DAOException, NotFoundException {
        try {
            Connection connection = Source.getInstance().getConnection();
            String query = "UPDATE public.event " +
                    "SET name = ?, auction_time = ?, " +
                    "location = ?, auction_type = ?, category = ?, " +
                    "owner = ? " +
                    "WHERE id = ?";

            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, event.getName());
            statement.setInt(2, event.getAuctionTime());
            statement.setString(3, event.getLocation());
            statement.setString(4, event.getAuctionType());
            statement.setString(5, event.getCategory());
            statement.setInt(6, event.getOwner().getId());
            int nUpdated = statement.executeUpdate();

            if (nUpdated == 0) {
                throw new NotFoundException("Event not found");
            }
        } catch (NamingException|SQLException e) {
            throw new DAOException(e);
        }
    }

    public void deleteEvent(int id) throws DAOException, NotFoundException {
        try {
            Connection connection = Source.getInstance().getConnection();
            String query = "DELETE FROM public.event WHERE id = ?";

            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, id);
            int nDeleted = statement.executeUpdate();

            if (nDeleted == 0) {
                throw new NotFoundException("Event not found");
            }
        } catch (NamingException|SQLException e) {
            throw new DAOException(e);
        }
    }

    private Event createEventFromResultSet(ResultSet resultSet) throws SQLException {
        User user = new UserSQL();
        Event event = new EventSQL();
        event.setId(resultSet.getInt(DB_ID));
        event.setName(resultSet.getString(DB_NAME));
        event.setAuctionTime(resultSet.getInt(DB_AUCTION_TIME));
        event.setLocation(resultSet.getString(DB_LOCATION));
        event.setAuctionType(resultSet.getString(DB_AUCTION_TYPE));
        event.setCategory(resultSet.getString(DB_CATEGORY));
        event.setOwner(user);
        return event;
    }
}
