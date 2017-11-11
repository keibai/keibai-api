package main.java.dao;

import main.java.models.AuctionStatus;

public interface AuctionStatusDAO {
    // Create
    void createAuctionStatus(AuctionStatus AuctionStatus);

    // Read
    AuctionStatus getAuctionStatusById(int id);
    AuctionStatus getAuctionStatusByName(String name);

    // Update
    void updateAuctionStatus(AuctionStatus AuctionStatus);

    // Delete
    void deleteAuctionStatus(int id);
}
