package main.java.dao;

import main.java.models.User;

public interface UserDAO {

    // Create
    User create(User user) throws DAOException;

    // Read
    User getById(int id) throws DAOException;
    User getByEmail(String email) throws DAOException;

    // Update
    User update(User user) throws DAOException;

    // Delete
    boolean delete(int id) throws DAOException;
}
