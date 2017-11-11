package main.java.dao;

import main.java.models.AuctionType;

public interface AuctionTypeDAO {
    // Create
    void createAuctionType(AuctionType AuctionType);

    // Read
    AuctionType getAuctionTypeById(int id);
    AuctionType getAuctionTypeByName(String name);

    // Update
    void updateAuctionType(AuctionType AuctionType);

    // Delete
    void deleteAuctionType(int id);
}
