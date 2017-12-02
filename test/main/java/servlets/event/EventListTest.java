package main.java.servlets.event;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import main.java.dao.sql.AbstractDBTest;
import main.java.dao.sql.EventDBTest;
import main.java.mocks.HttpServletStubber;
import main.java.models.Event;
import main.java.models.User;
import main.java.utils.DBFeeder;
import main.java.utils.ImpreciseDate;
import org.junit.Test;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.*;

public class EventListTest extends AbstractDBTest {

    @Test
    public void test_get_event_list_with_no_event_returns_empty_list() throws Exception {
        HttpServletStubber stubber = new HttpServletStubber();
        stubber.listen();
        new EventList().doGet(stubber.servletRequest, stubber.servletResponse);
        List<Event> outputListEvent = new Gson().fromJson(stubber.gathered(), new TypeToken<List<Event>>(){}.getType());
        assertEquals(0, outputListEvent.size());
    }

    @Test
    public void test_get_event_list_returns_list_of_events() throws Exception {
        User dummyUser = DBFeeder.createDummyUser();
        Event event = DBFeeder.createDummyEvent();
        Event otherEvent = DBFeeder.createOtherDummyEvent(dummyUser.id);

        List<Event> expectedEventList = new LinkedList<Event>() {{
            add(event);
            add(otherEvent);
        }};

        HttpServletStubber stubber = new HttpServletStubber();
        stubber.listen();
        new EventList().doGet(stubber.servletRequest, stubber.servletResponse);
        List<Event> outputEventList = new Gson().fromJson(stubber.gathered(), new TypeToken<List<Event>>(){}.getType());

        EventDBTest.assertEventListEquals(expectedEventList, outputEventList);
    }
}