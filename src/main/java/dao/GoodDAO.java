package main.java.dao;

import main.java.models.Good;

public interface GoodDAO {
    // Create
    void createGood(Good Good);

    // Read
    Good getGoodById(int id);

    // Update
    void updateGood(Good Good);

    // Delete
    void deleteGood(int id);
}
