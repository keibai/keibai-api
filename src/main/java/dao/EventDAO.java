package main.java.dao;

import main.java.models.Event;
import main.java.models.meta.ModelList;

public interface EventDAO extends CRUD<Event> {

    ModelList<Event> getList() throws DAOException;

}
