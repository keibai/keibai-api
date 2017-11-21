package main.java.dao;

public interface CRUD<T> {
    T create(T object) throws DAOException;
    T getById(int objectId) throws DAOException;
    T update(T object) throws DAOException;
    boolean delete(int objectId) throws DAOException;
}
