package main.java.dao.sql;

import main.java.dao.GoodDAO;
import main.java.dao.DAOException;
import main.java.db.Source;
import main.java.models.Good;

import javax.naming.NamingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

public class GoodDAOSQL extends SQLDAOAbstract<Good> implements GoodDAO {

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

    public Good create(Good good) throws DAOException {
        try {
            Connection connection = Source.getInstance().getConnection();
            String query = "INSERT INTO public.good (name, image, auction) " +
                    "VALUES (?, ?, ?)";

            PreparedStatement statement = connection.prepareStatement(query, new String[] { "id" });
            statement.setString(1, good.name);
            statement.setBytes(2, good.image.getBytes());
            statement.setInt(3, good.auctionId);
            statement.executeUpdate();
            return recentlyUpdated(statement);
        } catch (NamingException |SQLException e) {
            throw new DAOException(e);
        }
    }

    public Good getById(int id) throws DAOException {
        try {
            Connection connection = Source.getInstance().getConnection();
            String query = "SELECT * FROM public.good WHERE \"good\".id = ?";

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
    public List<Good> getListByAuctionId(int auctionId) throws DAOException {
        try {
            Connection connection = Source.getInstance().getConnection();
            String query = "SELECT * FROM public.good WHERE \"auction\" = ?";

            PreparedStatement statement = connection.prepareStatement(query, new String[] { "id" });
            statement.setInt(1, auctionId);
            ResultSet resultSet = statement.executeQuery();

            List<Good> goodList = new LinkedList<>();
            while (resultSet.next()) {
                goodList.add(objectFromResultSet(resultSet));
            }

            return goodList;
        } catch (NamingException|SQLException e) {
            throw new DAOException(e);
        }
    }

    public Good update(Good good) throws DAOException {
        try {
            Connection connection = Source.getInstance().getConnection();
            String query = "UPDATE public.good " +
                    "SET name = ?, image = ?, " +
                    "auction = ?" +
                    "WHERE id = ?";

            PreparedStatement statement = connection.prepareStatement(query, new String[] { "id" });
            statement.setString(1, good.name);
            statement.setBytes(2, good.image.getBytes());
            statement.setInt(3, good.auctionId);
            statement.setInt(4,good.id);
            statement.executeUpdate();

            return recentlyUpdated(statement);
        } catch (NamingException|SQLException e) {
            throw new DAOException(e);
        }
    }

    public boolean delete(int id) throws DAOException {
        try {
            Connection connection = Source.getInstance().getConnection();
            String query = "DELETE FROM public.good WHERE id = ?";

            PreparedStatement statement = connection.prepareStatement(query, new String[] { "id" });
            statement.setInt(1, id);
            int nDeleted = statement.executeUpdate();

            return nDeleted != 0;
        } catch (NamingException|SQLException e) {
            throw new DAOException(e);
        }
    }

    @Override
    Good objectFromResultSet(ResultSet resultSet) throws SQLException {
        Good good = new Good();

        good.id = resultSet.getInt(DB_ID);
        good.name = resultSet.getString(DB_NAME);
        good.image = new String(resultSet.getBytes(DB_IMAGE));
        good.auctionId = resultSet.getInt(DB_AUCTION_ID);
        return good;
    }
}
