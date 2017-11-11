package main.java.dao;

import main.java.models.Bid;

public interface BidDAO {
    // Create
    void createBid(Bid Bid);

    // Read
    Bid getBidById(int id);

    // Update
    void updateBid(Bid Bid);

    // Delete
    void deleteBid(int id);
}
