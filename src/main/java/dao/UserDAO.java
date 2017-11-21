package main.java.dao;

import main.java.models.User;

public interface UserDAO extends CRUD<User> {

    User getByEmail(String email) throws DAOException;
}
