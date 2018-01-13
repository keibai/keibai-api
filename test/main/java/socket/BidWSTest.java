package main.java.socket;

import com.google.gson.Gson;
import main.java.ProjectVariables;
import main.java.dao.*;
import main.java.dao.sql.*;
import main.java.mocks.MockHttpSession;
import main.java.mocks.MockSession;
import main.java.mocks.MockWSSender;
import main.java.models.*;
import main.java.models.meta.BodyWS;
import main.java.models.meta.Msg;
import main.java.utils.DBFeeder;
import main.java.utils.DummyGenerator;
import main.java.utils.JsonCommon;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.theories.suppliers.TestedOn;

import javax.jws.soap.SOAPBinding;

import static org.junit.Assert.*;

public class BidWSTest extends AbstractDBTest {

    UserDAO userDAO = UserDAOSQL.getInstance();
    EventDAO eventDAO = EventDAOSQL.getInstance();
    AuctionDAO auctionDAO = AuctionDAOSQL.getInstance();
    GoodDAO goodDAO = GoodDAOSQL.getInstance();
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
        mockSender = new MockWSSender<>();

        BidWS.clearConnected();
        bidWS = new BidWS();
        bidWS.sender = mockSender;
        bidWS.onOpen(mockSession, mockHttpSession);
    }

    /**
     * TYPE_AUCTION_SUBSCRIBE
     */

    @Test
    public void should_not_subscribe_if_not_authenticated() {
        // Deauthenticate user first.
        mockHttpSession.setUserId(-1);

        successfulSubscription();

        BodyWS replyBody = mockSender.newObjLastReply;
        assertEquals(JsonCommon.unauthorized(), replyBody.json);
        assertEquals(400, replyBody.status);
    }

    @Test
    public void should_not_subscribe_if_invalid_json() {
        BodyWS requestBody = new BodyWS();
        requestBody.type = BidWS.TYPE_AUCTION_SUBSCRIBE;
        requestBody.nonce = "any";
        requestBody.status = 200;
        requestBody.json = "{";
        bidWS.onMessage(mockSession, requestBody);

        BodyWS replyBody = mockSender.newObjLastReply;
        assertEquals(400, replyBody.status);
        assertEquals(JsonCommon.invalidRequest(), replyBody.json);
    }

    @Test
    public void should_not_subscribe_if_empty_auction_id() {
        Auction emptyAuction = new Auction();

        BodyWS requestBody = new BodyWS();
        requestBody.type = BidWS.TYPE_AUCTION_SUBSCRIBE;
        requestBody.nonce = "any";
        requestBody.status = 200;
        requestBody.json = new Gson().toJson(emptyAuction);
        bidWS.onMessage(mockSession, requestBody);

        BodyWS replyBody = mockSender.newObjLastReply;
        assertEquals(400, replyBody.status);
        assertEquals(JsonCommon.error(BidWS.AUCTION_ID_ERROR), replyBody.json);
    }

    @Test
    public void should_not_subscribe_if_auction_does_not_exist() {
        Auction nonexistentAuction = new Auction();
        nonexistentAuction.id = 404;

        BodyWS requestBody = new BodyWS();
        requestBody.type = BidWS.TYPE_AUCTION_SUBSCRIBE;
        requestBody.nonce = "any";
        requestBody.status = 200;
        requestBody.json = new Gson().toJson(nonexistentAuction);
        bidWS.onMessage(mockSession, requestBody);

        BodyWS replyBody = mockSender.newObjLastReply;
        assertEquals(400, replyBody.status);
        assertEquals(JsonCommon.error(BidWS.AUCTION_DOES_NOT_EXIST), replyBody.json);
    }

    @Test
    public void bid_subscription_should_complete() {
        Auction requestAuction = new Auction();
        requestAuction.id = auction.id;

        BodyWS requestBody = new BodyWS();
        requestBody.type = BidWS.TYPE_AUCTION_SUBSCRIBE;
        requestBody.nonce = "any";
        requestBody.status = 200;
        requestBody.json = new Gson().toJson(auction);
        bidWS.onMessage(mockSession, requestBody);

        BodyWS replyBody = mockSender.newObjLastReply;
        assertEquals(mockSession, mockSender.sessionLastReply);
        assertEquals(requestBody, mockSender.originObjLastReply);
        assertEquals(200, replyBody.status);
        assertEquals(JsonCommon.ok(), replyBody.json);
    }

    @Test
    public void should_send_new_connection_on_new_subscription() {
        Auction requestAuction = new Auction();
        requestAuction.id = auction.id;

        BodyWS requestBody = new BodyWS();
        requestBody.type = BidWS.TYPE_AUCTION_SUBSCRIBE;
        requestBody.nonce = "any";
        requestBody.status = 200;
        requestBody.json = new Gson().toJson(auction);
        bidWS.onMessage(mockSession, requestBody);

        assertEquals(1, mockSender.sessionsLastListSend.size());
        BodyWS sendBody = mockSender.objLastListSend;
        User sendUser = new Gson().fromJson(sendBody.json, User.class);
        assertEquals(BidWS.TYPE_AUCTION_NEW_CONNECTION, sendBody.type);
        assertEquals(200, sendBody.status);
        assertNotEquals("", sendBody.nonce);
        assertEquals(user.id, sendUser.id);
        assertEquals(user.name, sendUser.name);
        assertEquals(null, sendUser.password);
        assertEquals(0.0, sendUser.credit, 0);
    }

    /**
     * TYPE_AUCTION_SUBSCRIBERS_ONCE
     */

    @Test
    public void should_not_return_subscribers_if_not_subscribed() {
        BodyWS requestBody = new BodyWS();
        requestBody.type = BidWS.TYPE_AUCTION_CONNECTIONS_ONCE;
        bidWS.onMessage(mockSession, requestBody);

        BodyWS replyBody = mockSender.newObjLastReply;
        assertEquals(JsonCommon.error(BidWS.SUBSCRIPTION_ERROR), replyBody.json);
        assertEquals(400, replyBody.status);
    }

    @Test
    public void should_return_subscribers() {
        successfulSubscription();

        BodyWS requestBody = new BodyWS();
        requestBody.type = BidWS.TYPE_AUCTION_CONNECTIONS_ONCE;
        bidWS.onMessage(mockSession, requestBody);

        BodyWS replyBody = mockSender.newObjLastReply;
        Msg replyMsg = new Gson().fromJson(replyBody.json, Msg.class);
        assertEquals(200, replyBody.status);
        assertEquals(1, (int) Integer.valueOf(replyMsg.msg));
    }

    @Test
    public void should_notify_on_disconnection() throws DAOException {
        successfulSubscription();
        mockHttpSession.setUserId(user.id);

        // Alt mocks.
        User altUser = DBFeeder.createOtherDummyUser();
        MockSession altMockSession = new MockSession();
        MockHttpSession altMockHttpSession = new MockHttpSession();
        altMockHttpSession.setUserId(altUser.id);
        MockWSSender<BodyWS> altMockSender = new MockWSSender<>();
        BidWS altBidWS = new BidWS();
        altBidWS.sender = altMockSender;
        altBidWS.onOpen(altMockSession, altMockHttpSession);

        // Alt subscription.
        BodyWS requestBody = new BodyWS();
        requestBody.type = BidWS.TYPE_AUCTION_SUBSCRIBE;
        requestBody.nonce = "alt-successful-subscription";
        requestBody.status = 200;
        requestBody.json = new Gson().toJson(auction);
        altBidWS.onMessage(altMockSession, requestBody);

        // Alt close connection.
        altBidWS.onClose(altMockSession);

        BodyWS altSendBody = altMockSender.objLastListSend;
        User altSendUser = new Gson().fromJson(altSendBody.json, User.class);
        BodyWS sendBody = mockSender.objLastListSend;
        assertEquals(BidWS.TYPE_AUCTION_NEW_DISCONNECTION, altSendBody.type);
        assertEquals(altUser.id, altSendUser.id);
        assertEquals(1, mockSender.sessionsLastListSend.size());
        assertEquals(BidWS.TYPE_AUCTION_NEW_CONNECTION, sendBody.type);
    }

    /**
     * TYPE_AUCTION_BID
     */

    /* ENGLISH */
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

        BodyWS replyBody = mockSender.newObjLastReply;
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

        BodyWS replyBody = mockSender.newObjLastReply;
        assertEquals(JsonCommon.invalidRequest(), replyBody.json);
        assertEquals(400, replyBody.status);
    }

    @Test
    public void should_not_create_bid_with_invalid_amount() {
        Auction auction = successfulSubscription();

        Bid attemptBid = DummyGenerator.getDummyBid();
        attemptBid.auctionId = auction.id;
        attemptBid.amount = 0.005; // Min is 0.01
        Bid[] attemptBids = new Bid[] {attemptBid};
        BodyWS requestBody = new BodyWS();
        requestBody.type = BidWS.TYPE_AUCTION_BID;
        requestBody.nonce = "any";
        requestBody.json = new Gson().toJson(attemptBids);
        bidWS.onMessage(mockSession, requestBody);

        BodyWS replyBody = mockSender.newObjLastReply;
        assertEquals(JsonCommon.error(BidWS.INVALID_AMOUNT_ERROR), replyBody.json);
        assertEquals(400, replyBody.status);
    }

    @Test
    public void should_not_create_bid_if_not_subscribed_to_the_auction() {
        Bid attemptBid = DummyGenerator.getDummyBid();
        attemptBid.amount = 1.0;
        attemptBid.auctionId = auction.id;
        Bid[] attemptBids = new Bid[] {attemptBid};
        BodyWS requestBody = new BodyWS();
        requestBody.type = BidWS.TYPE_AUCTION_BID;
        requestBody.nonce = "any";
        requestBody.json = new Gson().toJson(attemptBids);
        bidWS.onMessage(mockSession, requestBody);

        BodyWS replyBody = mockSender.newObjLastReply;
        assertEquals(JsonCommon.error(BidWS.SUBSCRIPTION_ERROR), replyBody.json);
        assertEquals(400, replyBody.status);
    }

    @Test
    public void should_not_create_bid_if_auction_is_not_in_progress() throws DAOException {
        Auction auction = successfulSubscription();

        Good good = DBFeeder.createDummyGood(auction.id);

        Bid attemptBid = DummyGenerator.getDummyBid();
        attemptBid.auctionId = auction.id;
        attemptBid.goodId = good.id;
        attemptBid.amount = 1.0;
        Bid[] attemptBids = new Bid[] {attemptBid};
        BodyWS requestBody = new BodyWS();
        requestBody.type = BidWS.TYPE_AUCTION_BID;
        requestBody.nonce = "any";
        requestBody.json = new Gson().toJson(attemptBids);
        bidWS.onMessage(mockSession, requestBody);

        BodyWS replyBody = mockSender.newObjLastReply;
        assertEquals(JsonCommon.error(BidWS.AUCTION_NOT_IN_PROGRESS), replyBody.json);
        assertEquals(400, replyBody.status);
    }

    @Test
    public void should_not_create_bid_if_good_does_not_exist() throws DAOException {
        Auction auction = successfulSubscription();
        Auction dbAuction = auctionDAO.getById(auction.id);
        dbAuction.status = Auction.IN_PROGRESS;
        auctionDAO.update(dbAuction);

        Good good = DBFeeder.createDummyGood(auction.id);

        Bid attemptBid = DummyGenerator.getDummyBid();
        attemptBid.auctionId = auction.id;
        attemptBid.goodId = good.id + 1; // Definitely a non-existing good.
        attemptBid.amount = 1.0;
        Bid[] attemptBids = new Bid[] {attemptBid};
        BodyWS requestBody = new BodyWS();
        requestBody.type = BidWS.TYPE_AUCTION_BID;
        requestBody.nonce = "any";
        requestBody.json = new Gson().toJson(attemptBids);
        bidWS.onMessage(mockSession, requestBody);

        BodyWS replyBody = mockSender.newObjLastReply;
        assertEquals(JsonCommon.error(BidWS.GOOD_DOES_NOT_EXIST), replyBody.json);
        assertEquals(400, replyBody.status);
    }

    @Test
    public void should_not_create_bid_if_amount_is_lower_than_starting_price() throws DAOException {
        Auction auction = successfulSubscription();
        Auction dbAuction = auctionDAO.getById(auction.id);
        dbAuction.status = Auction.IN_PROGRESS;
        auctionDAO.update(dbAuction);

        Good good = DBFeeder.createDummyGood(auction.id);

        Bid attemptBid = DummyGenerator.getDummyBid();
        attemptBid.auctionId = auction.id;
        attemptBid.goodId = good.id;
        attemptBid.amount = dbAuction.startingPrice - 0.01;
        Bid[] attemptBids = new Bid[] {attemptBid};
        BodyWS requestBody = new BodyWS();
        requestBody.type = BidWS.TYPE_AUCTION_BID;
        requestBody.nonce = "any";
        requestBody.json = new Gson().toJson(attemptBids);
        bidWS.onMessage(mockSession, requestBody);

        BodyWS replyBody = mockSender.newObjLastReply;
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

        Good good = DBFeeder.createDummyGood(auction.id);

        Bid attemptBid = DummyGenerator.getDummyBid();
        attemptBid.auctionId = auction.id;
        attemptBid.goodId = good.id;
        attemptBid.amount = dbAuction.startingPrice;
        Bid[] attemptBids = new Bid[] {attemptBid};
        BodyWS requestBody = new BodyWS();
        requestBody.type = BidWS.TYPE_AUCTION_BID;
        requestBody.nonce = "any";
        requestBody.json = new Gson().toJson(attemptBids);
        bidWS.onMessage(mockSession, requestBody);

        BodyWS replyBody = mockSender.newObjLastReply;
        assertEquals(JsonCommon.error(BidWS.NO_CREDIT), replyBody.json);
        assertEquals(400, replyBody.status);
    }

    @Test
    public void should_not_create_if_there_is_a_higher_bid_by_himself() throws DAOException {
        Auction auction = successfulSubscription();
        Auction dbAuction = auctionDAO.getById(auction.id);
        dbAuction.status = Auction.IN_PROGRESS;
        auctionDAO.update(dbAuction);

        Good good = DBFeeder.createDummyGood(auction.id);

        User dbUser = userDAO.getById(user.id);
        dbUser.credit = 2000;
        userDAO.update(dbUser);

        // First bid @ 2000.
        Bid attemptBid1 = DummyGenerator.getDummyBid();
        attemptBid1.auctionId = auction.id;
        attemptBid1.goodId = good.id;
        attemptBid1.amount = 2000;
        Bid[] attemptBids1 = new Bid[] {attemptBid1};
        BodyWS requestBody = new BodyWS();
        requestBody.type = BidWS.TYPE_AUCTION_BID;
        requestBody.nonce = "first";
        requestBody.json = new Gson().toJson(attemptBids1);
        bidWS.onMessage(mockSession, requestBody);

        BodyWS replyBody1 = mockSender.newObjLastReply;
        Bid dbBid1 = new Gson().fromJson(replyBody1.json, Bid.class);
        assertEquals(user.id, dbBid1.ownerId);
        assertEquals(2000, dbBid1.amount, 0);
        assertEquals(200, replyBody1.status);

        // Second bid @ 1000.
        Bid attemptBid2 = DummyGenerator.getDummyBid();
        attemptBid2.auctionId = auction.id;
        attemptBid2.goodId = good.id;
        attemptBid2.amount = 1000;
        Bid[] attemptBids2 = new Bid[] {attemptBid2};
        BodyWS requestBody2 = new BodyWS();
        requestBody2.type = BidWS.TYPE_AUCTION_BID;
        requestBody2.nonce = "second";
        requestBody2.json = new Gson().toJson(attemptBids2);
        bidWS.onMessage(mockSession, requestBody2);

        BodyWS replyBody2 = mockSender.newObjLastReply;
        assertEquals(JsonCommon.error(BidWS.LOW_BID_HIGHER_BID), replyBody2.json);
        assertEquals(400, replyBody2.status);
    }

    @Test
    public void should_not_create_if_there_is_a_higher_bid_by_another_user() throws DAOException {
        Auction auction = successfulSubscription();
        Auction dbAuction = auctionDAO.getById(auction.id);
        dbAuction.status = Auction.IN_PROGRESS;
        auctionDAO.update(dbAuction);

        Good good = DBFeeder.createDummyGood(auction.id);

        User dbUser = userDAO.getById(user.id);
        dbUser.credit = 2000;
        userDAO.update(dbUser);

        User altUser = DBFeeder.createOtherDummyUser();
        altUser.credit = 2000;
        userDAO.update(altUser);

        // User bids @ 1000.
        Bid attemptBid1 = DummyGenerator.getDummyBid();
        attemptBid1.auctionId = auction.id;
        attemptBid1.goodId = good.id;
        attemptBid1.amount = 1000;
        Bid[] attemptBids1 = new Bid[] {attemptBid1};
        BodyWS requestBody = new BodyWS();
        requestBody.type = BidWS.TYPE_AUCTION_BID;
        requestBody.nonce = "first";
        requestBody.json = new Gson().toJson(attemptBids1);
        bidWS.onMessage(mockSession, requestBody);

        BodyWS replyBody1 = mockSender.newObjLastReply;
        Bid dbBid1 = new Gson().fromJson(replyBody1.json, Bid.class);
        assertEquals(user.id, dbBid1.ownerId);
        assertEquals(1000, dbBid1.amount, 0);
        assertEquals(200, replyBody1.status);

        // Alt user authenticates.
        mockHttpSession.setUserId(altUser.id);

        // Alt user bids @ 1000.
        Bid attemptBid2 = DummyGenerator.getDummyBid();
        attemptBid2.auctionId = auction.id;
        attemptBid2.goodId = good.id;
        attemptBid2.amount = 1000;
        Bid[] attemptBids2 = new Bid[] {attemptBid2};
        BodyWS requestBody2 = new BodyWS();
        requestBody2.type = BidWS.TYPE_AUCTION_BID;
        requestBody2.nonce = "second";
        requestBody2.json = new Gson().toJson(attemptBids2);
        bidWS.onMessage(mockSession, requestBody2);

        BodyWS replyBody2 = mockSender.newObjLastReply;
        assertEquals(JsonCommon.error(BidWS.LOW_BID_HIGHER_BID), replyBody2.json);
        assertEquals(400, replyBody2.status);

        // Alt user bids @ 2000.
        Bid attemptBid3 = DummyGenerator.getDummyBid();
        attemptBid3.auctionId = auction.id;
        attemptBid3.goodId = good.id;
        attemptBid3.amount = 2000;
        Bid[] attemptBids3 = new Bid[] {attemptBid3};
        BodyWS requestBody3 = new BodyWS();
        requestBody3.type = BidWS.TYPE_AUCTION_BID;
        requestBody3.nonce = "third";
        requestBody3.json = new Gson().toJson(attemptBids3);
        bidWS.onMessage(mockSession, requestBody3);

        BodyWS replyBody3 = mockSender.newObjLastReply;
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

        Good good = DBFeeder.createDummyGood(auction.id);

        Bid attemptBid1 = DummyGenerator.getDummyBid();
        attemptBid1.auctionId = auction.id;
        attemptBid1.goodId = good.id;
        attemptBid1.amount = 500.005; // Last decimal will be truncated.
        Bid[] attemptBids1 = new Bid[] {attemptBid1};
        BodyWS requestBody = new BodyWS();
        requestBody.type = BidWS.TYPE_AUCTION_BID;
        requestBody.nonce = "first";
        requestBody.json = new Gson().toJson(attemptBids1);
        bidWS.onMessage(mockSession, requestBody);

        BodyWS replyBody1 = mockSender.newObjLastReply;
        Bid dbBid1 = new Gson().fromJson(replyBody1.json, Bid.class);
        assertEquals(user.id, dbBid1.ownerId);
        assertEquals(500, dbBid1.amount, 0);
        assertEquals(200, replyBody1.status);

        Auction updatedAuction = auctionDAO.getById(auction.id);
        assertEquals(dbBid1.amount, updatedAuction.maxBid, 0.0000001);
        assertEquals(dbAuction.startingPrice, updatedAuction.startingPrice, 0.0000001);
        assertNotEquals(updatedAuction.startingPrice, updatedAuction.maxBid);
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

        Good good = DBFeeder.createDummyGood(auction.id);

        // First bid @ 1000.
        Bid attemptBid1 = DummyGenerator.getDummyBid();
        attemptBid1.auctionId = auction.id;
        attemptBid1.goodId = good.id;
        attemptBid1.amount = 1000;
        Bid[] attemptBids1 = new Bid[] {attemptBid1};
        BodyWS requestBody1 = new BodyWS();
        requestBody1.type = BidWS.TYPE_AUCTION_BID;
        requestBody1.nonce = "first";
        requestBody1.json = new Gson().toJson(attemptBids1);
        bidWS.onMessage(mockSession, requestBody1);

        BodyWS replyBody1 = mockSender.newObjLastReply;
        Bid dbBid1 = new Gson().fromJson(replyBody1.json, Bid.class);
        assertEquals(user.id, dbBid1.ownerId);
        assertEquals(1000, dbBid1.amount, 0);
        assertEquals(200, replyBody1.status);

        // Second bid @ 2000.
        Bid attemptBid2 = DummyGenerator.getDummyBid();
        attemptBid2.auctionId = auction.id;
        attemptBid2.goodId = good.id;
        attemptBid2.amount = 2000;
        Bid[] attemptBids2 = new Bid[] {attemptBid2};
        BodyWS requestBody2 = new BodyWS();
        requestBody2.type = BidWS.TYPE_AUCTION_BID;
        requestBody2.nonce = "second";
        requestBody2.json = new Gson().toJson(attemptBids2);
        bidWS.onMessage(mockSession, requestBody2);

        BodyWS replyBody2 = mockSender.newObjLastReply;
        Bid dbBid2 = new Gson().fromJson(replyBody2.json, Bid.class);
        assertEquals(user.id, dbBid2.ownerId);
        assertEquals(2000, dbBid2.amount, 0);
        assertEquals(200, replyBody2.status);

        Auction updatedAuction = auctionDAO.getById(auction.id);
        assertEquals(dbBid2.amount, updatedAuction.maxBid, 0.0000001);
        assertEquals(dbAuction.startingPrice, updatedAuction.startingPrice, 0.0000001);
        assertNotEquals(updatedAuction.startingPrice, updatedAuction.maxBid);
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

        Good good = DBFeeder.createDummyGood(auction.id);

        Bid attemptBid = DummyGenerator.getDummyBid();
        attemptBid.auctionId = auction.id;
        attemptBid.goodId = good.id;
        attemptBid.amount = 1000;
        Bid[] attemptBids = new Bid[] {attemptBid};
        BodyWS requestBody = new BodyWS();
        requestBody.type = BidWS.TYPE_AUCTION_BID;
        requestBody.nonce = "any";
        requestBody.json = new Gson().toJson(attemptBids);
        bidWS.onMessage(mockSession, requestBody);

        BodyWS replyBody = mockSender.newObjLastReply;
        Bid dbBid = new Gson().fromJson(replyBody.json, Bid.class);
        assertEquals(user.id, dbBid.ownerId);
        assertEquals(1000, dbBid.amount, 0);
        assertEquals(200, replyBody.status);

        assertEquals(2, mockSender.sessionsLastListSend.size());
        assertNotEquals(mockSender.sessionsLastListSend.get(0), mockSender.sessionsLastListSend.get(1));
        assertTrue(mockSender.sessionsLastListSend.stream().anyMatch(e -> e == altMockSession));
        assertTrue(mockSender.sessionsLastListSend.stream().anyMatch(e -> e == mockSession));
        assertEquals(BidWS.TYPE_AUCTION_BIDDED, mockSender.objLastListSend.type);
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

        Good good = DBFeeder.createDummyGood(auction.id);

        Bid attemptBid = DummyGenerator.getDummyBid();
        attemptBid.auctionId = auction.id;
        attemptBid.goodId = good.id;
        attemptBid.amount = 1000;
        Bid[] attemptBids = new Bid[] {attemptBid};
        BodyWS requestBody = new BodyWS();
        requestBody.type = BidWS.TYPE_AUCTION_BID;
        requestBody.nonce = "any";
        requestBody.json = new Gson().toJson(attemptBids);
        bidWS.onMessage(mockSession, requestBody);

        BodyWS replyBody = mockSender.newObjLastReply;
        Bid dbBid = new Gson().fromJson(replyBody.json, Bid.class);
        assertEquals(user.id, dbBid.ownerId);
        assertEquals(1000, dbBid.amount, 0);
        assertEquals(200, replyBody.status);

        assertEquals(1, mockSender.sessionsLastListSend.size());
        assertTrue(mockSender.sessionsLastListSend.stream().anyMatch(e -> e == mockSession));
    }

    @Test
    public void should_not_create_bid_if_they_are_currently_bidding_on_another_auction() throws DAOException {
        Auction auction = successfulSubscription();
        Auction dbAuction = auctionDAO.getById(auction.id);
        dbAuction.status = Auction.IN_PROGRESS;
        auctionDAO.update(dbAuction);

        User dbUser = userDAO.getById(user.id);
        dbUser.credit = 9999;
        userDAO.update(dbUser);

        Good good = DBFeeder.createDummyGood(auction.id);

        Bid attemptBid1 = DummyGenerator.getDummyBid();
        attemptBid1.auctionId = auction.id;
        attemptBid1.goodId = good.id;
        attemptBid1.amount = 1000;
        Bid[] attemptBids1 = new Bid[] {attemptBid1};
        BodyWS requestBody = new BodyWS();
        requestBody.type = BidWS.TYPE_AUCTION_BID;
        requestBody.nonce = "first";
        requestBody.json = new Gson().toJson(attemptBids1);
        bidWS.onMessage(mockSession, requestBody);

        BodyWS replyBody1 = mockSender.newObjLastReply;
        Bid dbBid1 = new Gson().fromJson(replyBody1.json, Bid.class);
        assertEquals(user.id, dbBid1.ownerId);
        assertEquals(1000, dbBid1.amount, 0);
        assertEquals(200, replyBody1.status);

        // Join another auction and bid on it while the first one is in progress.
        Auction altAuction = DummyGenerator.getOtherDummyAuction();
        altAuction.ownerId = dbUser.id;
        altAuction.eventId = dbAuction.eventId;
        altAuction.status = Auction.IN_PROGRESS;
        Auction dbAltAuction = auctionDAO.create(altAuction);

        successfulSubscription(dbAltAuction);

        Good altGood = DBFeeder.createOtherDummyGood(auction.id);

        Bid attemptBid2 = DummyGenerator.getDummyBid();
        attemptBid2.auctionId = dbAltAuction.id;
        attemptBid2.goodId = altGood.id;
        attemptBid2.amount = 1000;
        Bid[] attemptBids2 = new Bid[] {attemptBid2};
        BodyWS requestBody2 = new BodyWS();
        requestBody2.type = BidWS.TYPE_AUCTION_BID;
        requestBody2.nonce = "second";
        requestBody2.json = new Gson().toJson(attemptBids2);
        bidWS.onMessage(mockSession, requestBody2);

        BodyWS replyBody2 = mockSender.newObjLastReply;
        assertEquals(JsonCommon.error(BidWS.HAS_BIDDED_IN_IN_PROGRESS_AUCTION_TRYING_TO_BID_ANOTHER), replyBody2.json);
        assertEquals(400, replyBody2.status);
    }

    /* COMBINATORIAL */
    @Test
    public void cannot_create_combinatorial_bid_if_bids_amount_less_than_1() throws DAOException {
        Auction auction = successfulSubscription();
        Auction dbAuction = auctionDAO.getById(auction.id);
        dbAuction.status = Auction.IN_PROGRESS;
        auctionDAO.update(dbAuction);

        Event dbEvent = eventDAO.getById(dbAuction.eventId);
        dbEvent.auctionType = Event.COMBINATORIAL;
        dbEvent = eventDAO.update(dbEvent);

        Good good = DBFeeder.createDummyGood(auction.id);

        Bid attemptBid = DummyGenerator.getDummyBid();
        attemptBid.auctionId = auction.id;
        attemptBid.goodId = good.id;
        attemptBid.amount = 0.1;
        Bid[] attemptBids = new Bid[] {attemptBid};
        BodyWS requestBody = new BodyWS();
        requestBody.type = BidWS.TYPE_AUCTION_BID;
        requestBody.nonce = "any";
        requestBody.json = new Gson().toJson(attemptBids);
        bidWS.onMessage(mockSession, requestBody);

        BodyWS replyBody = mockSender.newObjLastReply;
        assertEquals(JsonCommon.error(BidWS.INVALID_AMOUNT_ERROR), replyBody.json);
        assertEquals(400, replyBody.status);
    }

    @Test
    public void cannot_create_combinatorial_bid_if_bids_have_different_amounts() throws DAOException {
        Auction auction = successfulSubscription();
        Auction dbAuction = auctionDAO.getById(auction.id);
        dbAuction.status = Auction.IN_PROGRESS;
        auctionDAO.update(dbAuction);

        Event dbEvent = eventDAO.getById(dbAuction.eventId);
        dbEvent.auctionType = Event.COMBINATORIAL;
        dbEvent = eventDAO.update(dbEvent);

        Good good = DBFeeder.createDummyGood(auction.id);
        Good otherGood = DBFeeder.createOtherDummyGood(auction.id);

        Bid attemptBid1 = DummyGenerator.getDummyBid();
        attemptBid1.auctionId = auction.id;
        attemptBid1.goodId = good.id;
        attemptBid1.amount = 10;
        Bid attemptBid2 = DummyGenerator.getOtherDummyBid();
        attemptBid2.auctionId = auction.id;
        attemptBid2.goodId = otherGood.id;
        attemptBid2.amount = 100;
        Bid[] attemptBids = new Bid[] {attemptBid1, attemptBid2};
        BodyWS requestBody = new BodyWS();
        requestBody.type = BidWS.TYPE_AUCTION_BID;
        requestBody.nonce = "any";
        requestBody.json = new Gson().toJson(attemptBids);
        bidWS.onMessage(mockSession, requestBody);

        BodyWS replyBody = mockSender.newObjLastReply;
        assertEquals(JsonCommon.error(BidWS.DIFFERENT_AMOUNTS), replyBody.json);
        assertEquals(400, replyBody.status);
    }

    @Test
    public void cannot_create_combinatorial_bid_if_bids_have_different_auction_ids() throws DAOException {
        Auction auction = successfulSubscription();
        Auction dbAuction = auctionDAO.getById(auction.id);
        dbAuction.status = Auction.IN_PROGRESS;
        auctionDAO.update(dbAuction);

        Event dbEvent = eventDAO.getById(dbAuction.eventId);
        dbEvent.auctionType = Event.COMBINATORIAL;
        dbEvent = eventDAO.update(dbEvent);

        Auction otherAuction = DBFeeder.createOtherDummyAuction(dbEvent.id, dbAuction.ownerId);

        Good good = DBFeeder.createDummyGood(auction.id);
        Good otherGood = DBFeeder.createOtherDummyGood(auction.id);

        Bid attemptBid1 = DummyGenerator.getDummyBid();
        attemptBid1.auctionId = auction.id;
        attemptBid1.goodId = good.id;
        attemptBid1.amount = 100;
        Bid attemptBid2 = DummyGenerator.getOtherDummyBid();
        attemptBid2.auctionId = otherAuction.id;
        attemptBid2.goodId = otherGood.id;
        attemptBid2.amount = 100;
        Bid[] attemptBids = new Bid[] {attemptBid1, attemptBid2};
        BodyWS requestBody = new BodyWS();
        requestBody.type = BidWS.TYPE_AUCTION_BID;
        requestBody.nonce = "any";
        requestBody.json = new Gson().toJson(attemptBids);
        bidWS.onMessage(mockSession, requestBody);

        BodyWS replyBody = mockSender.newObjLastReply;
        assertEquals(JsonCommon.error(BidWS.DIFFERENT_AUCTIONS), replyBody.json);
        assertEquals(400, replyBody.status);
    }

    @Test
    public void cannot_create_combinatorial_bid_if_some_good_id_is_0() throws DAOException {
        Auction auction = successfulSubscription();
        Auction dbAuction = auctionDAO.getById(auction.id);
        dbAuction.status = Auction.IN_PROGRESS;
        auctionDAO.update(dbAuction);

        Event dbEvent = eventDAO.getById(dbAuction.eventId);
        dbEvent.auctionType = Event.COMBINATORIAL;
        dbEvent = eventDAO.update(dbEvent);

        Good good = DBFeeder.createDummyGood(auction.id);
        Good otherGood = DBFeeder.createOtherDummyGood(auction.id);

        Bid attemptBid1 = DummyGenerator.getDummyBid();
        attemptBid1.auctionId = auction.id;
        attemptBid1.goodId = good.id;
        attemptBid1.amount = 100;
        Bid attemptBid2 = DummyGenerator.getOtherDummyBid();
        attemptBid2.auctionId = auction.id;
        attemptBid2.goodId = 0;
        attemptBid2.amount = 100;
        Bid[] attemptBids = new Bid[] {attemptBid1, attemptBid2};
        BodyWS requestBody = new BodyWS();
        requestBody.type = BidWS.TYPE_AUCTION_BID;
        requestBody.nonce = "any";
        requestBody.json = new Gson().toJson(attemptBids);
        bidWS.onMessage(mockSession, requestBody);

        BodyWS replyBody = mockSender.newObjLastReply;
        assertEquals(JsonCommon.error(BidWS.GOOD_ID_ERROR), replyBody.json);
        assertEquals(400, replyBody.status);
    }

    @Test
    public void cannot_create_combinatorial_bid_if_some_good_does_not_exist() throws DAOException {
        Auction auction = successfulSubscription();
        Auction dbAuction = auctionDAO.getById(auction.id);
        dbAuction.status = Auction.IN_PROGRESS;
        auctionDAO.update(dbAuction);

        Event dbEvent = eventDAO.getById(dbAuction.eventId);
        dbEvent.auctionType = Event.COMBINATORIAL;
        dbEvent = eventDAO.update(dbEvent);

        Good good = DBFeeder.createDummyGood(auction.id);

        Bid attemptBid1 = DummyGenerator.getDummyBid();
        attemptBid1.auctionId = auction.id;
        attemptBid1.goodId = good.id;
        attemptBid1.amount = 100;
        Bid attemptBid2 = DummyGenerator.getOtherDummyBid();
        attemptBid2.auctionId = auction.id;
        attemptBid2.goodId = 1234;
        attemptBid2.amount = 100;
        Bid[] attemptBids = new Bid[] {attemptBid1, attemptBid2};
        BodyWS requestBody = new BodyWS();
        requestBody.type = BidWS.TYPE_AUCTION_BID;
        requestBody.nonce = "any";
        requestBody.json = new Gson().toJson(attemptBids);
        bidWS.onMessage(mockSession, requestBody);

        BodyWS replyBody = mockSender.newObjLastReply;
        assertEquals(JsonCommon.error(BidWS.GOOD_DOES_NOT_EXIST), replyBody.json);
        assertEquals(400, replyBody.status);
    }

    @Test
    public void cannot_create_combinatorial_bid_if_auction_does_not_exist() throws DAOException {
        Auction auction = successfulSubscription();
        Auction dbAuction = auctionDAO.getById(auction.id);
        dbAuction.status = Auction.IN_PROGRESS;
        auctionDAO.update(dbAuction);

        Event dbEvent = eventDAO.getById(dbAuction.eventId);
        dbEvent.auctionType = Event.COMBINATORIAL;
        dbEvent = eventDAO.update(dbEvent);

        Good good = DBFeeder.createDummyGood(auction.id);
        Good otherGood = DBFeeder.createOtherDummyGood(auction.id);

        Bid attemptBid1 = DummyGenerator.getDummyBid();
        attemptBid1.auctionId = 1234;
        attemptBid1.goodId = good.id;
        attemptBid1.amount = 100;
        Bid attemptBid2 = DummyGenerator.getOtherDummyBid();
        attemptBid2.auctionId = 1234;
        attemptBid2.goodId = otherGood.id;
        attemptBid2.amount = 100;
        Bid[] attemptBids = new Bid[] {attemptBid1, attemptBid2};
        BodyWS requestBody = new BodyWS();
        requestBody.type = BidWS.TYPE_AUCTION_BID;
        requestBody.nonce = "any";
        requestBody.json = new Gson().toJson(attemptBids);
        bidWS.onMessage(mockSession, requestBody);

        BodyWS replyBody = mockSender.newObjLastReply;
        assertEquals(JsonCommon.error(BidWS.AUCTION_DOES_NOT_EXIST), replyBody.json);
        assertEquals(400, replyBody.status);
    }

    @Test
    public void cannot_create_combinatorial_bid_if_auction_not_subscribed() throws DAOException {
        Auction dbAuction = auctionDAO.getById(auction.id);
        dbAuction.status = Auction.IN_PROGRESS;
        auctionDAO.update(dbAuction);

        Event dbEvent = eventDAO.getById(dbAuction.eventId);
        dbEvent.auctionType = Event.COMBINATORIAL;
        dbEvent = eventDAO.update(dbEvent);

        Good good = DBFeeder.createDummyGood(auction.id);
        Good otherGood = DBFeeder.createOtherDummyGood(auction.id);

        Bid attemptBid1 = DummyGenerator.getDummyBid();
        attemptBid1.auctionId = auction.id;
        attemptBid1.goodId = good.id;
        attemptBid1.amount = 100;
        Bid attemptBid2 = DummyGenerator.getOtherDummyBid();
        attemptBid2.auctionId = auction.id;
        attemptBid2.goodId = otherGood.id;
        attemptBid2.amount = 100;
        Bid[] attemptBids = new Bid[] {attemptBid1, attemptBid2};
        BodyWS requestBody = new BodyWS();
        requestBody.type = BidWS.TYPE_AUCTION_BID;
        requestBody.nonce = "any";
        requestBody.json = new Gson().toJson(attemptBids);
        bidWS.onMessage(mockSession, requestBody);

        BodyWS replyBody = mockSender.newObjLastReply;
        assertEquals(JsonCommon.error(BidWS.SUBSCRIPTION_ERROR), replyBody.json);
        assertEquals(400, replyBody.status);
    }

    @Test
    public void cannot_create_combinatorial_bid_if_auction_not_in_progress() throws DAOException {
        Auction auction = successfulSubscription();
        Auction dbAuction = auctionDAO.getById(auction.id);
        dbAuction.status = Auction.ACCEPTED;
        auctionDAO.update(dbAuction);

        Event dbEvent = eventDAO.getById(dbAuction.eventId);
        dbEvent.auctionType = Event.COMBINATORIAL;
        dbEvent = eventDAO.update(dbEvent);

        Good good = DBFeeder.createDummyGood(auction.id);
        Good otherGood = DBFeeder.createOtherDummyGood(auction.id);

        Bid attemptBid1 = DummyGenerator.getDummyBid();
        attemptBid1.auctionId = auction.id;
        attemptBid1.goodId = good.id;
        attemptBid1.amount = 100;
        Bid attemptBid2 = DummyGenerator.getOtherDummyBid();
        attemptBid2.auctionId = auction.id;
        attemptBid2.goodId = otherGood.id;
        attemptBid2.amount = 100;
        Bid[] attemptBids = new Bid[] {attemptBid1, attemptBid2};
        BodyWS requestBody = new BodyWS();
        requestBody.type = BidWS.TYPE_AUCTION_BID;
        requestBody.nonce = "any";
        requestBody.json = new Gson().toJson(attemptBids);
        bidWS.onMessage(mockSession, requestBody);

        BodyWS replyBody = mockSender.newObjLastReply;
        assertEquals(JsonCommon.error(BidWS.AUCTION_NOT_IN_PROGRESS), replyBody.json);
        assertEquals(400, replyBody.status);
    }

    @Test
    public void cannot_create_combinatorial_bid_if_user_already_bidded() throws DAOException {
        Auction auction = successfulSubscription();
        Auction dbAuction = auctionDAO.getById(auction.id);
        dbAuction.status = Auction.IN_PROGRESS;
        auctionDAO.update(dbAuction);

        Event dbEvent = eventDAO.getById(dbAuction.eventId);
        dbEvent.auctionType = Event.COMBINATORIAL;
        dbEvent = eventDAO.update(dbEvent);

        Good good = DBFeeder.createDummyGood(auction.id);
        Good otherGood = DBFeeder.createOtherDummyGood(auction.id);

        Bid pastBid = DBFeeder.createOtherDummyBid(dbAuction.id, user.id, good.id);

        Bid attemptBid1 = DummyGenerator.getDummyBid();
        attemptBid1.auctionId = auction.id;
        attemptBid1.goodId = good.id;
        attemptBid1.amount = 100;
        Bid attemptBid2 = DummyGenerator.getOtherDummyBid();
        attemptBid2.auctionId = auction.id;
        attemptBid2.goodId = otherGood.id;
        attemptBid2.amount = 100;
        Bid[] attemptBids = new Bid[] {attemptBid1, attemptBid2};
        BodyWS requestBody = new BodyWS();
        requestBody.type = BidWS.TYPE_AUCTION_BID;
        requestBody.nonce = "any";
        requestBody.json = new Gson().toJson(attemptBids);
        bidWS.onMessage(mockSession, requestBody);

        BodyWS replyBody = mockSender.newObjLastReply;
        assertEquals(JsonCommon.error(BidWS.USER_ALREADY_BIDDED), replyBody.json);
        assertEquals(400, replyBody.status);
    }

    @Test
    public void cannot_create_combinatorial_bid_if_user_has_not_enough_credit() throws DAOException {
        Auction auction = successfulSubscription();
        Auction dbAuction = auctionDAO.getById(auction.id);
        dbAuction.status = Auction.IN_PROGRESS;
        auctionDAO.update(dbAuction);

        User user = userDAO.getById(this.user.id);
        user.credit = 50;
        user = userDAO.update(user);

        Event dbEvent = eventDAO.getById(dbAuction.eventId);
        dbEvent.auctionType = Event.COMBINATORIAL;
        dbEvent = eventDAO.update(dbEvent);

        Good good = DBFeeder.createDummyGood(auction.id);
        Good otherGood = DBFeeder.createOtherDummyGood(auction.id);

        Bid attemptBid1 = DummyGenerator.getDummyBid();
        attemptBid1.auctionId = auction.id;
        attemptBid1.goodId = good.id;
        attemptBid1.amount = 100;
        Bid attemptBid2 = DummyGenerator.getOtherDummyBid();
        attemptBid2.auctionId = auction.id;
        attemptBid2.goodId = otherGood.id;
        attemptBid2.amount = 100;
        Bid[] attemptBids = new Bid[] {attemptBid1, attemptBid2};
        BodyWS requestBody = new BodyWS();
        requestBody.type = BidWS.TYPE_AUCTION_BID;
        requestBody.nonce = "any";
        requestBody.json = new Gson().toJson(attemptBids);
        bidWS.onMessage(mockSession, requestBody);

        BodyWS replyBody = mockSender.newObjLastReply;
        assertEquals(JsonCommon.error(BidWS.NO_CREDIT), replyBody.json);
        assertEquals(400, replyBody.status);
    }

    @Test
    public void combinatorial_bid_successfully_created() throws DAOException {
        Auction auction = successfulSubscription();
        Auction dbAuction = auctionDAO.getById(auction.id);
        dbAuction.status = Auction.IN_PROGRESS;
        auctionDAO.update(dbAuction);

        Event dbEvent = eventDAO.getById(dbAuction.eventId);
        dbEvent.auctionType = Event.COMBINATORIAL;
        dbEvent = eventDAO.update(dbEvent);

        User user = userDAO.getById(this.user.id);
        user.credit = 200;
        user = userDAO.update(user);

        Good good = DBFeeder.createDummyGood(auction.id);
        Good otherGood = DBFeeder.createOtherDummyGood(auction.id);

        Bid attemptBid1 = DummyGenerator.getDummyBid();
        attemptBid1.auctionId = auction.id;
        attemptBid1.goodId = good.id;
        attemptBid1.amount = 100;
        Bid attemptBid2 = DummyGenerator.getOtherDummyBid();
        attemptBid2.auctionId = auction.id;
        attemptBid2.goodId = otherGood.id;
        attemptBid2.amount = 100;
        Bid[] attemptBids = new Bid[] {attemptBid1, attemptBid2};
        BodyWS requestBody = new BodyWS();
        requestBody.type = BidWS.TYPE_AUCTION_BID;
        requestBody.nonce = "any";
        requestBody.json = new Gson().toJson(attemptBids);
        bidWS.onMessage(mockSession, requestBody);

        BodyWS replyBody = mockSender.newObjLastReply;
        Bid replyBid = new Gson().fromJson(replyBody.json, Bid.class);
        assertEquals(0, replyBid.amount, 0.0000001);
        assertEquals(attemptBid2.goodId, replyBid.goodId);
        assertEquals(attemptBid2.auctionId, replyBid.auctionId);
        assertEquals(user.id, replyBid.auctionId);
        assertNotNull(replyBid.createdAt);
        assertEquals(200, replyBody.status);
    }

    /**
     * TYPE_AUCTION_START
     */

    @Test
    public void cannot_start_if_unauthenticated() {
        Auction auction = successfulSubscription();

        // Deauthenticate user first.
        mockHttpSession.setUserId(-1);

        BodyWS requestBody = new BodyWS();
        requestBody.type = BidWS.TYPE_AUCTION_START;
        requestBody.nonce = "any";
        requestBody.json = new Gson().toJson(auction);
        bidWS.onMessage(mockSession, requestBody);

        BodyWS replyBody = mockSender.newObjLastReply;
        assertEquals(JsonCommon.unauthorized(), replyBody.json);
        assertEquals(400, replyBody.status);
    }

    @Test
    public void cannot_start_if_invalid_json() {
        Auction auction = successfulSubscription();

        BodyWS requestBody = new BodyWS();
        requestBody.type = BidWS.TYPE_AUCTION_START;
        requestBody.nonce = "any";
        requestBody.json = "{";
        bidWS.onMessage(mockSession, requestBody);

        BodyWS replyBody = mockSender.newObjLastReply;
        assertEquals(JsonCommon.invalidRequest(), replyBody.json);
        assertEquals(400, replyBody.status);
    }

    @Test
    public void cannot_accept_if_not_subscribed_to_that_auction() {
        BodyWS requestBody = new BodyWS();
        requestBody.type = BidWS.TYPE_AUCTION_START;
        requestBody.nonce = "any";
        requestBody.json = new Gson().toJson(auction);
        bidWS.onMessage(mockSession, requestBody);

        BodyWS replyBody = mockSender.newObjLastReply;
        assertEquals(JsonCommon.error(BidWS.SUBSCRIPTION_ERROR), replyBody.json);
        assertEquals(400, replyBody.status);
    }

    @Test
    public void auction_not_accepted_can_not_be_started() throws DAOException {
        Auction auction = successfulSubscription();

        Auction dbAuction = auctionDAO.getById(auction.id);
        dbAuction.status = Auction.PENDING;
        auctionDAO.update(dbAuction);

        BodyWS requestBody = new BodyWS();
        requestBody.type = BidWS.TYPE_AUCTION_START;
        requestBody.nonce = "any";
        requestBody.json = new Gson().toJson(auction);
        bidWS.onMessage(mockSession, requestBody);

        BodyWS replyBody = mockSender.newObjLastReply;
        assertEquals(JsonCommon.error(BidWS.WRONG_AUCTION_STATUS), replyBody.json);
        assertEquals(400, replyBody.status);
    }

    @Test
    public void auction_cannot_be_started_if_user_is_not_the_owner() throws Exception {
        User altUser = DBFeeder.createOtherDummyUser();

        Auction auction = successfulSubscription();

        Event dbEvent = eventDAO.getById(event.id);
        dbEvent.ownerId = altUser.id;
        eventDAO.update(dbEvent);

        Auction dbAuction = auctionDAO.getById(auction.id);
        dbAuction.status = Auction.ACCEPTED;
        auctionDAO.update(dbAuction);

        BodyWS requestBody = new BodyWS();
        requestBody.type = BidWS.TYPE_AUCTION_START;
        requestBody.nonce = "any";
        requestBody.json = new Gson().toJson(auction);
        bidWS.onMessage(mockSession, requestBody);

        BodyWS replyBody = mockSender.newObjLastReply;
        assertEquals(JsonCommon.unauthorized(), replyBody.json);
        assertEquals(400, replyBody.status);
    }

    @Test
    public void cannot_start_auction_with_finished_event() throws DAOException {
        User altUser = DBFeeder.createOtherDummyUser();

        Auction auction = successfulSubscription();

        Event dbEvent = eventDAO.getById(event.id);
        dbEvent.status = Event.FINISHED;
        eventDAO.update(dbEvent);

        Auction dbAuction = auctionDAO.getById(auction.id);
        dbAuction.status = Auction.ACCEPTED;
        auctionDAO.update(dbAuction);

        BodyWS requestBody = new BodyWS();
        requestBody.type = BidWS.TYPE_AUCTION_START;
        requestBody.nonce = "any";
        requestBody.json = new Gson().toJson(auction);
        bidWS.onMessage(mockSession, requestBody);

        BodyWS replyBody = mockSender.newObjLastReply;
        assertEquals(JsonCommon.error(BidWS.EVENT_FINISHED), replyBody.json);
        assertEquals(400, replyBody.status);
    }

    @Test
    public void cannot_start_auction_if_not_all_auctions_have_been_accepted() throws DAOException {
        Auction auction = successfulSubscription();

        Event dbEvent = eventDAO.getById(event.id);
        dbEvent.status = Event.OPENED;
        eventDAO.update(dbEvent);

        Auction dbAuction = auctionDAO.getById(auction.id);
        dbAuction.status = Auction.ACCEPTED;
        auctionDAO.update(dbAuction);

        // Create alt auction to simulate not accepted auction in the same event.
        User altUser = DBFeeder.createOtherDummyUser();
        Auction altAuction = DBFeeder.createDummyAuction(dbAuction.eventId, altUser.id);
        altAuction.status = Auction.PENDING;
        auctionDAO.update(altAuction);

        BodyWS requestBody = new BodyWS();
        requestBody.type = BidWS.TYPE_AUCTION_START;
        requestBody.nonce = "any";
        requestBody.json = new Gson().toJson(auction);
        bidWS.onMessage(mockSession, requestBody);

        BodyWS replyBody = mockSender.newObjLastReply;
        assertEquals(JsonCommon.error(BidWS.SOME_AUCTION_PENDING), replyBody.json);
        assertEquals(400, replyBody.status);
    }

    @Test
    public void auction_successfully_started() throws DAOException {
        User altUser = DBFeeder.createOtherDummyUser();

        Auction auction = successfulSubscription();

        Event dbEvent = eventDAO.getById(event.id);
        dbEvent.status = Event.OPENED;
        eventDAO.update(dbEvent);

        Auction dbAuction = auctionDAO.getById(auction.id);
        dbAuction.status = Auction.ACCEPTED;
        auctionDAO.update(dbAuction);

        BodyWS requestBody = new BodyWS();
        requestBody.type = BidWS.TYPE_AUCTION_START;
        requestBody.nonce = "any";
        requestBody.json = new Gson().toJson(auction);
        bidWS.onMessage(mockSession, requestBody);

        BodyWS replyBody = mockSender.newObjLastReply;
        Auction replyAuction = new Gson().fromJson(replyBody.json, Auction.class);
        assertEquals(dbAuction.id, replyAuction.id);
        assertNotNull(replyAuction.startTime);
        assertEquals(Auction.IN_PROGRESS, replyAuction.status);
        assertEquals(200, replyBody.status);

        Event newDbEvent = eventDAO.getById(dbEvent.id);
        assertEquals(Event.IN_PROGRESS, newDbEvent.status);
    }

    @Test
    public void auction_successfully_started_if_other_events_have_pending_auctions() throws DAOException {
        // Create alternative event & auction.
        Event dbEvent2 = DBFeeder.createOtherDummyEvent(user.id);
        dbEvent2.status = Event.OPENED;
        eventDAO.update(dbEvent2);

        Auction dbAuction2 = DBFeeder.createOtherDummyAuction(dbEvent2.id, user.id);
        dbAuction2.status = Auction.PENDING;
        auctionDAO.update(dbAuction2);

        // Execute auctions over "auction".
        Auction auction = successfulSubscription();

        Event dbEvent = eventDAO.getById(event.id);
        dbEvent.status = Event.OPENED;
        eventDAO.update(dbEvent);

        Auction dbAuction = auctionDAO.getById(auction.id);
        dbAuction.status = Auction.ACCEPTED;
        auctionDAO.update(dbAuction);

        BodyWS requestBody = new BodyWS();
        requestBody.type = BidWS.TYPE_AUCTION_START;
        requestBody.nonce = "any";
        requestBody.json = new Gson().toJson(auction);
        bidWS.onMessage(mockSession, requestBody);

        BodyWS replyBody = mockSender.newObjLastReply;
        Auction replyAuction = new Gson().fromJson(replyBody.json, Auction.class);
        assertEquals(dbAuction.id, replyAuction.id);
        assertNotNull(replyAuction.startTime);
        assertEquals(Auction.IN_PROGRESS, replyAuction.status);
        assertEquals(200, replyBody.status);

        Event newDbEvent = eventDAO.getById(dbEvent.id);
        assertEquals(Event.IN_PROGRESS, newDbEvent.status);
    }

    @Test
    public void auction_start_should_be_broadcasted_to_auction_subscribers() throws DAOException {
        User altUser = DBFeeder.createOtherDummyUser();

        Auction auction = successfulSubscription();

        Event dbEvent = eventDAO.getById(event.id);
        dbEvent.status = Event.OPENED;
        eventDAO.update(dbEvent);

        Auction dbAuction = auctionDAO.getById(auction.id);
        dbAuction.status = Auction.ACCEPTED;
        auctionDAO.update(dbAuction);

        BodyWS requestBody = new BodyWS();
        requestBody.type = BidWS.TYPE_AUCTION_START;
        requestBody.nonce = "any";
        requestBody.json = new Gson().toJson(auction);
        bidWS.onMessage(mockSession, requestBody);

        BodyWS replyBody = mockSender.newObjLastReply;
        assertEquals(200, replyBody.status);

        assertEquals(1, mockSender.sessionsLastListSend.size());
        assertEquals(mockSession, mockSender.sessionsLastListSend.get(0));
        BodyWS broadcastBody = mockSender.objLastListSend;
        Auction broadcastAuction = new Gson().fromJson(broadcastBody.json, Auction.class);
        assertEquals(BidWS.TYPE_AUCTION_STARTED, broadcastBody.type);
        assertEquals(broadcastAuction.id, dbAuction.id);
        assertEquals(broadcastAuction.status, Auction.IN_PROGRESS);
    }

    /**
     * TYPE_AUCTION_END
     */

    @Test
    public void cannot_close_if_unauthenticated() {
        Auction auction = successfulSubscription();

        // Deauthenticate user first.
        mockHttpSession.setUserId(-1);

        BodyWS requestBody = new BodyWS();
        requestBody.type = BidWS.TYPE_AUCTION_CLOSE;
        requestBody.nonce = "any";
        requestBody.json = new Gson().toJson(auction);
        bidWS.onMessage(mockSession, requestBody);

        BodyWS replyBody = mockSender.newObjLastReply;
        assertEquals(JsonCommon.unauthorized(), replyBody.json);
        assertEquals(400, replyBody.status);
    }

    @Test
    public void cannot_close_if_invalid_json() {
        Auction auction = successfulSubscription();

        BodyWS requestBody = new BodyWS();
        requestBody.type = BidWS.TYPE_AUCTION_CLOSE;
        requestBody.nonce = "any";
        requestBody.json = "{";
        bidWS.onMessage(mockSession, requestBody);

        BodyWS replyBody = mockSender.newObjLastReply;
        assertEquals(JsonCommon.invalidRequest(), replyBody.json);
        assertEquals(400, replyBody.status);
    }

    @Test
    public void cannot_close_if_not_subscribed_to_that_auction() {
        BodyWS requestBody = new BodyWS();
        requestBody.type = BidWS.TYPE_AUCTION_CLOSE;
        requestBody.nonce = "any";
        requestBody.json = new Gson().toJson(auction);
        bidWS.onMessage(mockSession, requestBody);

        BodyWS replyBody = mockSender.newObjLastReply;
        assertEquals(JsonCommon.error(BidWS.SUBSCRIPTION_ERROR), replyBody.json);
        assertEquals(400, replyBody.status);
    }

    @Test
    public void auction_not_in_progress_can_not_be_closed() throws DAOException {
        Auction auction = successfulSubscription();

        Auction dbAuction = auctionDAO.getById(auction.id);
        dbAuction.status = Auction.PENDING;
        auctionDAO.update(dbAuction);

        BodyWS requestBody = new BodyWS();
        requestBody.type = BidWS.TYPE_AUCTION_CLOSE;
        requestBody.nonce = "any";
        requestBody.json = new Gson().toJson(auction);
        bidWS.onMessage(mockSession, requestBody);

        BodyWS replyBody = mockSender.newObjLastReply;
        assertEquals(JsonCommon.error(BidWS.WRONG_AUCTION_STATUS), replyBody.json);
        assertEquals(400, replyBody.status);
    }

    @Test
    public void auction_cannot_be_closed_if_user_is_not_the_owner() throws DAOException {
        User altUser = DBFeeder.createOtherDummyUser();

        Auction auction = successfulSubscription();

        Event dbEvent = eventDAO.getById(event.id);
        dbEvent.ownerId = altUser.id;
        eventDAO.update(dbEvent);

        Auction dbAuction = auctionDAO.getById(auction.id);
        dbAuction.status = Auction.IN_PROGRESS;
        auctionDAO.update(dbAuction);

        BodyWS requestBody = new BodyWS();
        requestBody.type = BidWS.TYPE_AUCTION_CLOSE;
        requestBody.nonce = "any";
        requestBody.json = new Gson().toJson(auction);
        bidWS.onMessage(mockSession, requestBody);

        BodyWS replyBody = mockSender.newObjLastReply;
        assertEquals(JsonCommon.unauthorized(), replyBody.json);
        assertEquals(400, replyBody.status);
    }

    @Test
    public void auction_closes_with_no_bids() throws DAOException {
        User altUser = DBFeeder.createOtherDummyUser();

        Auction auction = successfulSubscription();

        User dbUser = userDAO.getById(user.id);
        dbUser.credit = 1000;
        userDAO.update(dbUser);

        Auction dbAuction = auctionDAO.getById(auction.id);
        dbAuction.status = Auction.IN_PROGRESS;
        auctionDAO.update(dbAuction);

        Event dbEvent = eventDAO.getById(dbAuction.eventId);
        dbEvent.status = Event.IN_PROGRESS;
        dbEvent.ownerId = user.id;
        eventDAO.update(dbEvent);

        BodyWS requestBody = new BodyWS();
        requestBody.type = BidWS.TYPE_AUCTION_CLOSE;
        requestBody.nonce = "any";
        requestBody.json = new Gson().toJson(auction);
        bidWS.onMessage(mockSession, requestBody);

        BodyWS replyBody = mockSender.newObjLastReply;
        Auction replyAuction = new Gson().fromJson(replyBody.json, Auction.class);
        assertEquals(auction.id, replyAuction.id);
        assertEquals(Auction.FINISHED, replyAuction.status);
        assertNotNull(replyAuction.endingTime);
        assertEquals(200, replyBody.status);

        // No more auctions set as pending -> event has to be set as finished.
        Event newDbEvent = eventDAO.getById(dbEvent.id);
        assertEquals(Event.FINISHED, newDbEvent.status);

        // User credit has to remain the same.
        User newDbUser = userDAO.getById(user.id);
        assertEquals(dbUser.credit, newDbUser.credit, 0);
    }

    @Test
    public void english_auction_closes_with_bids() throws DAOException {
        // Bid twice (1. dbUser, 2. altUser)
        Auction auction = successfulSubscription();
        Auction dbAuction = auctionDAO.getById(auction.id);
        dbAuction.ownerId = user.id;
        dbAuction.status = Auction.IN_PROGRESS;
        auctionDAO.update(dbAuction);

        Event dbEvent = eventDAO.getById(dbAuction.eventId);
        dbEvent.ownerId = user.id;
        dbEvent.status = Event.IN_PROGRESS;
        eventDAO.update(dbEvent);

        Good good = DBFeeder.createDummyGood(auction.id);

        User dbUser = userDAO.getById(user.id);
        dbUser.credit = 2000;
        userDAO.update(dbUser);

        User altUser = DBFeeder.createOtherDummyUser();
        altUser.credit = 2000;
        userDAO.update(altUser);

        // User bids @ 1000.
        Bid attemptBid1 = DummyGenerator.getDummyBid();
        attemptBid1.auctionId = auction.id;
        attemptBid1.goodId = good.id;
        attemptBid1.amount = 1000;
        Bid[] attemptBids1 = new Bid[] {attemptBid1};
        BodyWS requestBody = new BodyWS();
        requestBody.type = BidWS.TYPE_AUCTION_BID;
        requestBody.nonce = "first-bid";
        requestBody.json = new Gson().toJson(attemptBids1);
        bidWS.onMessage(mockSession, requestBody);

        BodyWS replyBody1 = mockSender.newObjLastReply;
        Bid dbBid1 = new Gson().fromJson(replyBody1.json, Bid.class);
        assertEquals(user.id, dbBid1.ownerId);
        assertEquals(1000, dbBid1.amount, 0);
        assertEquals(200, replyBody1.status);

        // Alt user authenticates.
        mockHttpSession.setUserId(altUser.id);

        // Alt user bids @ 2000.
        Bid attemptBid2 = DummyGenerator.getDummyBid();
        attemptBid2.auctionId = auction.id;
        attemptBid2.goodId = good.id;
        attemptBid2.amount = 2000;
        Bid[] attemptBids2 = new Bid[] {attemptBid2};
        BodyWS requestBody2 = new BodyWS();
        requestBody2.type = BidWS.TYPE_AUCTION_BID;
        requestBody2.nonce = "second-bid";
        requestBody2.json = new Gson().toJson(attemptBids2);
        bidWS.onMessage(mockSession, requestBody2);

        BodyWS replyBody2 = mockSender.newObjLastReply;
        Bid dbBid2 = new Gson().fromJson(replyBody2.json, Bid.class);
        assertEquals(altUser.id, dbBid2.ownerId);
        assertEquals(2000, dbBid2.amount, 0);
        assertEquals(200, replyBody2.status);

        // User authenticates back.
        mockHttpSession.setUserId(dbUser.id);

        // Close auction (event owner: user).
        BodyWS requestCloseBody = new BodyWS();
        requestCloseBody.type = BidWS.TYPE_AUCTION_CLOSE;
        requestCloseBody.nonce = "any";
        requestCloseBody.json = new Gson().toJson(auction);
        bidWS.onMessage(mockSession, requestCloseBody);

        BodyWS replyBody = mockSender.newObjLastReply;
        Auction replyAuction = new Gson().fromJson(replyBody.json, Auction.class);
        assertEquals(auction.id, replyAuction.id);
        assertEquals(Auction.FINISHED, replyAuction.status);
        assertNotNull(replyAuction.endingTime);
        assertEquals(200, replyBody.status);

        // English auction specifics.
        User newDbUser = userDAO.getById(user.id);
        User newAltUser = userDAO.getById(altUser.id);
        assertEquals(altUser.id, replyAuction.winnerId);
        assertEquals(2000, altUser.credit, 0);
        assertEquals(0, newAltUser.credit, 0);
        assertEquals(2000, dbUser.credit, 0);
        assertEquals(2000 + 2000 - 40, newDbUser.credit, 0); // Item was from user. 2000 initial + (2000 max bid - (2% + 1% fees)). <-- Since he was the event owner he gets a 1% fees back.

    }

    @Test
    public void english_auction_4_man_game() throws DAOException {
        User userEventOwner = DBFeeder.createThirdDummyUser();
        User userAuctionOwner = DBFeeder.createFourthDummyUser();

        Auction dbAuction = auctionDAO.getById(auction.id);
        dbAuction.ownerId = userAuctionOwner.id;
        dbAuction.status = Auction.IN_PROGRESS;
        auctionDAO.update(dbAuction);

        Event dbEvent = eventDAO.getById(dbAuction.eventId);
        dbEvent.ownerId = userEventOwner.id;
        dbEvent.status = Event.IN_PROGRESS;
        eventDAO.update(dbEvent);

        Good good = DBFeeder.createDummyGood(auction.id);

        // Bid twice (1. dbUser, 2. altUser)
        Auction auction = successfulSubscription();

        User dbUser = userDAO.getById(user.id);
        dbUser.credit = 2000;
        userDAO.update(dbUser);

        User altUser = DBFeeder.createOtherDummyUser();
        altUser.credit = 2000;
        userDAO.update(altUser);

        // User bids @ 1000.
        Bid attemptBid1 = DummyGenerator.getDummyBid();
        attemptBid1.auctionId = auction.id;
        attemptBid1.goodId = good.id;
        attemptBid1.amount = 1000;
        Bid[] attemptBids1 = new Bid[] {attemptBid1};
        BodyWS requestBody = new BodyWS();
        requestBody.type = BidWS.TYPE_AUCTION_BID;
        requestBody.nonce = "first-bid";
        requestBody.json = new Gson().toJson(attemptBids1);
        bidWS.onMessage(mockSession, requestBody);

        BodyWS replyBody1 = mockSender.newObjLastReply;
        Bid dbBid1 = new Gson().fromJson(replyBody1.json, Bid.class);
        assertEquals(user.id, dbBid1.ownerId);
        assertEquals(1000, dbBid1.amount, 0);
        assertEquals(200, replyBody1.status);

        // Alt user authenticates.
        mockHttpSession.setUserId(altUser.id);

        // Alt user bids @ 2000.
        Bid attemptBid2 = DummyGenerator.getDummyBid();
        attemptBid2.auctionId = auction.id;
        attemptBid2.goodId = good.id;
        attemptBid2.amount = 2000;
        Bid[] attemptBids2 = new Bid[] {attemptBid2};
        BodyWS requestBody2 = new BodyWS();
        requestBody2.type = BidWS.TYPE_AUCTION_BID;
        requestBody2.nonce = "second-bid";
        requestBody2.json = new Gson().toJson(attemptBids2);
        bidWS.onMessage(mockSession, requestBody2);

        BodyWS replyBody2 = mockSender.newObjLastReply;
        Bid dbBid2 = new Gson().fromJson(replyBody2.json, Bid.class);
        assertEquals(altUser.id, dbBid2.ownerId);
        assertEquals(2000, dbBid2.amount, 0);
        assertEquals(200, replyBody2.status);

        // User authenticates back.
        mockHttpSession.setUserId(userEventOwner.id);

        // Close auction (event owner: user).
        BodyWS requestCloseBody = new BodyWS();
        requestCloseBody.type = BidWS.TYPE_AUCTION_CLOSE;
        requestCloseBody.nonce = "any";
        requestCloseBody.json = new Gson().toJson(auction);
        bidWS.onMessage(mockSession, requestCloseBody);

        BodyWS replyBody = mockSender.newObjLastReply;
        Auction replyAuction = new Gson().fromJson(replyBody.json, Auction.class);
        assertEquals(auction.id, replyAuction.id);
        assertEquals(Auction.FINISHED, replyAuction.status);
        assertNotNull(replyAuction.endingTime);
        assertEquals(200, replyBody.status);

        // Just making sure the game settings are correct.
        assertEquals(userEventOwner.id, dbEvent.ownerId);
        assertEquals(userAuctionOwner.id, dbAuction.ownerId);
        assertNotEquals(userEventOwner.id, dbUser.id);
        assertNotEquals(userAuctionOwner.id, dbUser.id);
        assertNotEquals(altUser.id, dbUser.id);

        // English auction specifics.
        User newUserEventOwner = userDAO.getById(userEventOwner.id);
        User newUserAuctionOwner = userDAO.getById(userAuctionOwner.id);
        User newDbUser = userDAO.getById(dbUser.id);
        User newAltUser = userDAO.getById(altUser.id);
        assertEquals(altUser.id, replyAuction.winnerId);
        assertEquals(2000, altUser.credit, 0);
        assertEquals(0, newAltUser.credit, 0);
        assertEquals(2000, dbUser.credit, 0);
        assertEquals(2000, newDbUser.credit, 0);
        assertEquals(0, userEventOwner.credit, 0);
        assertEquals(20, newUserEventOwner.credit, 0);
        assertEquals(0, userAuctionOwner.credit, 0);
        assertEquals(2000 - 40 - 20, newUserAuctionOwner.credit, 0); // Item was from user. 2000 initial + (2000 max bid - (2% + 1% fees)).
    }

    @Test
    public void auction_closes_with_another_auction_finished() throws DAOException {
        User altUser = DBFeeder.createOtherDummyUser();

        Auction auction = successfulSubscription();

        Auction dbAuction = auctionDAO.getById(auction.id);
        dbAuction.status = Auction.IN_PROGRESS;
        auctionDAO.update(dbAuction);

        Event dbEvent = eventDAO.getById(dbAuction.eventId);
        dbEvent.status = Event.IN_PROGRESS;
        eventDAO.update(dbEvent);

        // Create alt auction and assign it to the same event. We won't be using it but it'll be there.
        Auction altAuction = DBFeeder.createOtherDummyAuction(dbEvent.id, dbAuction.ownerId);
        altAuction.status = Auction.FINISHED;
        auctionDAO.update(altAuction);

        BodyWS requestBody = new BodyWS();
        requestBody.type = BidWS.TYPE_AUCTION_CLOSE;
        requestBody.nonce = "any";
        requestBody.json = new Gson().toJson(auction);
        bidWS.onMessage(mockSession, requestBody);

        BodyWS replyBody = mockSender.newObjLastReply;
        Auction replyAuction = new Gson().fromJson(replyBody.json, Auction.class);
        assertEquals(auction.id, replyAuction.id);
        assertEquals(Auction.FINISHED, replyAuction.status);
        assertNotNull(replyAuction.endingTime);
        assertEquals(200, replyBody.status);

        // No more auctions set as pending -> event has to be set as finished.
        Event newDbEvent = eventDAO.getById(dbEvent.id);
        assertEquals(Event.FINISHED, newDbEvent.status);
    }

    @Test
    public void auction_closes_but_there_is_another_auction_left() throws DAOException {
        User altUser = DBFeeder.createOtherDummyUser();

        Auction auction = successfulSubscription();

        Auction dbAuction = auctionDAO.getById(auction.id);
        dbAuction.status = Auction.IN_PROGRESS;
        auctionDAO.update(dbAuction);

        Event dbEvent = eventDAO.getById(dbAuction.eventId);
        dbEvent.status = Event.IN_PROGRESS;
        eventDAO.update(dbEvent);

        // Create alt auction and assign it to the same event. We won't be using it but it'll be there.
        Auction altAuction = DBFeeder.createOtherDummyAuction(dbEvent.id, dbAuction.ownerId);

        BodyWS requestBody = new BodyWS();
        requestBody.type = BidWS.TYPE_AUCTION_CLOSE;
        requestBody.nonce = "any";
        requestBody.json = new Gson().toJson(auction);
        bidWS.onMessage(mockSession, requestBody);

        BodyWS replyBody = mockSender.newObjLastReply;
        Auction replyAuction = new Gson().fromJson(replyBody.json, Auction.class);
        assertEquals(auction.id, replyAuction.id);
        assertEquals(Auction.FINISHED, replyAuction.status);
        assertNotNull(replyAuction.endingTime);
        assertEquals(200, replyBody.status);

        // More auctions -> event shouldn't be set as finished.
        Event newDbEvent = eventDAO.getById(dbEvent.id);
        assertEquals(Event.IN_PROGRESS, newDbEvent.status);
    }

    @Test
    public void auction_closes_is_broadcasted() throws DAOException {
        User altUser = DBFeeder.createOtherDummyUser();

        Auction auction = successfulSubscription();

        Auction dbAuction = auctionDAO.getById(auction.id);
        dbAuction.status = Auction.IN_PROGRESS;
        auctionDAO.update(dbAuction);

        Event dbEvent = eventDAO.getById(dbAuction.eventId);
        dbEvent.status = Event.IN_PROGRESS;
        eventDAO.update(dbEvent);

        // Create alt auction and assign it to the same event. We won't be using it but it'll be there.
        Auction altAuction = DummyGenerator.getOtherDummyAuction();
        altAuction.eventId = dbEvent.id;
        altAuction.status = Auction.ACCEPTED;
        auctionDAO.update(altAuction);

        BodyWS requestBody = new BodyWS();
        requestBody.type = BidWS.TYPE_AUCTION_CLOSE;
        requestBody.nonce = "any";
        requestBody.json = new Gson().toJson(auction);
        bidWS.onMessage(mockSession, requestBody);

        BodyWS replyBody = mockSender.newObjLastReply;
        assertEquals(200, replyBody.status);

        // Should be broadcasted
        assertEquals(1, mockSender.sessionsLastListSend.size());
        assertEquals(mockSession, mockSender.sessionsLastListSend.get(0));
        BodyWS broadcastBody = mockSender.objLastListSend;
        Auction broadcastAuction = new Gson().fromJson(broadcastBody.json, Auction.class);
        assertEquals(BidWS.TYPE_AUCTION_CLOSED, broadcastBody.type);
        assertEquals(broadcastAuction.id, dbAuction.id);
        assertEquals(broadcastAuction.status, Auction.FINISHED);
    }

    @Test
    public void combinatorial_auction_closes_with_no_bids() throws DAOException {
        Auction auction = successfulSubscription();

        User dbUser = userDAO.getById(user.id);
        dbUser.credit = 1000;
        userDAO.update(dbUser);

        Auction dbAuction = auctionDAO.getById(auction.id);
        dbAuction.status = Auction.IN_PROGRESS;
        auctionDAO.update(dbAuction);

        Event dbEvent = eventDAO.getById(dbAuction.eventId);
        dbEvent.status = Event.IN_PROGRESS;
        dbEvent.ownerId = user.id;
        dbEvent.auctionType = Event.COMBINATORIAL;
        eventDAO.update(dbEvent);

        BodyWS requestBody = new BodyWS();
        requestBody.type = BidWS.TYPE_AUCTION_CLOSE;
        requestBody.nonce = "any";
        requestBody.json = new Gson().toJson(auction);
        bidWS.onMessage(mockSession, requestBody);

        BodyWS replyBody = mockSender.newObjLastReply;
        Auction replyAuction = new Gson().fromJson(replyBody.json, Auction.class);
        assertEquals(auction.id, replyAuction.id);
        assertEquals(Auction.FINISHED, replyAuction.status);
        assertEquals(0, replyAuction.winnerId);
        assertNull(replyAuction.combinatorialWinners);
        assertNotNull(replyAuction.endingTime);
        assertEquals(200, replyBody.status);

        // No more auctions set as pending -> event has to be set as finished.
        Event newDbEvent = eventDAO.getById(dbEvent.id);
        assertEquals(Event.FINISHED, newDbEvent.status);

        // User credit has to remain the same.
        User newDbUser = userDAO.getById(user.id);
        assertEquals(dbUser.credit, newDbUser.credit, 0);
    }

    @Test
    public void combinatorial_auction_closes_with_bids() throws DAOException {
        // altUser bids 10 for good1
        // dbUser bids 100 for good1, good2
        Auction auction = successfulSubscription();
        Auction dbAuction = auctionDAO.getById(auction.id);
        dbAuction.ownerId = user.id;
        dbAuction.status = Auction.IN_PROGRESS;
        auctionDAO.update(dbAuction);

        Event dbEvent = eventDAO.getById(dbAuction.eventId);
        dbEvent.ownerId = user.id;
        dbEvent.status = Event.IN_PROGRESS;
        dbEvent.auctionType = Event.COMBINATORIAL;
        eventDAO.update(dbEvent);

        Good good1 = DBFeeder.createDummyGood(auction.id);
        Good good2 = DBFeeder.createOtherDummyGood(auction.id);

        User dbUser = userDAO.getById(user.id);
        dbUser.credit = 2000;
        userDAO.update(dbUser);

        User altUser = DBFeeder.createOtherDummyUser();
        altUser.credit = 2000;
        userDAO.update(altUser);

        // dbUser bids @ 100
        Bid attemptBid1 = DummyGenerator.getDummyBid();
        attemptBid1.auctionId = auction.id;
        attemptBid1.goodId = good1.id;
        attemptBid1.amount = 10;
        Bid[] dbUserBids = new Bid[] {attemptBid1};
        BodyWS requestBody = new BodyWS();
        requestBody.type = BidWS.TYPE_AUCTION_BID;
        requestBody.nonce = "first-bid";
        requestBody.json = new Gson().toJson(dbUserBids);
        bidWS.onMessage(mockSession, requestBody);

        BodyWS replyBody1 = mockSender.newObjLastReply;
        Bid dbBid1 = new Gson().fromJson(replyBody1.json, Bid.class);
        assertEquals(200, replyBody1.status);
        assertEquals(user.id, dbBid1.ownerId);
        assertEquals(0, dbBid1.amount, 0);

        // Alt user authenticates.
        mockHttpSession.setUserId(altUser.id);

        Bid attemptBid2 = DummyGenerator.getDummyBid();
        attemptBid2.auctionId = auction.id;
        attemptBid2.goodId = good1.id;
        attemptBid2.amount = 100;
        Bid attemptBid3 = DummyGenerator.getDummyBid();
        attemptBid3.auctionId = auction.id;
        attemptBid3.goodId = good2.id;
        attemptBid3.amount = 100;
        Bid[] altUserBids = new Bid[] {attemptBid3, attemptBid2};
        BodyWS requestBody2 = new BodyWS();
        requestBody2.type = BidWS.TYPE_AUCTION_BID;
        requestBody2.nonce = "second-bid";
        requestBody2.json = new Gson().toJson(altUserBids);
        bidWS.onMessage(mockSession, requestBody2);

        BodyWS replyBody2 = mockSender.newObjLastReply;
        Bid dbBid2 = new Gson().fromJson(replyBody2.json, Bid.class);
        assertEquals(altUser.id, dbBid2.ownerId);
        assertEquals(0, dbBid2.amount, 0);
        assertEquals(200, replyBody2.status);

        // User authenticates back.
        mockHttpSession.setUserId(dbUser.id);

        // Close auction (event owner: user).
        BodyWS requestCloseBody = new BodyWS();
        requestCloseBody.type = BidWS.TYPE_AUCTION_CLOSE;
        requestCloseBody.nonce = "any";
        requestCloseBody.json = new Gson().toJson(auction);
        bidWS.onMessage(mockSession, requestCloseBody);

        BodyWS replyBody = mockSender.newObjLastReply;
        Auction replyAuction = new Gson().fromJson(replyBody.json, Auction.class);
        assertEquals(auction.id, replyAuction.id);
        assertEquals(Auction.FINISHED, replyAuction.status);
        assertNotNull(replyAuction.endingTime);
        assertEquals(200, replyBody.status);

        // Combinatorial auction specifics.
        User newDbUser = userDAO.getById(user.id);
        User newAltUser = userDAO.getById(altUser.id);
        assertEquals(String.valueOf(altUser.id), replyAuction.combinatorialWinners);
        assertEquals(1900, newAltUser.credit, 0.00001);
        // Item was from user. 2000 initial + (100 winner bid - (3% + 1% fees)). <-- Since he was the event owner he gets a 1% fees back.
        assertEquals(2000 + 100 * ProjectVariables.EVENT_OWNER_FEE, newDbUser.credit, 0.000001);
    }

    private Auction successfulSubscription() {
        Auction requestAuction = new Auction();
        requestAuction.id = auction.id;

        return successfulSubscription(requestAuction);
    }

    private Auction successfulSubscription(Auction auction) {
        BodyWS requestBody = new BodyWS();
        requestBody.type = BidWS.TYPE_AUCTION_SUBSCRIBE;
        requestBody.nonce = "successful-subscription-nonce";
        requestBody.status = 200;
        requestBody.json = new Gson().toJson(auction);
        bidWS.onMessage(mockSession, requestBody);

        return auction;
    }
}