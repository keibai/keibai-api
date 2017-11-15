package main.java.dao;

import main.java.models.User;

public interface UserDAO {

    // Create
    void createUser(User user) throws DAOException;

    // Read
    User getUserById(int id) throws DAOException, NotFoundException;
    User getUserByEmail(String email);

    // Update
    void updateUser(User user);

    // Delete
    void deleteUser(int id);
}
