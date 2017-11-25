package main.java.utils;

import main.java.models.Auction;
import main.java.models.Event;
import main.java.models.Good;
import main.java.models.User;

import java.sql.Timestamp;

public class DummyGenerator {

    public static final String TEST_GOOD_NAME = "TestGoodName";
    public static final String TEST_GOOD_IMAGE = "TestGoodImage";
    public static final int TEST_GOOD_AUCTION_ID = 1;

    public static Good getDummyGood() {
        Good good = new Good();
        good.name = TEST_GOOD_NAME;
        good.image = TEST_GOOD_IMAGE;
        good.auctionId = TEST_GOOD_AUCTION_ID;
        return good;
    }

    public static final String TEST_AUCTION_NAME = "TestName";
    public static final double TEST_AUCTION_STARTING_PRICE = 1.0;
    public static final Timestamp TEST_AUCTION_START_TIME = new Timestamp(System.currentTimeMillis());
    public static final String TEST_AUCTION_STATUS = Auction.AUCTION_STATUSES[0];
    public static final boolean TEST_AUCTION_IS_VALID = true;

    public static final String TEST_AUCTION_OTHER_NAME = "TestOtherName";
    public static final double TEST_AUCTION_OTHER_STARTING_PRICE = 2.0;
    public static final Timestamp TEST_AUCTION_OTHER_START_TIME = new Timestamp(System.currentTimeMillis());
    public static final String TEST_AUCTION_OTHER_STATUS = Auction.AUCTION_STATUSES[1];
    public static final boolean TEST_AUCTION_OTHER_IS_VALID = true;

    public static Auction getDummyAuction() {
        Auction auction = new Auction();
        auction.name = TEST_AUCTION_NAME;
        auction.startingPrice = TEST_AUCTION_STARTING_PRICE;
        auction.startTime = TEST_AUCTION_START_TIME;
        auction.status = TEST_AUCTION_STATUS;
        auction.isValid = TEST_AUCTION_IS_VALID;
        return auction;
    }

    public static Auction getOtherDummyAuction() {
        Auction auction = new Auction();
        auction.name = TEST_AUCTION_OTHER_NAME;
        auction.startingPrice = TEST_AUCTION_OTHER_STARTING_PRICE;
        auction.startTime = TEST_AUCTION_OTHER_START_TIME;
        auction.status = TEST_AUCTION_OTHER_STATUS;
        auction.isValid = TEST_AUCTION_OTHER_IS_VALID;
        return auction;
    }

    public static final String TEST_EVENT_NAME = "TestName";
    public static final int TEST_EVENT_AUCTION_TIME = 100;
    public static final String TEST_EVENT_LOCATION = "TestLocation";
    public static final String TEST_EVENT_AUCTION_TYPE = Event.AUCTION_TYPES[0];
    public static final String TEST_EVENT_CATEGORY = "TestCategory";

    public static final String TEST_EVENT_OTHER_NAME = "TestOtherName";
    public static final int TEST_EVENT_OTHER_AUCTION_TIME = 100;
    public static final String TEST_EVENT_OTHER_LOCATION = "TestLocation";
    public static final String TEST_EVENT_OTHER_AUCTION_TYPE = Event.AUCTION_TYPES[1];
    public static final String TEST_EVENT__OTHER_CATEGORY = "TestCategory";

    public static Event getDummyEvent() {
        Event event = new Event();
        event.name = TEST_EVENT_NAME;
        event.auctionTime = TEST_EVENT_AUCTION_TIME;
        event.location = TEST_EVENT_LOCATION;
        event.auctionType = TEST_EVENT_AUCTION_TYPE;
        event.category = TEST_EVENT_CATEGORY;
        return event;
    }

    public static Event getOtherDummyEvent() {
        Event event = new Event();
        event.name = TEST_EVENT_OTHER_NAME;
        event.category = TEST_EVENT__OTHER_CATEGORY;
        event.auctionTime = TEST_EVENT_OTHER_AUCTION_TIME;
        event.auctionType = TEST_EVENT_OTHER_AUCTION_TYPE;
        event.location = TEST_EVENT_OTHER_LOCATION;
        return event;
    }

    public static final String TEST_USER_NAME = "Erik";
    public static final String TEST_USER_LAST_NAME = "Green";
    public static final String TEST_USER_EMAIL = "hi@example.com";
    public static final String TEST_USER_PASSWORD = "1234";

    public static final String TEST_USER_OTHER_NAME = "TestOtherName";
    public static final String TEST_USER_OTHER_LAST_NAME = "TestOtherLastName";
    public static final String TEST_USER_OTHER_EMAIL = "TestOtherEmail";
    public static final String TEST_USER_OTHER_PASSWORD = "TestOtherPassword";

    public static User getDummyUser() {
        User user = new User();
        user.name = TEST_USER_NAME;
        user.lastName = TEST_USER_LAST_NAME;
        user.email = TEST_USER_EMAIL;
        user.password = TEST_USER_PASSWORD;
        return user;
    }

    public static User getOtherDummyUser() {
        User user = new User();
        user.name = TEST_USER_OTHER_NAME;
        user.lastName = TEST_USER_OTHER_LAST_NAME;
        user.email = TEST_USER_OTHER_EMAIL;
        user.password = TEST_USER_OTHER_PASSWORD;
        return user;
    }
}
