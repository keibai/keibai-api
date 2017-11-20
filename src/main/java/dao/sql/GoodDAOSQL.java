package main.java.dao.sql;

import main.java.dao.GoodDAO;
import main.java.dao.DAOException;
import main.java.dao.NotFoundException;
import main.java.db.Source;
import main.java.models.Good;

import javax.naming.NamingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class GoodDAOSQL implements GoodDAO {

    private static final String DB_ID = "id";
    private static final String DB_NAME = "name";
    private static final String DB_IMAGE = "image";
    private static final String DB_AUCTION_ID = "auction";

    private static GoodDAO instance;

    private GoodDAOSQL() {

    }

    public static GoodDAO getInstance() {
        if (instance == null) {
            instance = new GoodDAOSQL();
        }
        return instance;
    }

    public void createGood(Good good) throws DAOException {
        try {
            Connection connection = Source.getInstance().getConnection();
            String query = "INSERT INTO public.good (name, image, auction) " +
                    "VALUES (?, ?, ?)";

            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, good.name);
            statement.setBytes(2, good.image.getBytes());
            statement.setInt(3, good.auctionId);
            statement.execute();
        } catch (NamingException |SQLException e) {
            throw new DAOException(e);
        }
    }

    public Good getGoodById(int id) throws DAOException, NotFoundException {
        try {
            Connection connection = Source.getInstance().getConnection();
            String query = "SELECT * FROM public.good WHERE \"good\".id = ?";

            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();

            if (!resultSet.next()) {
                throw new NotFoundException("Good not found");
            }

            return createGoodFromResultSet(resultSet);
        } catch (NamingException|SQLException e) {
            throw new DAOException(e);
        }
    }

    public void updateGood(Good good) throws DAOException, NotFoundException {
        try {
            Connection connection = Source.getInstance().getConnection();
            String query = "UPDATE public.good " +
                    "SET name = ?, image = ?, " +
                    "auction = ?" +
                    "WHERE id = ?";

            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, good.name);
            statement.setString(2, good.image);
            statement.setInt(3, good.auctionId);
            int nUpdated = statement.executeUpdate();

            if (nUpdated == 0) {
                throw new NotFoundException("Good not found");
            }
        } catch (NamingException|SQLException e) {
            throw new DAOException(e);
        }
    }

    public void deleteGood(int id) throws DAOException, NotFoundException {
        try {
            Connection connection = Source.getInstance().getConnection();
            String query = "DELETE FROM public.good WHERE id = ?";

            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, id);
            int nDeleted = statement.executeUpdate();

            if (nDeleted == 0) {
                throw new NotFoundException("Good not found");
            }
        } catch (NamingException|SQLException e) {
            throw new DAOException(e);
        }
    }

    private Good createGoodFromResultSet(ResultSet resultSet) throws SQLException {
        Good good = new Good();

        good.id = resultSet.getInt(DB_ID);
        good.name = resultSet.getString(DB_NAME);
        good.image = new String(resultSet.getBytes(DB_IMAGE));
        good.auctionId = resultSet.getInt(DB_AUCTION_ID);
        return good;
    }
}
