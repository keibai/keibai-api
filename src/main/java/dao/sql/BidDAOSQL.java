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
import java.util.LinkedList;
import java.util.List;

public class BidDAOSQL extends SQLDAOAbstract<Bid> implements BidDAO {

    private static final String DB_ID = "id";
    private static final String DB_AMOUNT = "amount";
    private static final String DB_AUCTION_ID = "auction";
    private static final String DB_OWNER_ID = "owner";
    private static final String DB_CREATED_AT = "created_at";

    private static BidDAO instance;

    private BidDAOSQL() {

    }

    public static BidDAO getInstance() {
        if (instance == null) {
            instance = new BidDAOSQL();
        }
        return instance;
    }

    public Bid create(Bid bid) throws DAOException {
        try {
            Connection connection = Source.getInstance().getConnection();
            String query = "INSERT INTO public.bid (amount, auction, owner) " +
                    "VALUES (?, ?, ?)";

            PreparedStatement statement = connection.prepareStatement(query, new String[]{"id"});
            statement.setDouble(1, bid.amount);
            statement.setInt(2, bid.auctionId);
            statement.setInt(3, bid.ownerId);
            statement.executeUpdate();
            return recentlyUpdated(statement);
        } catch (NamingException | SQLException e) {
            throw new DAOException(e);
        }
    }

    public Bid getById(int id) throws DAOException {
        try {
            Connection connection = Source.getInstance().getConnection();
            String query = "SELECT * FROM public.bid WHERE \"bid\".id = ?";

            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();

            if (!resultSet.next()) {
                return null;
            }

            return objectFromResultSet(resultSet);
        } catch (NamingException | SQLException e) {
            throw new DAOException(e);
        }
    }

    @Override
    public List<Bid> getListByOwnerId(int ownerId) throws DAOException {
        try {
            Connection connection = Source.getInstance().getConnection();
            String query = "SELECT * FROM public.bid WHERE \"owner\" = ?";

            PreparedStatement statement = connection.prepareStatement(query, new String[] { "id" });
            statement.setInt(1, ownerId);
            ResultSet resultSet = statement.executeQuery();

            List<Bid> bidList = new LinkedList<>();
            while (resultSet.next()) {
                bidList.add(objectFromResultSet(resultSet));
            }

            return bidList;
        } catch (NamingException|SQLException e) {
            throw new DAOException(e);
        }
    }

    public Bid update(Bid bid) throws DAOException {
        try {
            Connection connection = Source.getInstance().getConnection();
            String query = "UPDATE public.bid " +
                    "SET amount = ?, auction = ?, " +
                    "owner = ?" +
                    "WHERE id = ?";

            PreparedStatement statement = connection.prepareStatement(query, new String[]{"id"});
            statement.setDouble(1, bid.amount);
            statement.setInt(2, bid.auctionId);
            statement.setInt(3, bid.ownerId);
            statement.setInt(4, bid.id);
            statement.executeUpdate();

            return recentlyUpdated(statement);
        } catch (NamingException | SQLException e) {
            throw new DAOException(e);
        }
    }

    public boolean delete(int id) throws DAOException {
        try {
            Connection connection = Source.getInstance().getConnection();
            String query = "DELETE FROM public.bid WHERE id = ?";

            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, id);
            int nDeleted = statement.executeUpdate();

            return nDeleted != 0;
        } catch (NamingException | SQLException e) {
            throw new DAOException(e);
        }
    }

    @Override
    public Bid objectFromResultSet(ResultSet resultSet) throws SQLException {
        Bid bid = new Bid();

        bid.id = resultSet.getInt(DB_ID);
        bid.amount = resultSet.getDouble(DB_AMOUNT);
        bid.auctionId = resultSet.getInt(DB_AUCTION_ID);
        bid.ownerId = resultSet.getInt(DB_OWNER_ID);
        bid.createdAt = resultSet.getTimestamp(DB_CREATED_AT);
        return bid;
    }
}
