package main.java.socket;

import com.google.gson.Gson;
import main.java.dao.AuctionDAO;
import main.java.dao.BidDAO;
import main.java.dao.DAOException;
import main.java.dao.UserDAO;
import main.java.dao.sql.AbstractDBTest;
import main.java.dao.sql.AuctionDAOSQL;
import main.java.dao.sql.BidDAOSQL;
import main.java.dao.sql.UserDAOSQL;
import main.java.mocks.MockHttpSession;
import main.java.mocks.MockSession;
import main.java.mocks.MockWSSender;
import main.java.models.Auction;
import main.java.models.Bid;
import main.java.models.Event;
import main.java.models.User;
import main.java.models.meta.BodyWS;
import main.java.utils.DBFeeder;
import main.java.utils.DummyGenerator;
import main.java.utils.JsonCommon;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class BidWSTest extends AbstractDBTest {

    UserDAO userDAO = UserDAOSQL.getInstance();
    AuctionDAO auctionDAO = AuctionDAOSQL.getInstance();
    BidDAO bidDAO = BidDAOSQL.getInstance();

    MockSession mockSession;
    MockHttpSession mockHttpSession;
    MockWSSender<BodyWS> mockSender;
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

        BidWS.clearConnected();
        bidWS = new BidWS();
        bidWS.sender = mockSender;
        bidWS.onOpen(mockSession, mockHttpSession);
    }

    /* AuctionSubscribe */

    @Test
    public void bid_subscription_should_complete() {
        Auction requestAuction = new Auction();
        requestAuction.id = auction.id;

        BodyWS requestBody = new BodyWS();
        requestBody.type = BidWS.TYPE_AUCTION_SUBSCRIBE;
        requestBody.nonce = "successful-subscription-nonce";
        requestBody.status = 200;
        requestBody.json = new Gson().toJson(auction);
        bidWS.onMessage(mockSession, requestBody);

        BodyWS replyBody = (BodyWS) mockSender.newObjLastReply;
        assertEquals(mockSession, mockSender.sessionLastReply);
        assertEquals(requestBody, mockSender.originObjLastReply);
        assertEquals(200, replyBody.status);
        assertEquals(JsonCommon.ok(), replyBody.json);
    }

    /* AuctionBid */

    @Test
    public void should_not_create_bid_when_not_authenticated() {
        Auction auction = successfulSubscription();

        // Deauthenticate user first.
        mockHttpSession.setUserId(-1);

        Bid attemptBid = DummyGenerator.getDummyBid();
        attemptBid.auctionId = auction.id;
        BodyWS requestBody = new BodyWS();
        requestBody.type = BidWS.TYPE_AUCTION_BID;
        requestBody.nonce = "any";
        requestBody.json = new Gson().toJson(attemptBid);
        bidWS.onMessage(mockSession, requestBody);

        BodyWS replyBody = (BodyWS) mockSender.newObjLastReply;
        assertEquals(JsonCommon.unauthorized(), replyBody.json);
        assertEquals(400, replyBody.status);
    }

    @Test
    public void should_not_create_bid_when_json_is_invalid() {
        Auction auction = successfulSubscription();

        String attemptBid = "{";
        BodyWS requestBody = new BodyWS();
        requestBody.type = BidWS.TYPE_AUCTION_BID;
        requestBody.nonce = "any";
        requestBody.json = attemptBid;
        bidWS.onMessage(mockSession, requestBody);

        BodyWS replyBody = (BodyWS) mockSender.newObjLastReply;
        assertEquals(JsonCommon.invalidRequest(), replyBody.json);
        assertEquals(400, replyBody.status);
    }

    @Test
    public void should_not_create_bid_with_invalid_amount() {
        Auction auction = successfulSubscription();

        Bid attemptBid = DummyGenerator.getDummyBid();
        attemptBid.auctionId = auction.id;
        attemptBid.amount = 0.0;
        BodyWS requestBody = new BodyWS();
        requestBody.type = BidWS.TYPE_AUCTION_BID;
        requestBody.nonce = "any";
        requestBody.json = new Gson().toJson(attemptBid);
        bidWS.onMessage(mockSession, requestBody);

        BodyWS replyBody = (BodyWS) mockSender.newObjLastReply;
        assertEquals(JsonCommon.error(BidWS.INVALID_AMOUNT_ERROR), replyBody.json);
        assertEquals(400, replyBody.status);
    }

    @Test
    public void should_not_create_bid_without_auction() {
        Auction auction = successfulSubscription();

        Bid attemptBid = DummyGenerator.getDummyBid();
        attemptBid.amount = 1.0;
        BodyWS requestBody = new BodyWS();
        requestBody.type = BidWS.TYPE_AUCTION_BID;
        requestBody.nonce = "any";
        requestBody.json = new Gson().toJson(attemptBid);
        bidWS.onMessage(mockSession, requestBody);

        BodyWS replyBody = (BodyWS) mockSender.newObjLastReply;
        assertEquals(JsonCommon.error(BidWS.AUCTION_ID_ERROR), replyBody.json);
        assertEquals(400, replyBody.status);
    }

    @Test
    public void should_not_create_bid_if_not_subscribed_to_the_auction() {
        Bid attemptBid = DummyGenerator.getDummyBid();
        attemptBid.amount = 1.0;
        attemptBid.auctionId = auction.id;
        BodyWS requestBody = new BodyWS();
        requestBody.type = BidWS.TYPE_AUCTION_BID;
        requestBody.nonce = "any";
        requestBody.json = new Gson().toJson(attemptBid);
        bidWS.onMessage(mockSession, requestBody);

        BodyWS replyBody = (BodyWS) mockSender.newObjLastReply;
        assertEquals(JsonCommon.error(BidWS.SUBSCRIPTION_ERROR), replyBody.json);
        assertEquals(400, replyBody.status);
    }

    @Test
    public void should_not_create_bid_if_auction_is_not_in_progress() {
        Auction auction = successfulSubscription();

        Bid attemptBid = DummyGenerator.getDummyBid();
        attemptBid.auctionId = auction.id;
        attemptBid.amount = 1.0;
        BodyWS requestBody = new BodyWS();
        requestBody.type = BidWS.TYPE_AUCTION_BID;
        requestBody.nonce = "any";
        requestBody.json = new Gson().toJson(attemptBid);
        bidWS.onMessage(mockSession, requestBody);

        BodyWS replyBody = (BodyWS) mockSender.newObjLastReply;
        assertEquals(JsonCommon.error(BidWS.AUCTION_NOT_IN_PROGRESS), replyBody.json);
        assertEquals(400, replyBody.status);
    }

    @Test
    public void should_not_create_bid_if_amount_is_lower_than_starting_price() throws DAOException {
        Auction auction = successfulSubscription();
        Auction dbAuction = auctionDAO.getById(auction.id);
        dbAuction.status = Auction.IN_PROGRESS;
        auctionDAO.update(dbAuction);

        Bid attemptBid = DummyGenerator.getDummyBid();
        attemptBid.auctionId = auction.id;
        attemptBid.amount = dbAuction.startingPrice - 0.01;
        BodyWS requestBody = new BodyWS();
        requestBody.type = BidWS.TYPE_AUCTION_BID;
        requestBody.nonce = "any";
        requestBody.json = new Gson().toJson(attemptBid);
        bidWS.onMessage(mockSession, requestBody);

        BodyWS replyBody = (BodyWS) mockSender.newObjLastReply;
        assertEquals(JsonCommon.error(BidWS.LOW_BID_STARTING_PRICE), replyBody.json);
        assertEquals(400, replyBody.status);
    }
    
    @Test
    public void should_not_create_bid_if_not_enough_credit() throws DAOException {
        Auction auction = successfulSubscription();
        Auction dbAuction = auctionDAO.getById(auction.id);
        dbAuction.status = Auction.IN_PROGRESS;
        auctionDAO.update(dbAuction);

        User dbUser = userDAO.getById(user.id);
        dbUser.credit = dbAuction.startingPrice - 0.01;
        userDAO.update(dbUser);

        Bid attemptBid = DummyGenerator.getDummyBid();
        attemptBid.auctionId = auction.id;
        attemptBid.amount = dbAuction.startingPrice;
        BodyWS requestBody = new BodyWS();
        requestBody.type = BidWS.TYPE_AUCTION_BID;
        requestBody.nonce = "any";
        requestBody.json = new Gson().toJson(attemptBid);
        bidWS.onMessage(mockSession, requestBody);

        BodyWS replyBody = (BodyWS) mockSender.newObjLastReply;
        assertEquals(JsonCommon.error(BidWS.NO_CREDIT), replyBody.json);
        assertEquals(400, replyBody.status);
    }

    @Test
    public void should_not_create_if_there_is_a_higher_bid_by_himself() throws DAOException {
        Auction auction = successfulSubscription();
        Auction dbAuction = auctionDAO.getById(auction.id);
        dbAuction.status = Auction.IN_PROGRESS;
        auctionDAO.update(dbAuction);

        User dbUser = userDAO.getById(user.id);
        dbUser.credit = 2000;
        userDAO.update(dbUser);

        // First bid @ 2000.
        Bid attemptBid1 = DummyGenerator.getDummyBid();
        attemptBid1.auctionId = auction.id;
        attemptBid1.amount = 2000;
        BodyWS requestBody = new BodyWS();
        requestBody.type = BidWS.TYPE_AUCTION_BID;
        requestBody.nonce = "first";
        requestBody.json = new Gson().toJson(attemptBid1);
        bidWS.onMessage(mockSession, requestBody);

        BodyWS replyBody1 = (BodyWS) mockSender.newObjLastReply;
        Bid dbBid1 = new Gson().fromJson(replyBody1.json, Bid.class);
        assertEquals(user.id, dbBid1.ownerId);
        assertEquals(2000, dbBid1.amount, 0);
        assertEquals(200, replyBody1.status);

        // Second bid @ 1000.
        Bid attemptBid2 = DummyGenerator.getDummyBid();
        attemptBid2.auctionId = auction.id;
        attemptBid2.amount = 1000;
        BodyWS requestBody2 = new BodyWS();
        requestBody2.type = BidWS.TYPE_AUCTION_BID;
        requestBody2.nonce = "second";
        requestBody2.json = new Gson().toJson(attemptBid2);
        bidWS.onMessage(mockSession, requestBody2);

        BodyWS replyBody2 = (BodyWS) mockSender.newObjLastReply;
        assertEquals(JsonCommon.error(BidWS.LOW_BID_HIGHER_BID), replyBody2.json);
        assertEquals(400, replyBody2.status);
    }

    @Test
    public void should_not_create_if_there_is_a_higher_bid_by_another_user() throws DAOException {
        Auction auction = successfulSubscription();
        Auction dbAuction = auctionDAO.getById(auction.id);
        dbAuction.status = Auction.IN_PROGRESS;
        auctionDAO.update(dbAuction);

        User dbUser = userDAO.getById(user.id);
        dbUser.credit = 2000;
        userDAO.update(dbUser);

        User altUser = DBFeeder.createOtherDummyUser();
        altUser.credit = 2000;
        userDAO.update(altUser);

        // User bids @ 1000.
        Bid attemptBid1 = DummyGenerator.getDummyBid();
        attemptBid1.auctionId = auction.id;
        attemptBid1.amount = 1000;
        BodyWS requestBody = new BodyWS();
        requestBody.type = BidWS.TYPE_AUCTION_BID;
        requestBody.nonce = "first";
        requestBody.json = new Gson().toJson(attemptBid1);
        bidWS.onMessage(mockSession, requestBody);

        BodyWS replyBody1 = (BodyWS) mockSender.newObjLastReply;
        Bid dbBid1 = new Gson().fromJson(replyBody1.json, Bid.class);
        assertEquals(user.id, dbBid1.ownerId);
        assertEquals(1000, dbBid1.amount, 0);
        assertEquals(200, replyBody1.status);

        // Alt user authenticates.
        mockHttpSession.setUserId(altUser.id);

        // Alt user bids @ 1000.
        Bid attemptBid2 = DummyGenerator.getDummyBid();
        attemptBid2.auctionId = auction.id;
        attemptBid2.amount = 1000;
        BodyWS requestBody2 = new BodyWS();
        requestBody2.type = BidWS.TYPE_AUCTION_BID;
        requestBody2.nonce = "second";
        requestBody2.json = new Gson().toJson(attemptBid2);
        bidWS.onMessage(mockSession, requestBody2);

        BodyWS replyBody2 = (BodyWS) mockSender.newObjLastReply;
        assertEquals(JsonCommon.error(BidWS.LOW_BID_HIGHER_BID), replyBody2.json);
        assertEquals(400, replyBody2.status);

        // Alt user bids @ 2000.
        Bid attemptBid3 = DummyGenerator.getDummyBid();
        attemptBid3.auctionId = auction.id;
        attemptBid3.amount = 2000;
        BodyWS requestBody3 = new BodyWS();
        requestBody3.type = BidWS.TYPE_AUCTION_BID;
        requestBody3.nonce = "third";
        requestBody3.json = new Gson().toJson(attemptBid3);
        bidWS.onMessage(mockSession, requestBody3);

        BodyWS replyBody3 = (BodyWS) mockSender.newObjLastReply;
        Bid dbBid3 = new Gson().fromJson(replyBody3.json, Bid.class);
        assertEquals(altUser.id, dbBid3.ownerId);
        assertEquals(2000, dbBid3.amount, 0);
        assertEquals(200, replyBody3.status);
    }

    @Test
    public void should_create_bid() throws DAOException {
        Auction auction = successfulSubscription();
        Auction dbAuction = auctionDAO.getById(auction.id);
        dbAuction.status = Auction.IN_PROGRESS;
        auctionDAO.update(dbAuction);

        User dbUser = userDAO.getById(user.id);
        dbUser.credit = 1000;
        userDAO.update(dbUser);

        Bid attemptBid1 = DummyGenerator.getDummyBid();
        attemptBid1.auctionId = auction.id;
        attemptBid1.amount = 1000;
        BodyWS requestBody = new BodyWS();
        requestBody.type = BidWS.TYPE_AUCTION_BID;
        requestBody.nonce = "first";
        requestBody.json = new Gson().toJson(attemptBid1);
        bidWS.onMessage(mockSession, requestBody);

        BodyWS replyBody1 = (BodyWS) mockSender.newObjLastReply;
        Bid dbBid1 = new Gson().fromJson(replyBody1.json, Bid.class);
        assertEquals(user.id, dbBid1.ownerId);
        assertEquals(1000, dbBid1.amount, 0);
        assertEquals(200, replyBody1.status);
    }

    @Test
    public void should_create_bid_if_highest_bidder() throws DAOException {
        Auction auction = successfulSubscription();
        Auction dbAuction = auctionDAO.getById(auction.id);
        dbAuction.status = Auction.IN_PROGRESS;
        auctionDAO.update(dbAuction);

        User dbUser = userDAO.getById(user.id);
        dbUser.credit = 2000;
        userDAO.update(dbUser);

        // First bid @ 1000.
        Bid attemptBid1 = DummyGenerator.getDummyBid();
        attemptBid1.auctionId = auction.id;
        attemptBid1.amount = 1000;
        BodyWS requestBody1 = new BodyWS();
        requestBody1.type = BidWS.TYPE_AUCTION_BID;
        requestBody1.nonce = "first";
        requestBody1.json = new Gson().toJson(attemptBid1);
        bidWS.onMessage(mockSession, requestBody1);

        BodyWS replyBody1 = (BodyWS) mockSender.newObjLastReply;
        Bid dbBid1 = new Gson().fromJson(replyBody1.json, Bid.class);
        assertEquals(user.id, dbBid1.ownerId);
        assertEquals(1000, dbBid1.amount, 0);
        assertEquals(200, replyBody1.status);

        // Second bid @ 2000.
        Bid attemptBid2 = DummyGenerator.getDummyBid();
        attemptBid2.auctionId = auction.id;
        attemptBid2.amount = 2000;
        BodyWS requestBody2 = new BodyWS();
        requestBody2.type = BidWS.TYPE_AUCTION_BID;
        requestBody2.nonce = "second";
        requestBody2.json = new Gson().toJson(attemptBid2);
        bidWS.onMessage(mockSession, requestBody2);

        BodyWS replyBody2 = (BodyWS) mockSender.newObjLastReply;
        Bid dbBid2 = new Gson().fromJson(replyBody2.json, Bid.class);
        assertEquals(user.id, dbBid2.ownerId);
        assertEquals(2000, dbBid2.amount, 0);
        assertEquals(200, replyBody2.status);
    }

    @Test
    public void should_broadcast_bid_after_a_successful_bid() throws DAOException {
        User altUser = DBFeeder.createOtherDummyUser();

        // Authenticate with alt user first and subscribe to auction.
        MockSession altMockSession = new MockSession();
        MockHttpSession altMockHttpSession = new MockHttpSession();
        altMockHttpSession.setUserId(altUser.id);
        MockWSSender<BodyWS> altMockSender = new MockWSSender<>();
        successfulSubscription();

        BidWS altBidWS = new BidWS();
        altBidWS.sender = mockSender;
        altBidWS.onOpen(altMockSession, altMockHttpSession);

        // Subscribe to auction with alt user.
        Auction altRequestAuction = new Auction();
        altRequestAuction.id = auction.id;

        BodyWS altRequestBody = new BodyWS();
        altRequestBody.type = BidWS.TYPE_AUCTION_SUBSCRIBE;
        altRequestBody.nonce = "successful-alt-subscription-nonce";
        altRequestBody.status = 200;
        altRequestBody.json = new Gson().toJson(auction);
        bidWS.onMessage(mockSession, altRequestBody);
        altBidWS.onMessage(altMockSession, altRequestBody);

        // Authentication with user and bid.
        mockHttpSession.setUserId(user.id);

        Auction auction = successfulSubscription();
        Auction dbAuction = auctionDAO.getById(auction.id);
        dbAuction.status = Auction.IN_PROGRESS;
        auctionDAO.update(dbAuction);

        User dbUser = userDAO.getById(user.id);
        dbUser.credit = 1000;
        userDAO.update(dbUser);

        Bid attemptBid = DummyGenerator.getDummyBid();
        attemptBid.auctionId = auction.id;
        attemptBid.amount = 1000;
        BodyWS requestBody = new BodyWS();
        requestBody.type = BidWS.TYPE_AUCTION_BID;
        requestBody.nonce = "any";
        requestBody.json = new Gson().toJson(attemptBid);
        bidWS.onMessage(mockSession, requestBody);

        BodyWS replyBody = (BodyWS) mockSender.newObjLastReply;
        Bid dbBid = new Gson().fromJson(replyBody.json, Bid.class);
        assertEquals(user.id, dbBid.ownerId);
        assertEquals(1000, dbBid.amount, 0);
        assertEquals(200, replyBody.status);

        assertEquals(2, mockSender.sessionsLastListSend.size());
        assertNotEquals(mockSender.sessionsLastListSend.get(0), mockSender.sessionsLastListSend.get(1));
        assertTrue(mockSender.sessionsLastListSend.stream().anyMatch(e -> e == altMockSession));
        assertTrue(mockSender.sessionsLastListSend.stream().anyMatch(e -> e == mockSession));
    }

    @Test
    public void should_not_broadcast_bid_if_disconnected() throws DAOException {
        User altUser = DBFeeder.createOtherDummyUser();

        // Authenticate with alt user first and subscribe to auction.
        mockHttpSession.setUserId(altUser.id);
        MockSession altMockSession = new MockSession();
        MockHttpSession altMockHttpSession = new MockHttpSession();
        altMockHttpSession.setUserId(user.id);
        MockWSSender<BodyWS> altMockSender = new MockWSSender<>();
        successfulSubscription();

        BidWS altBidWS = new BidWS();
        altBidWS.sender = mockSender;
        altBidWS.onOpen(altMockSession, altMockHttpSession);

        // Subscribe to auction with alt user.
        Auction altRequestAuction = new Auction();
        altRequestAuction.id = auction.id;

        BodyWS altRequestBody = new BodyWS();
        altRequestBody.type = BidWS.TYPE_AUCTION_SUBSCRIBE;
        altRequestBody.nonce = "successful-alt-subscription-nonce";
        altRequestBody.status = 200;
        altRequestBody.json = new Gson().toJson(auction);
        bidWS.onMessage(altMockSession, altRequestBody);
        altBidWS.onMessage(altMockSession, altRequestBody);

        // Close socket connection (aka unsubscribe).
        altBidWS.onClose(altMockSession);

        // Authentication with user and bid.
        mockHttpSession.setUserId(user.id);

        Auction auction = successfulSubscription();
        Auction dbAuction = auctionDAO.getById(auction.id);
        dbAuction.status = Auction.IN_PROGRESS;
        auctionDAO.update(dbAuction);

        User dbUser = userDAO.getById(user.id);
        dbUser.credit = 1000;
        userDAO.update(dbUser);

        Bid attemptBid = DummyGenerator.getDummyBid();
        attemptBid.auctionId = auction.id;
        attemptBid.amount = 1000;
        BodyWS requestBody = new BodyWS();
        requestBody.type = BidWS.TYPE_AUCTION_BID;
        requestBody.nonce = "any";
        requestBody.json = new Gson().toJson(attemptBid);
        bidWS.onMessage(mockSession, requestBody);

        BodyWS replyBody = (BodyWS) mockSender.newObjLastReply;
        Bid dbBid = new Gson().fromJson(replyBody.json, Bid.class);
        assertEquals(user.id, dbBid.ownerId);
        assertEquals(1000, dbBid.amount, 0);
        assertEquals(200, replyBody.status);

        assertEquals(1, mockSender.sessionsLastListSend.size());
        assertTrue(mockSender.sessionsLastListSend.stream().anyMatch(e -> e == mockSession));
    }

    private Auction successfulSubscription() {
        Auction requestAuction = new Auction();
        requestAuction.id = auction.id;

        BodyWS requestBody = new BodyWS();
        requestBody.type = BidWS.TYPE_AUCTION_SUBSCRIBE;
        requestBody.nonce = "successful-subscription-nonce";
        requestBody.status = 200;
        requestBody.json = new Gson().toJson(auction);
        bidWS.onMessage(mockSession, requestBody);

        return requestAuction;
    }
}