package main.java.servlets.event;

import com.google.gson.Gson;
import main.java.dao.sql.AbstractDBTest;
import main.java.dao.sql.EventDBTest;
import main.java.gson.BetterGson;
import main.java.mocks.HttpServletStubber;
import main.java.models.Event;
import main.java.models.User;
import main.java.utils.DBFeeder;
import org.junit.Test;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class EventListTest extends AbstractDBTest {

    @Test
    public void test_get_event_list_with_no_event_returns_empty_array() throws Exception {
        HttpServletStubber stubber = new HttpServletStubber();
        stubber.listen();
        new EventList().doGet(stubber.servletRequest, stubber.servletResponse);
        Event[] outputEvents = new BetterGson().newInstance().fromJson(stubber.gathered(), Event[].class);

        assertEquals(0, outputEvents.length);
    }

    @Test
    public void test_get_event_list_returns_list_of_events() throws Exception {
        User dummyUser = DBFeeder.createDummyUser();
        Event event = DBFeeder.createDummyEvent();
        Event otherEvent = DBFeeder.createOtherDummyEvent(dummyUser.id);

        List<Event> expectedEventList = new LinkedList<Event>() {{
            add(otherEvent);
            add(event);
        }};

        HttpServletStubber stubber = new HttpServletStubber();
        stubber.listen();
        new EventList().doGet(stubber.servletRequest, stubber.servletResponse);
        Event[] modelList = new BetterGson().newInstance().fromJson(stubber.gathered(), Event[].class);

        EventDBTest.assertEventListEquals(expectedEventList, Arrays.asList(modelList));
    }
}