package main.java.dao;

import main.java.models.Good;

public interface GoodDAO {
    // Create
    void createGood(Good Good) throws DAOException;

    // Read
    Good getGoodById(int id) throws DAOException, NotFoundException;

    // Update
    void updateGood(Good Good) throws DAOException, NotFoundException;

    // Delete
    void deleteGood(int id) throws DAOException, NotFoundException;
}
