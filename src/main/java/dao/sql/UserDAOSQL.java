package main.java.dao.sql;

import main.java.dao.DAOException;
import main.java.dao.UserDAO;
import main.java.dao.sql.models.UserSQL;
import main.java.db.Source;
import main.java.models.User;

import javax.naming.NamingException;
import java.sql.*;

public class UserDAOSQL implements UserDAO {

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
            String query = "INSERT INTO public.user(name, last_name, password, email) VALUES(?, ?, ?, ?)";

            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, user.getName());
            statement.setString(2, user.getLastName());
            statement.setString(3, user.getPassword());
            statement.setString(4, user.getEmail());
            statement.execute();
        } catch (NamingException|SQLException e) {
            throw new DAOException(e);
        }
    }

    public UserSQL getUserById(int id) {
        throw new UnsupportedOperationException();
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
