package main.java.socket;

import com.google.gson.Gson;
import main.java.dao.DAOException;
import main.java.dao.sql.AbstractDBTest;
import main.java.mocks.MockHttpSession;
import main.java.mocks.MockSession;
import main.java.mocks.MockWSSender;
import main.java.models.Auction;
import main.java.models.Bid;
import main.java.models.Event;
import main.java.models.User;
import main.java.models.meta.BodyWS;
import main.java.models.meta.Error;
import main.java.models.meta.Msg;
import main.java.utils.DBFeeder;
import main.java.utils.DummyGenerator;
import main.java.utils.JsonCommon;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class BidWSTest extends AbstractDBTest {

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
        assertEquals(JsonCommon.OK, new Gson().fromJson(replyBody.json, Msg.class).msg);
    }

    /* AuctionBid */

    @Test
    public void test_not_authenticate_user_can_not_create_bid() {
        Auction auction = succesfulSubscription();

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

//    @Test
//    public void test_bid_with_invalid_amount_can_not_be_created() throws Exception {
//        Bid dummyBid = DummyGenerator.getDummyBid();
//        dummyBid.amount = -1.0;
//        common_bid_error_test(dummyBid, BidNew.INVALID_AMOUNT_ERROR);
//    }
//
//    @Test
//    public void test_bid_without_auction_can_not_be_created() throws Exception {
//        Bid dummyBid = DummyGenerator.getDummyBid();
//        common_bid_error_test(dummyBid, BidNew.AUCTION_ID_ERROR);
//    }
//
//
//    @Test
//    public void should_create_bid() throws Exception {
//        Auction dummyAuction = DBFeeder.createDummyAuction();
//
//        Bid attemptBid = DummyGenerator.getDummyBid();
//        attemptBid.ownerId = dummyAuction.ownerId;
//        attemptBid.auctionId = dummyAuction.id;
//        String attemptBidJson = new Gson().toJson(attemptBid);
//
//        HttpServletStubber stubber = new HttpServletStubber();
//        stubber.authenticate(attemptBid.ownerId);
//        stubber.body(attemptBidJson).listen();
//        new BidNew().doPost(stubber.servletRequest, stubber.servletResponse);
//
//        Bid outputBid = new Gson().fromJson(stubber.gathered(), Bid.class);
//
//        assertEquals(attemptBid.amount, outputBid.amount, 0.0000001);
//        assertEquals(attemptBid.ownerId, outputBid.ownerId);
//        assertEquals(attemptBid.auctionId, outputBid.auctionId);
//        assertNotNull(outputBid.createdAt);
//    }
//
//    private void common_bid_error_test(Bid attemptBid, String errorMsg) throws DAOException, IOException, ServletException {
//        User dummyUser = DBFeeder.createDummyUser();
//
//        attemptBid.ownerId = dummyUser.id;
//        String attemptBidJson = new Gson().toJson(attemptBid);
//
//        HttpServletStubber stubber = new HttpServletStubber();
//        stubber.authenticate(dummyUser.id);
//        stubber.body(attemptBidJson).listen();
//        new BidNew().doPost(stubber.servletRequest, stubber.servletResponse);
//
//        Error error = new Gson().fromJson(stubber.gathered(), Error.class);
//        assertEquals(errorMsg, error.error);
//    }

    private Auction succesfulSubscription() {
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