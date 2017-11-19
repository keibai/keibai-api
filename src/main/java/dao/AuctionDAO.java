package main.java.dao;

import main.java.models.Auction;

public interface AuctionDAO {
    // Create
    void createAuction(Auction Auction) throws DAOException;

    // Read
    Auction getAuctionById(int id) throws DAOException, NotFoundException;

    // Update
    void updateAuction(Auction Auction) throws DAOException, NotFoundException;

    // Delete
    void deleteAuction(int id) throws DAOException, NotFoundException;
}
