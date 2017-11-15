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
            String query = "INSERT INTO public.user (name, password, email) VALUES (?, ?, ?)";

            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, user.getName());
            statement.setString(2, user.getPassword()); // TODO: Hash password.
            statement.setString(3, user.getEmail());
            statement.execute();
        } catch (NamingException|SQLException e) {
            throw new DAOException(e);
        }
    }

    public UserSQL getUserById(int id) throws DAOException, NotFoundException {
        try {
            Connection connection = Source.getInstance().getConnection();
            String query = "SELECT * FROM public.user WHERE \"user\".id = ?";

            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();

            if (!resultSet.next()) {
                throw new NotFoundException("User not found");
            }

            UserSQL user = new UserSQL();
            user.setName(resultSet.getString(DB_NAME));
            user.setLastName(resultSet.getString(DB_LAST_NAME));
            user.setPassword(resultSet.getString(DB_PASSWORD));
            user.setEmail(resultSet.getString(DB_EMAIL));
            user.setCredit(resultSet.getFloat(DB_CREDIT));
            user.setCreatedAt(resultSet.getTimestamp(DB_CREATED_AT));
            user.setUpdatedAt(resultSet.getTimestamp(DB_UPDATED_AT));
            return user;
        } catch (NamingException|SQLException e) {
            throw new DAOException(e);
        }
    }

    public UserSQL getUserByEmail(String email) {
        throw new UnsupportedOperationException();
    }

    public void updateUser(User user) {
        throw new UnsupportedOperationException();
    }

    public void deleteUser(int id) {
        throw new UnsupportedOperationException();
    }
}
