package main.java.servlets.event;

import com.google.gson.Gson;
import main.java.dao.sql.AbstractDBTest;
import main.java.mocks.HttpServletStubber;
import main.java.models.Event;
import main.java.models.User;
import main.java.models.meta.Error;
import main.java.utils.DBFeeder;
import main.java.utils.ImpreciseDate;
import main.java.utils.JsonResponse;
import org.junit.Test;

import static org.junit.Assert.*;

public class EventUpdateStatusTest extends AbstractDBTest {

    @Test
    public void test_update_can_not_be_done_if_user_not_authenticated() throws Exception {
        HttpServletStubber stubber = new HttpServletStubber();
        stubber.body("").listen();
        new EventUpdateStatus().doPost(stubber.servletRequest, stubber.servletResponse);
        Error error = new Gson().fromJson(stubber.gathered(), Error.class);

        assertEquals(JsonResponse.UNAUTHORIZED, error.error);
    }

    @Test
    public void test_invalid_request_if_body_is_empty() throws Exception {
        User dummyUser = DBFeeder.createDummyUser();

        HttpServletStubber stubber = new HttpServletStubber();
        stubber.authenticate(dummyUser.id);
        stubber.body("").listen();
        new EventUpdateStatus().doPost(stubber.servletRequest, stubber.servletResponse);
        Error error = new Gson().fromJson(stubber.gathered(), Error.class);

        assertEquals(JsonResponse.INVALID_REQUEST, error.error);
    }

    @Test
    public void test_invalid_request_if_event_id_is_0() throws Exception {
        User dummyUser = DBFeeder.createDummyUser();

        Event wrongEvent = new Event();
        wrongEvent.id = 0;
        wrongEvent.status = Event.ACTIVE;
        String wrongEventJson = new Gson().toJson(wrongEvent);

        HttpServletStubber stubber = new HttpServletStubber();
        stubber.authenticate(dummyUser.id);
        stubber.body(wrongEventJson).listen();
        new EventUpdateStatus().doPost(stubber.servletRequest, stubber.servletResponse);
        Error error = new Gson().fromJson(stubber.gathered(), Error.class);

        assertEquals(JsonResponse.INVALID_REQUEST, error.error);
    }

    @Test
    public void test_error_if_event_status_not_valid() throws Exception {
        Event dummyEvent = DBFeeder.createDummyEvent();

        Event wrongEvent = new Event();
        wrongEvent.id = dummyEvent.id;
        wrongEvent.status = "WRONG_STATUS";
        String wrongEventJson = new Gson().toJson(wrongEvent);

        HttpServletStubber stubber = new HttpServletStubber();
        stubber.authenticate(dummyEvent.ownerId);
        stubber.body(wrongEventJson).listen();
        new EventUpdateStatus().doPost(stubber.servletRequest, stubber.servletResponse);
        Error error = new Gson().fromJson(stubber.gathered(), Error.class);

        assertEquals(EventUpdateStatus.INVALID_STATUS, error.error);
    }

    @Test
    public void test_error_if_event_does_not_exist() throws Exception {
        User dummyUser = DBFeeder.createDummyUser();

        Event wrongEvent = new Event();
        wrongEvent.id = 2;
        wrongEvent.status = Event.CLOSED;
        String wrongEventJson = new Gson().toJson(wrongEvent);

        HttpServletStubber stubber = new HttpServletStubber();
        stubber.authenticate(dummyUser.id);
        stubber.body(wrongEventJson).listen();
        new EventUpdateStatus().doPost(stubber.servletRequest, stubber.servletResponse);
        Error error = new Gson().fromJson(stubber.gathered(), Error.class);

        assertEquals(EventUpdateStatus.EVENT_NOT_EXIST, error.error);
    }

    @Test
    public void test_unauthorized_when_owner_is_not_the_user() throws Exception {
        User dummyUser = DBFeeder.createDummyUser();
        Event dummyEvent = DBFeeder.createDummyEvent();

        Event updateEvent = new Event();
        updateEvent.id = dummyEvent.id;
        updateEvent.status = Event.CLOSED;
        String updateEventJson = new Gson().toJson(updateEvent);

        HttpServletStubber stubber = new HttpServletStubber();
        stubber.authenticate(dummyUser.id);
        stubber.body(updateEventJson).listen();
        new EventUpdateStatus().doPost(stubber.servletRequest, stubber.servletResponse);
        Error error = new Gson().fromJson(stubber.gathered(), Error.class);

        assertEquals(JsonResponse.UNAUTHORIZED, error.error);
    }

    @Test
    public void test_event_statue_update_is_performed() throws Exception {
        Event dummyEvent = DBFeeder.createDummyEvent();

        // Wait 1 second to avoid createdAt == updatedAt
        Thread.sleep(1000);

        Event updateEvent = new Event();
        updateEvent.id = dummyEvent.id;
        updateEvent.status = Event.CLOSED;
        String updatedEventJson = new Gson().toJson(updateEvent);

        HttpServletStubber stubber = new HttpServletStubber();
        stubber.authenticate(dummyEvent.ownerId);
        stubber.body(updatedEventJson).listen();
        new EventUpdateStatus().doPost(stubber.servletRequest, stubber.servletResponse);
        Event outputEvent = new Gson().fromJson(stubber.gathered(), Event.class);

        assertEquals(dummyEvent.id, outputEvent.id);
        assertEquals(dummyEvent.name, outputEvent.name);
        assertEquals(dummyEvent.auctionTime, outputEvent.auctionTime);
        assertEquals(dummyEvent.location, outputEvent.location);
        assertEquals(dummyEvent.auctionType, outputEvent.auctionType);
        assertEquals(dummyEvent.category, outputEvent.category);
        assertEquals(dummyEvent.ownerId, outputEvent.ownerId);
        assertEquals(new ImpreciseDate(dummyEvent.createdAt), new ImpreciseDate(outputEvent.createdAt));
        assertNotEquals(new ImpreciseDate(dummyEvent.updatedAt), new ImpreciseDate(outputEvent.updatedAt));
        assertEquals(Event.CLOSED, outputEvent.status);
    }
}