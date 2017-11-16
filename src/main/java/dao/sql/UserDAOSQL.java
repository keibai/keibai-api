package main.java.dao.sql;

import main.java.dao.DAOException;
import main.java.dao.NotFoundException;
import main.java.dao.UserDAO;
import main.java.dao.sql.models.UserSQL;
import main.java.db.Source;
import main.java.models.User;

import javax.naming.NamingException;
import java.sql.*;

public class UserDAOSQL implements UserDAO {

    public static final String DB_ID = "id";
    public static final String DB_NAME = "name";
    public static final String DB_LAST_NAME = "last_name";
    public static final String DB_PASSWORD = "password";
    public static final String DB_EMAIL = "email";
    public static final String DB_CREDIT = "credit";
    public static final String DB_CREATED_AT = "created_at";
    public static final String DB_UPDATED_AT = "updated_at";

    private static UserDAO instance;

    public static UserDAO getInstance() {
        if (instance == null) {
            instance = new UserDAOSQL();
        }
        return instance;
    }

    public void createUser(User user) throws DAOException {
        try {
            Connection connection = Source.getInstance().getConnection();
            String query = "INSERT INTO public.user (name, last_name, password, email) VALUES (?, ?, ?, ?)";

            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, user.getName());
            statement.setString(2, user.getLastName());
            statement.setString(3, user.getPassword());  // TODO: Hash password.
            statement.setString(4, user.getEmail());
            statement.execute();
        } catch (NamingException|SQLException e) {
            throw new DAOException(e);
        }
    }

    public User getUserById(int id) throws DAOException, NotFoundException {
        try {
            Connection connection = Source.getInstance().getConnection();
            String query = "SELECT * FROM public.user WHERE \"user\".id = ?";

            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();

            if (!resultSet.next()) {
                throw new NotFoundException("User not found");
            }

            return createUserFromResultSet(resultSet);
        } catch (NamingException|SQLException e) {
            throw new DAOException(e);
        }
    }

    public User getUserByEmail(String email) throws NotFoundException, DAOException {
        try {
            Connection connection = Source.getInstance().getConnection();
            String query = "SELECT * FROM public.user WHERE \"user\".email = ?";

            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, email);
            ResultSet resultSet = statement.executeQuery();

            if (!resultSet.next()) {
                throw new NotFoundException("User not found");
            }

            return createUserFromResultSet(resultSet);
        } catch (NamingException|SQLException e) {
            throw new DAOException(e);
        }
    }

    public void updateUser(User user) throws DAOException, NotFoundException {
        try {
            Connection connection = Source.getInstance().getConnection();
            String query = "UPDATE public.user " +
                    "SET name = ?, last_name = ?, " +
                    "password = ?, email = ?, credit = ? " +
                    "WHERE id = ?";

            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, user.getName());
            statement.setString(2, user.getLastName());
            statement.setString(3, user.getPassword());
            statement.setString(4, user.getEmail());
            statement.setDouble(5, user.getCredit());
            statement.setInt(6, user.getId());
            int nUpdated = statement.executeUpdate();

            if (nUpdated == 0) {
                throw new NotFoundException("User not found");
            }
        } catch (NamingException|SQLException e) {
            throw new DAOException(e);
        }
    }

    public void deleteUser(int id) {
        throw new UnsupportedOperationException();
    }
    
    private User createUserFromResultSet(ResultSet resultSet) throws SQLException {
        User user = new UserSQL();
        user.setId(resultSet.getInt(DB_ID));
        user.setName(resultSet.getString(DB_NAME));
        user.setLastName(resultSet.getString(DB_LAST_NAME));
        user.setPassword(resultSet.getString(DB_PASSWORD));
        user.setEmail(resultSet.getString(DB_EMAIL));
        user.setCredit(resultSet.getFloat(DB_CREDIT));
        user.setCreatedAt(resultSet.getTimestamp(DB_CREATED_AT));
        user.setUpdatedAt(resultSet.getTimestamp(DB_UPDATED_AT));
        return user;
    }
}
