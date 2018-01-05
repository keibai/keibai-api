package main.java.socket;

import com.google.gson.Gson;
import main.java.dao.DAOException;
import main.java.dao.sql.AbstractDBTest;
import main.java.mocks.MockHttpSession;
import main.java.mocks.MockSession;
import main.java.mocks.MockWSSender;
import main.java.models.Auction;
import main.java.models.Event;
import main.java.models.User;
import main.java.models.meta.BodyWS;
import main.java.models.meta.Msg;
import main.java.utils.DBFeeder;
import main.java.utils.JsonCommon;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class BidWSTest extends AbstractDBTest {

    MockSession mockSession;
    MockHttpSession mockHttpSession;
    MockWSSender mockSender;
    BidWS bidWS;

    // All correct BidWS requests will reference to this information.
    User user;
    Event event;
    Auction auction; // By default auction status is PENDING.

    @Before
    public void setup() throws DAOException {
        user = DBFeeder.createDummyUser();
        event = DBFeeder.createOtherDummyEvent(user.id);
        auction = DBFeeder.createDummyAuction(event.id, user.id);

        mockSession = new MockSession();
        mockHttpSession = new MockHttpSession();
        mockHttpSession.setUserId(user.id);
        mockSender = new MockWSSender();

        bidWS = new BidWS();
        bidWS.sender = mockSender;
        bidWS.onOpen(mockSession, mockHttpSession);
    }

    @Test
    public void bid_subscription_should_complete() {
        Auction requestAuction = new Auction();
        requestAuction.id = auction.id;

        BodyWS requestBody = new BodyWS();
        requestBody.type = "AuctionSubscribe";
        requestBody.nonce = "any";
        requestBody.status = 200;
        requestBody.json = new Gson().toJson(auction);
        bidWS.onMessage(mockSession, requestBody);

        BodyWS replyBody = (BodyWS) mockSender.newObjLastReply;
        assertEquals(mockSession, mockSender.sessionLastReply);
        assertEquals(requestBody, mockSender.originObjLastReply);
        assertEquals(200, replyBody.status);
        assertEquals(JsonCommon.OK, new Gson().fromJson(replyBody.json, Msg.class).msg);
    }
}