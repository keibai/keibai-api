package main.java.dao.sql;

import main.java.dao.UserDAO;
import main.java.db.Source;
import main.java.models.User;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

public class UserDAOSQL implements UserDAO {

    private static UserDAO instance = null;
    private Connection connection;

    public static UserDAO getInstance() {
        if (instance == null) {
            instance = new UserDAOSQL();
        }
        return instance;
    }

    private UserDAOSQL() {
        Source source = new Source();
        this.connection = source.getConnection();
    }


    public void createUser(User user) throws SQLException {
        String sql = "INSERT INTO public.\"user\" (name, last_name, password, email, country," +
                "city, address, zip_code, credit, created_at, updated_at)\n" +
                "VALUES (\"" + user.getName() + "\", \"" + user.getLastName() + "\", \"" +
                user.getPassword() + "\", \"" + user.getEmail() + "\", \"" + user.getCountry() + "\", \"" +
                user.getCity() + "\", \"" + user.getAddress() + "\", \"" + user.getZipCode() + "\", \"" +
                user.getCredit() + "\", \"" + new Timestamp(user.getCreatedAt().getTimeInMillis()) + "\", \"" +
                new Timestamp(user.getUpdatedAt().getTimeInMillis()) + "\");";
        System.out.println(sql);
        Statement stmt = connection.createStatement();
        stmt.execute(sql);
    }

    public User getUserById(int id) {
        return null;
    }

    public User getUserByEmail(String email) {
        return null;
    }

    public void updateUser(User user) {

    }

    public void deleteUser(int id) {
        throw new UnsupportedOperationException("Not implemented yet!");
    }
}
