package main.java.utils;

import main.java.dao.DAOException;
import main.java.dao.UserDAO;
import main.java.dao.sql.UserDAOSQL;
import main.java.models.User;

public class DBFeeder {

    public static User createDummyUser() throws DAOException {
        User dummyUser = DummyGenerator.getDummyUser();
        // Hash password like if he had really signed up.
        dummyUser.password = new PasswordAuthentication().hash(dummyUser.password.toCharArray());

        UserDAO userDAO = UserDAOSQL.getInstance();
        User user = userDAO.create(dummyUser);
        return user;
    }
}
