package main.java.dao;

import main.java.models.Event;

public interface EventDAO {
    // Create
    void createEvent(Event Event) throws DAOException;

    // Read
    Event getEventById(int id) throws DAOException, NotFoundException;

    // Update
    void updateEvent(Event Event) throws DAOException, NotFoundException;

    // Delete
    void deleteEvent(int id) throws DAOException, NotFoundException;
}
