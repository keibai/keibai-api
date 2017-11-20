package main.java.dao.sql;

import main.java.dao.AuctionDAO;
import main.java.dao.DAOException;
import main.java.dao.NotFoundException;
import main.java.db.Source;
import main.java.models.Auction;
import main.java.models.Event;
import main.java.models.User;

import javax.naming.NamingException;
import java.sql.*;

public class AuctionDAOSQL implements AuctionDAO {

    private static final String DB_ID = "id";
    private static final String DB_NAME = "name";
    private static final String DB_STARTING_PRICE = "starting_price";
    private static final String DB_START_TIME = "start_time";
    private static final String DB_IS_VALID = "is_valid";
    private static final String DB_STATUS = "status";

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
            statement.setString(1, auction.getName());
            statement.setDouble(2, auction.getStartingPrice());
            statement.setTimestamp(3, auction.getStartTime());
            statement.setBoolean(4, auction.isValid());
            statement.setInt(5, auction.getEvent().getId());
            statement.setInt(6, auction.getOwner().id);
            statement.setString(7, auction.getStatus());
            statement.setInt(8, auction.getWinner().id);
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
            statement.setString(1, auction.getName());
            statement.setDouble(2, auction.getStartingPrice());
            statement.setTimestamp(3, auction.getStartTime());
            statement.setBoolean(4, auction.isValid());
            statement.setInt(5, auction.getEvent().getId());
            statement.setInt(6, auction.getOwner().id);
            statement.setString(7, auction.getStatus());
            statement.setInt(8, auction.getWinner().id);
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
        User user = new User();
        User winner = new User();
        Event event = new Event();
        Auction auction = new Auction();

        auction.setId(resultSet.getInt(DB_ID));
        auction.setName(resultSet.getString(DB_NAME));
        auction.setStartingPrice(resultSet.getDouble(String.valueOf(DB_STARTING_PRICE)));
        auction.setStartTime(resultSet.getTimestamp(DB_START_TIME));
        auction.setValid(resultSet.getBoolean(String.valueOf(DB_IS_VALID)));
        auction.setEvent(event);
        auction.setOwner(user);
        auction.setStatus(resultSet.getString(DB_STATUS));
        auction.setWinner(winner);
        return auction;
    }
}
