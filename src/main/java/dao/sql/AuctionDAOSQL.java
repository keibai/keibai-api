package main.java.dao.sql;

import main.java.dao.AuctionDAO;
import main.java.dao.DAOException;
import main.java.dao.NotFoundException;
import main.java.db.Source;
import main.java.models.Auction;

import javax.naming.NamingException;
import java.sql.*;
import java.util.LinkedList;
import java.util.List;

public class AuctionDAOSQL extends SQLDAOAbstract<Auction> implements AuctionDAO {

    private static final String DB_ID = "id";
    private static final String DB_NAME = "name";
    private static final String DB_STARTING_PRICE = "starting_price";
    private static final String DB_START_TIME = "start_time";
    private static final String DB_IS_VALID = "is_valid";
    private static final String DB_STATUS = "status";
    private static final String DB_EVENT_ID = "event";
    private static final String DB_OWNER_ID = "owner";
    private static final String DB_WINNER_ID = "winner";

    private static AuctionDAO instance;

    private AuctionDAOSQL() {

    }

    public static AuctionDAO getInstance() {
        if (instance == null) {
            instance = new AuctionDAOSQL();
        }
        return instance;
    }
    
    public Auction create(Auction auction) throws DAOException {
        try {
            Connection connection = Source.getInstance().getConnection();
            String query = "INSERT INTO public.auction (name, starting_price, start_time, is_valid, event, owner, status, winner) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

            PreparedStatement statement = connection.prepareStatement(query, new String[] { "id" });
            statement.setString(1, auction.name);
            statement.setDouble(2, auction.startingPrice);
            statement.setTimestamp(3, auction.startTime);
            statement.setBoolean(4, auction.isValid);
            statement.setInt(5, auction.eventId);
            statement.setInt(6, auction.ownerId);
            statement.setString(7, auction.status);
            if (auction.winnerId == 0) {
                statement.setNull(8, 0);
            } else {
                statement.setInt(8, auction.winnerId);
            }
            statement.executeUpdate();
            return recentlyUpdated(statement);
        } catch (NamingException |SQLException e) {
            throw new DAOException(e);
        }
    }

    public Auction getById(int id) throws DAOException {
        try {
            Connection connection = Source.getInstance().getConnection();
            String query = "SELECT * FROM public.auction WHERE \"auction\".id = ?";

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
    public List<Auction> getListByEventId(int eventId) throws DAOException {
        try {
            Connection connection = Source.getInstance().getConnection();
            String query = "SELECT * FROM public.auction WHERE \"event\" = ?";

            PreparedStatement statement = connection.prepareStatement(query, new String[] { "id" });
            statement.setInt(1, eventId);
            ResultSet resultSet = statement.executeQuery();

            List<Auction> auctionList = new LinkedList<>();
            while (resultSet.next()) {
                auctionList.add(objectFromResultSet(resultSet));
            }

            return auctionList;
        } catch (NamingException|SQLException e) {
            throw new DAOException(e);
        }
    }

    public Auction update(Auction auction) throws DAOException {
        try {
            Connection connection = Source.getInstance().getConnection();
            String query = "UPDATE public.auction " +
                    "SET name = ?, starting_price = ?, " +
                    "start_time = ?, is_valid = ?, event = ?, " +
                    "owner = ?, status = ?, winner = ? " +
                    "WHERE id = ?";

            PreparedStatement statement = connection.prepareStatement(query, new String[] { "id" });
            statement.setString(1, auction.name);
            statement.setDouble(2, auction.startingPrice);
            statement.setTimestamp(3, auction.startTime);
            statement.setBoolean(4, auction.isValid);
            statement.setInt(5, auction.eventId);
            statement.setInt(6, auction.ownerId);
            statement.setString(7, auction.status);
            if (auction.winnerId == 0) {
                statement.setNull(8, 0);
            } else {
                statement.setInt(8, auction.winnerId);
            }
            statement.setInt(9, auction.id);
            statement.executeUpdate();

            return recentlyUpdated(statement);
        } catch (NamingException|SQLException e) {
            throw new DAOException(e);
        }
    }

    public boolean delete(int id) throws DAOException {
        try {
            Connection connection = Source.getInstance().getConnection();
            String query = "DELETE FROM public.auction WHERE id = ?";

            PreparedStatement statement = connection.prepareStatement(query, new String[] { "id" });
            statement.setInt(1, id);
            int nDeleted = statement.executeUpdate();

            return nDeleted != 0;
        } catch (NamingException|SQLException e) {
            throw new DAOException(e);
        }
    }

    @Override
    Auction objectFromResultSet(ResultSet resultSet) throws SQLException {
        Auction auction = new Auction();

        auction.id = resultSet.getInt(DB_ID);
        auction.name = resultSet.getString(DB_NAME);
        auction.startingPrice = resultSet.getDouble(DB_STARTING_PRICE);
        auction.startTime = resultSet.getTimestamp(DB_START_TIME);
        auction.isValid = resultSet.getBoolean(DB_IS_VALID);
        auction.eventId = resultSet.getInt(DB_EVENT_ID);
        auction.ownerId = resultSet.getInt(DB_OWNER_ID);
        auction.status = resultSet.getString(DB_STATUS);
        auction.winnerId = resultSet.getInt(DB_WINNER_ID);
        return auction;
    }
}
