package main.java.dao.sql;

import main.java.dao.BidDAO;
import main.java.dao.DAOException;
import main.java.dao.NotFoundException;
import main.java.db.Source;
import main.java.models.*;
import main.java.models.Bid;

import javax.naming.NamingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BidDAOSQL implements BidDAO {

    private static final String DB_ID = "id";
    private static final String DB_AMOUNT = "amount";

    private static BidDAO instance;

    private BidDAOSQL() {

    }

    public static BidDAO getInstance() {
        if (instance == null) {
            instance = new BidDAOSQL();
        }
        return instance;
    }

    public void createBid(Bid bid) throws DAOException {
        try {
            Connection connection = Source.getInstance().getConnection();
            String query = "INSERT INTO public.bid (amount, auction, owner) " +
                    "VALUES (?, ?, ?)";

            PreparedStatement statement = connection.prepareStatement(query);
            statement.setDouble(1, bid.getAmount());
            statement.setInt(2, bid.getAuction().getId());
            statement.setInt(3, bid.getOwner().id);
            statement.execute();
        } catch (NamingException |SQLException e) {
            throw new DAOException(e);
        }
    }

    public Bid getBidById(int id) throws DAOException, NotFoundException {
        try {
            Connection connection = Source.getInstance().getConnection();
            String query = "SELECT * FROM public.bid WHERE \"bid\".id = ?";

            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();

            if (!resultSet.next()) {
                throw new NotFoundException("Bid not found");
            }

            return createBidFromResultSet(resultSet);
        } catch (NamingException|SQLException e) {
            throw new DAOException(e);
        }
    }

    public void updateBid(Bid bid) throws DAOException, NotFoundException {
        try {
            Connection connection = Source.getInstance().getConnection();
            String query = "UPDATE public.bid " +
                    "SET amount = ?, auction = ?, " +
                    "owner = ?" +
                    "WHERE id = ?";

            PreparedStatement statement = connection.prepareStatement(query);
            statement.setDouble(1, bid.getAmount());
            statement.setInt(2, bid.getAuction().getId());
            statement.setInt(3, bid.getOwner().id);
            int nUpdated = statement.executeUpdate();

            if (nUpdated == 0) {
                throw new NotFoundException("Bid not found");
            }
        } catch (NamingException|SQLException e) {
            throw new DAOException(e);
        }
    }

    public void deleteBid(int id) throws DAOException, NotFoundException {
        try {
            Connection connection = Source.getInstance().getConnection();
            String query = "DELETE FROM public.bid WHERE id = ?";

            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, id);
            int nDeleted = statement.executeUpdate();

            if (nDeleted == 0) {
                throw new NotFoundException("Bid not found");
            }
        } catch (NamingException|SQLException e) {
            throw new DAOException(e);
        }
    }

    private Bid createBidFromResultSet(ResultSet resultSet) throws SQLException {
        User owner = new User();
        Auction auction = new Auction();
        Bid bid = new Bid();

        bid.setId(resultSet.getInt(DB_ID));
        bid.setAmount(resultSet.getDouble(DB_AMOUNT));
        bid.setAuction(auction);
        bid.setOwner(owner);
        return bid;
    }
}
