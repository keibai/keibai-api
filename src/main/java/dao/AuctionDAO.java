package main.java.dao;

import main.java.models.Auction;

public interface AuctionDAO {
    // Create
    void createAuction(Auction Auction);

    // Read
    Auction getAuctionById(int id);

    // Update
    void updateAuction(Auction Auction);

    // Delete
    void deleteAuction(int id);
}
