package main.java.dao;

import main.java.models.User;

public interface UserDAO {

    // Create
    void createUser(User user) throws DAOException;

    // Read
    User getUserById(int id) throws DAOException, NotFoundException;
    User getUserByEmail(String email) throws NotFoundException, DAOException;

    // Update
    void updateUser(User user) throws DAOException, NotFoundException;

    // Delete
    void deleteUser(int id);
}
