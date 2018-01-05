package main.java.dao;

import main.java.models.Good;

import java.util.List;

public interface GoodDAO extends CRUD<Good> {
    List<Good> getListByAuctionId(int auctionId) throws DAOException;
}
