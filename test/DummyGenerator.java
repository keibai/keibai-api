import main.java.models.Auction;
import main.java.models.Event;
import main.java.models.Good;
import main.java.models.User;

import java.sql.Timestamp;

class DummyGenerator {

    private static final String TEST_GOOD_NAME = "TestGoodName";
    private static final String TEST_GOOD_IMAGE = "TestGoodImage";
    private static final int TEST_GOOD_AUCTION_ID = 1;

    static Good getDummyGood() {
        Good good = new Good();
        good.name = TEST_GOOD_NAME;
        good.image = TEST_GOOD_IMAGE;
        good.auctionId = TEST_GOOD_AUCTION_ID;
        return good;
    }

    private static final String TEST_AUCTION_NAME = "TestName";
    private static final double TEST_AUCTION_STARTING_PRICE = 1.0;
    private static final Timestamp TEST_AUCTION_START_TIME = new Timestamp(System.currentTimeMillis());
    private static final String TEST_AUCTION_STATUS = "TestStatus";
    private static final boolean TEST_AUCTION_IS_VALID = false;

    static Auction getDummyAuction() {
        Auction auction = new Auction();
        auction.name = TEST_AUCTION_NAME;
        auction.startingPrice = TEST_AUCTION_STARTING_PRICE;
        auction.startTime = TEST_AUCTION_START_TIME;
        auction.status = TEST_AUCTION_STATUS;
        auction.isValid = TEST_AUCTION_IS_VALID;
        auction.ownerId = 1;
        auction.winnerId = 2;
        auction.eventId = 1;
        return auction;
    }

    private static final String TEST_EVENT_NAME = "TestName";
    private static final int TEST_EVENT_AUCTION_TIME = 100;
    private static final String TEST_EVENT_LOCATION = "TestLocation";
    private static final String TEST_EVENT_AUCTION_TYPE = "TestAuctionType";
    private static final String TEST_EVENT_CATEGORY = "TestCategory";

    static Event getDummyEvent() {
        Event event = new Event();
        event.name = TEST_EVENT_NAME;
        event.auctionTime = TEST_EVENT_AUCTION_TIME;
        event.location = TEST_EVENT_LOCATION;
        event.auctionType = TEST_EVENT_AUCTION_TYPE;
        event.category = TEST_EVENT_CATEGORY;
        event.ownerId = 1;
        return event;
    }

    private static final String TEST_USER_NAME = "TestName";
    private static final String TEST_USER_LAST_NAME = "TestLastName";
    private static final String TEST_USER_EMAIL = "TestEmail";
    private static final String TEST_USER_PASSWORD = "TestPassword";

    private static final String TEST_USER_OTHER_NAME = "TestOtherName";
    private static final String TEST_USER_OTHER_LAST_NAME = "TestOtherLastName";
    private static final String TEST_USER_OTHER_EMAIL = "TestOtherEmail";
    private static final String TEST_USER_OTHER_PASSWORD = "TestOtherPassword";

    static User getDummyUser() {
        User user = new User();
        user.name = TEST_USER_NAME;
        user.lastName = TEST_USER_LAST_NAME;
        user.email = TEST_USER_EMAIL;
        user.password = TEST_USER_PASSWORD;
        return user;
    }

    static User getOtherDummyUser() {
        User user = new User();
        user.name = TEST_USER_OTHER_NAME;
        user.lastName = TEST_USER_OTHER_LAST_NAME;
        user.email = TEST_USER_OTHER_EMAIL;
        user.password = TEST_USER_OTHER_PASSWORD;
        return user;
    }
}
