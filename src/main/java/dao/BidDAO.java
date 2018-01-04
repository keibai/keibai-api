package main.java.dao;

import main.java.models.Bid;

import java.util.List;

public interface BidDAO extends CRUD<Bid> {

    List<Bid> getListByOwnerId(int userId) throws DAOException;
    List<Bid> getListByAuctionId(int auctionId) throws DAOException;
}
