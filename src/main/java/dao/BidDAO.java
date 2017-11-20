package main.java.dao;

import main.java.models.Bid;

public interface BidDAO {
    // Create
    void createBid(Bid Bid) throws DAOException;

    // Read
    Bid getBidById(int id) throws DAOException, NotFoundException;

    // Update
    void updateBid(Bid Bid) throws DAOException, NotFoundException;

    // Delete
    void deleteBid(int id) throws DAOException, NotFoundException;
}
