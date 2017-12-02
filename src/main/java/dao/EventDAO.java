package main.java.dao;

import main.java.models.Event;

import java.util.List;

public interface EventDAO extends CRUD<Event> {

    List<Event> getList() throws DAOException;

}
