package main.java.servlets.event;

import com.google.gson.Gson;
import main.java.dao.sql.AbstractDBTest;
import main.java.mocks.HttpServletStubber;
import main.java.models.Event;
import main.java.models.meta.Error;
import main.java.utils.DBFeeder;
import main.java.utils.ImpreciseDate;
import org.junit.Test;

import static org.junit.Assert.*;

public class EventSearchTest extends AbstractDBTest {

    @Test
    public void should_return_event_does_not_exist_if_it_does_not_exist() throws Exception {
        HttpServletStubber stubber = new HttpServletStubber();
        stubber.parameter("id", "1").listen();
        new EventSearch().doGet(stubber.servletRequest, stubber.servletResponse);

        Error error = new Gson().fromJson(stubber.gathered(), Error.class);
        assertEquals(EventSearch.EVENT_NOT_FOUND_ERROR, error.error);
    }

    @Test
    public void should_error_if_parameter_is_invalid() throws Exception {
        HttpServletStubber stubber = new HttpServletStubber();
        stubber.parameter("id", "OMG").listen();
        new EventSearch().doGet(stubber.servletRequest, stubber.servletResponse);
        Error error = new Gson().fromJson(stubber.gathered(), Error.class);

        assertEquals(EventSearch.ID_ERROR, error.error);
    }

    @Test
    public void should_error_if_no_parameter_is_sent() throws Exception {
        HttpServletStubber stubber = new HttpServletStubber();
        stubber.listen();
        new EventSearch().doGet(stubber.servletRequest, stubber.servletResponse);
        Error error = new Gson().fromJson(stubber.gathered(), Error.class);

        assertEquals(EventSearch.ID_NONE_ERROR, error.error);
    }

    @Test
    public void should_return_event_if_it_exist() throws Exception {
        Event dummyEvent = DBFeeder.createDummyEvent();

        HttpServletStubber stubber = new HttpServletStubber();
        stubber.parameter("id", String.valueOf(dummyEvent.id)).listen();
        new EventSearch().doGet(stubber.servletRequest, stubber.servletResponse);
        Event outputEvent = new Gson().fromJson(stubber.gathered(), Event.class);

        assertEquals(dummyEvent.id, outputEvent.id);
        assertEquals(dummyEvent.name, outputEvent.name);
        assertEquals(dummyEvent.location, outputEvent.location);
        assertEquals(dummyEvent.auctionType, outputEvent.auctionType);
        assertEquals(dummyEvent.category, outputEvent.category);
        assertEquals(dummyEvent.ownerId, outputEvent.ownerId);
        assertEquals(new ImpreciseDate(dummyEvent.createdAt), new ImpreciseDate(outputEvent.createdAt));
        assertEquals(new ImpreciseDate(dummyEvent.updatedAt), new ImpreciseDate(outputEvent.updatedAt));
    }

}