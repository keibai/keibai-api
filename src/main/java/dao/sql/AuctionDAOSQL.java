package main.java.dao.sql;

import main.java.dao.AuctionDAO;
import main.java.dao.DAOException;
import main.java.dao.NotFoundException;
import main.java.db.Source;
import main.java.models.Auction;

import javax.naming.NamingException;
import java.sql.*;

public class AuctionDAOSQL implements AuctionDAO {

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
    
    public void createAuction(Auction auction) throws DAOException {
        try {
            Connection connection = Source.getInstance().getConnection();
            String query = "INSERT INTO public.auction (name, starting_price, start_time, is_valid, event, owner, status, winner) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, auction.name);
            statement.setDouble(2, auction.startingPrice);
            statement.setTimestamp(3, auction.startTime);
            statement.setBoolean(4, auction.isValid);
            statement.setInt(5, auction.eventId);
            statement.setInt(6, auction.ownerId);
            statement.setString(7, auction.status);
            statement.setInt(8, auction.winnerId);
            statement.execute();
        } catch (NamingException |SQLException e) {
            throw new DAOException(e);
        }
    }

    public Auction getAuctionById(int id) throws DAOException, NotFoundException {
        try {
            Connection connection = Source.getInstance().getConnection();
            String query = "SELECT * FROM public.auction WHERE \"auction\".id = ?";

            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();

            if (!resultSet.next()) {
                throw new NotFoundException("Auction not found");
            }

            return createAuctionFromResultSet(resultSet);
        } catch (NamingException|SQLException e) {
            throw new DAOException(e);
        }
    }

    public void updateAuction(Auction auction) throws DAOException, NotFoundException {
        try {
            Connection connection = Source.getInstance().getConnection();
            String query = "UPDATE public.auction " +
                    "SET name = ?, starting_price = ?, " +
                    "start_time = ?, is_valid = ?, event = ?, " +
                    "owner = ?, status = ?, winner = ? " +
                    "WHERE id = ?";

            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, auction.name);
            statement.setDouble(2, auction.startingPrice);
            statement.setTimestamp(3, auction.startTime);
            statement.setBoolean(4, auction.isValid);
            statement.setInt(5, auction.eventId);
            statement.setInt(6, auction.ownerId);
            statement.setString(7, auction.status);
            statement.setInt(8, auction.winnerId);
            int nUpdated = statement.executeUpdate();

            if (nUpdated == 0) {
                throw new NotFoundException("Auction not found");
            }
        } catch (NamingException|SQLException e) {
            throw new DAOException(e);
        }
    }

    public void deleteAuction(int id) throws DAOException, NotFoundException {
        try {
            Connection connection = Source.getInstance().getConnection();
            String query = "DELETE FROM public.auction WHERE id = ?";

            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, id);
            int nDeleted = statement.executeUpdate();

            if (nDeleted == 0) {
                throw new NotFoundException("Auction not found");
            }
        } catch (NamingException|SQLException e) {
            throw new DAOException(e);
        }
    }

    private Auction createAuctionFromResultSet(ResultSet resultSet) throws SQLException {
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
