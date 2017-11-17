package main.java.dao;

import main.java.models.Event;

public interface EventDAO {
    // Create
    void createEvent(Event Event) throws DAOException;

    // Read
    Event getEventById(int id);

    // Update
    void updateEvent(Event Event);

    // Delete
    void deleteEvent(int id);
}
