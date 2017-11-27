package main.java.servlets.event;

import com.google.gson.Gson;
import main.java.dao.DAOException;
import main.java.dao.sql.AbstractDBTest;
import main.java.mocks.HttpServletStubber;
import main.java.models.meta.Error;
import main.java.models.Event;
import main.java.models.User;
import main.java.utils.DBFeeder;
import main.java.utils.DummyGenerator;
import org.junit.Test;

import javax.servlet.ServletException;
import java.io.IOException;

import static org.junit.Assert.*;

public class EventNewTest extends AbstractDBTest {

    @Test
    public void test_not_authenticate_user_can_not_create_event() throws DAOException, IOException, ServletException {
        HttpServletStubber stubber = new HttpServletStubber();

        Event attemptEvent = DummyGenerator.getDummyEvent();
        String attemptEventJson = new Gson().toJson(attemptEvent);
        stubber.body(attemptEventJson).listen();
        new EventNew().doPost(stubber.servletRequest, stubber.servletResponse);

        Error error = new Gson().fromJson(stubber.gathered(), Error.class);
        assertEquals("Unauthorized.", error.error);
    }

    @Test
    public void test_event_with_blank_name_can_not_be_created() throws DAOException, IOException, ServletException {
        Event attemptEvent = DummyGenerator.getDummyEvent();
        attemptEvent.name = "";
        common_event_error_test(attemptEvent, EventNew.NAME_ERROR);
    }

    @Test
    public void test_event_with_wrong_auction_time_can_not_be_created() throws ServletException, DAOException, IOException {
        Event attemptEvent = DummyGenerator.getDummyEvent();
        attemptEvent.auctionTime = 4;
        common_event_error_test(attemptEvent, EventNew.AUCTION_TIME_ERROR);
    }

    @Test
    public void test_event_with_blank_location_can_not_be_created() throws ServletException, DAOException, IOException {
        Event attemptEvent = DummyGenerator.getDummyEvent();
        attemptEvent.location = "     ";
        common_event_error_test(attemptEvent, EventNew.LOCATION_ERROR);
    }

    @Test
    public void test_event_with_no_auction_type_can_not_be_created() throws ServletException, DAOException, IOException {
        Event attemptEvent = DummyGenerator.getDummyEvent();
        attemptEvent.auctionType = "";
        common_event_error_test(attemptEvent, EventNew.AUCTION_TYPE_ERROR);
    }

    @Test
    public void test_event_with_invalid_auction_type_can_not_be_created() throws ServletException, DAOException, IOException {
        Event attemptEvent = DummyGenerator.getDummyEvent();
        attemptEvent.auctionType = "NOT AN AUCTION TYPE";
        common_event_error_test(attemptEvent, EventNew.AUCTION_TYPE_ERROR);
    }

    @Test
    public void test_event_with_blank_category_can_not_be_created() throws ServletException, DAOException, IOException {
        Event attemptEvent = DummyGenerator.getDummyEvent();
        attemptEvent.category = "       ";
        common_event_error_test(attemptEvent, EventNew.CATEGORY_ERROR);
    }

    @Test
    public void test_event_with_invalid_owner_can_not_be_created() throws Exception {
        Event attemptEvent = DummyGenerator.getDummyEvent();
        attemptEvent.ownerId = 2;

        User dummyUser = DBFeeder.createDummyUser();
        String attemptEventJson = new Gson().toJson(attemptEvent);

        HttpServletStubber stubber = new HttpServletStubber();
        stubber.authenticate(dummyUser.id);
        stubber.body(attemptEventJson).listen();
        new EventNew().doPost(stubber.servletRequest, stubber.servletResponse);

        Error error = new Gson().fromJson(stubber.gathered(), Error.class);
        assertEquals(EventNew.OWNER_NOT_EXIST_EROR, error.error);
    }

    @Test
    public void should_create_new_event() throws IOException, ServletException, DAOException {
        User dummyUser = DBFeeder.createDummyUser();

        Event attemptEvent = DummyGenerator.getDummyEvent();
        attemptEvent.ownerId = dummyUser.id;
        String attemptEventJson = new Gson().toJson(attemptEvent);

        HttpServletStubber stubber = new HttpServletStubber();
        stubber.authenticate(dummyUser.id);
        stubber.body(attemptEventJson).listen();
        new EventNew().doPost(stubber.servletRequest, stubber.servletResponse);

        Event outputEvent = new Gson().fromJson(stubber.gathered(), Event.class);

        assertEquals(attemptEvent.name, outputEvent.name);
        assertEquals(attemptEvent.auctionTime, outputEvent.auctionTime);
        assertEquals(attemptEvent.auctionType, outputEvent.auctionType);
        assertEquals(attemptEvent.location, outputEvent.location);
        assertNotEquals(outputEvent.ownerId, 0);
        assertNotEquals(outputEvent.id, 0);
        assertNotNull(outputEvent.createdAt);
        assertNotNull(outputEvent.updatedAt);
    }

    private void common_event_error_test(Event attemptEvent, String errorMsg) throws DAOException, IOException, ServletException {
        User dummyUser = DBFeeder.createDummyUser();

        attemptEvent.ownerId = dummyUser.id;
        String attemptEventJson = new Gson().toJson(attemptEvent);

        HttpServletStubber stubber = new HttpServletStubber();
        stubber.authenticate(dummyUser.id);
        stubber.body(attemptEventJson).listen();
        new EventNew().doPost(stubber.servletRequest, stubber.servletResponse);

        Error error = new Gson().fromJson(stubber.gathered(), Error.class);
        assertEquals(errorMsg, error.error);
    }

}