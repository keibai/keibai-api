package main.java.dao.sql;

import main.java.dao.DAOException;
import main.java.dao.EventDAO;
import main.java.db.Source;
import main.java.models.Event;

import javax.naming.NamingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

public class EventDAOSQL extends SQLDAOAbstract<Event> implements EventDAO {

    private static final String DB_ID = "id";
    private static final String DB_NAME = "name";
    private static final String DB_AUCTION_TIME = "auction_time";
    private static final String DB_LOCATION = "location";
    private static final String DB_AUCTION_TYPE = "auction_type";
    private static final String DB_CATEGORY = "category";
    private static final String DB_OWNER_ID = "owner";
    private static final String DB_CREATED_AT = "created_at";
    private static final String DB_UPDATED_AT = "updated_at";
    private static final String DB_STATUS = "status";

    private static EventDAO instance;

    private EventDAOSQL() {

    }

    public static EventDAO getInstance() {
        if (instance == null) {
            instance = new EventDAOSQL();
        }
        return instance;
    }

    public Event create(Event event) throws DAOException {
        try {
            Connection connection = Source.getInstance().getConnection();
            String query = "INSERT INTO public.event (name, auction_time, location, auction_type, category, owner, status) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";

            PreparedStatement statement = connection.prepareStatement(query, new String[] { "id" });
            statement.setString(1, event.name);
            statement.setInt(2, event.auctionTime);
            statement.setString(3, event.location);
            statement.setString(4, event.auctionType);
            statement.setString(5, event.category);
            statement.setInt(6, event.ownerId);
            statement.setString(7, event.status);
            statement.executeUpdate();
            return recentlyUpdated(statement);
        } catch (NamingException|SQLException e) {
            throw new DAOException(e);
        }
    }

    public Event getById(int id) throws DAOException {
        try {
            Connection connection = Source.getInstance().getConnection();
            String query = "SELECT * FROM public.event WHERE \"event\".id = ?";

            PreparedStatement statement = connection.prepareStatement(query, new String[] { "id" });
            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();

            if (!resultSet.next()) {
                return null;
            }

            return objectFromResultSet(resultSet);
        } catch (NamingException|SQLException e) {
            throw new DAOException(e);
        }
    }

    @Override
    public List<Event> getList() throws DAOException {
        try {
            Connection connection = Source.getInstance().getConnection();
            String query = "SELECT * FROM public.event";

            PreparedStatement statement = connection.prepareStatement(query, new String[] { "id" });
            ResultSet resultSet = statement.executeQuery();

            List<Event> eventList = new LinkedList<>();
            while (resultSet.next()) {
                eventList.add(objectFromResultSet(resultSet));
            }

            return eventList;
        } catch (NamingException|SQLException e) {
            throw new DAOException(e);
        }
    }

    public Event update(Event event) throws DAOException {
        try {
            Connection connection = Source.getInstance().getConnection();
            String query = "UPDATE public.event " +
                    "SET name = ?, auction_time = ?, " +
                    "location = ?, auction_type = ?, category = ?, " +
                    "owner = ?, status = ? " +
                    "WHERE id = ?";

            PreparedStatement statement = connection.prepareStatement(query, new String[] { "id" });
            statement.setString(1, event.name);
            statement.setInt(2, event.auctionTime);
            statement.setString(3, event.location);
            statement.setString(4, event.auctionType);
            statement.setString(5, event.category);
            statement.setInt(6, event.ownerId);
            statement.setString(7, event.status);
            statement.setInt(8, event.id);
            statement.executeUpdate();

            return recentlyUpdated(statement);
        } catch (NamingException|SQLException e) {
            throw new DAOException(e);
        }
    }

    public boolean delete(int id) throws DAOException {
        try {
            Connection connection = Source.getInstance().getConnection();
            String query = "DELETE FROM public.event WHERE id = ?";

            PreparedStatement statement = connection.prepareStatement(query, new String[] { "id" });
            statement.setInt(1, id);
            int nDeleted = statement.executeUpdate();

            return nDeleted != 0;
        } catch (NamingException|SQLException e) {
            throw new DAOException(e);
        }
    }

    @Override
    Event objectFromResultSet(ResultSet resultSet) throws SQLException {
        Event event = new Event();
        event.id = resultSet.getInt(DB_ID);
        event.name = resultSet.getString(DB_NAME);
        event.auctionTime = resultSet.getInt(DB_AUCTION_TIME);
        event.location = resultSet.getString(DB_LOCATION);
        event.auctionType = resultSet.getString(DB_AUCTION_TYPE);
        event.category = resultSet.getString(DB_CATEGORY);
        event.ownerId = resultSet.getInt(DB_OWNER_ID);
        event.createdAt = resultSet.getTimestamp(DB_CREATED_AT);
        event.updatedAt = resultSet.getTimestamp(DB_UPDATED_AT);
        event.status = resultSet.getString(DB_STATUS);
        return event;
    }
}
