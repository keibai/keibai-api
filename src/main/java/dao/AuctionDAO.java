package main.java.dao;

import main.java.models.Auction;

import java.util.List;

public interface AuctionDAO extends CRUD<Auction> {

    List<Auction> getListByEventId(int eventId) throws DAOException;

    List<Auction> getListByWinnerId(int winnerId) throws DAOException;

}
