package main.java.servlets.bid;

import com.google.gson.Gson;
import main.java.dao.DAOException;
import main.java.dao.sql.AbstractDBTest;
import main.java.mocks.HttpServletStubber;
import main.java.models.Auction;
import main.java.models.Bid;
import main.java.models.User;
import main.java.models.meta.Error;
import main.java.utils.DBFeeder;
import main.java.utils.DummyGenerator;
import main.java.utils.ImpreciseDate;
import org.junit.Test;

import javax.servlet.ServletException;
import java.io.IOException;

import static org.junit.Assert.*;

public class BidNewTest extends AbstractDBTest {

    @Test
    public void test_not_authenticate_user_can_not_create_bid() throws Exception {
        HttpServletStubber stubber = new HttpServletStubber();

        Bid attemptBid = DummyGenerator.getDummyBid();
        String attemptBidString = new Gson().toJson(attemptBid);
        stubber.body(attemptBidString).listen();
        new BidNew().doPost(stubber.servletRequest, stubber.servletResponse);

        Error error = new Gson().fromJson(stubber.gathered(), Error.class);
        assertEquals("Unauthorized.", error.error);
    }

    @Test
    public void test_bid_with_invalid_amount_can_not_be_created() throws Exception {
        Bid dummyBid = DummyGenerator.getDummyBid();
        dummyBid.amount = -1.0;
        common_bid_error_test(dummyBid, BidNew.INVALID_AMOUNT_ERROR);
    }

    @Test
    public void test_bid_without_auction_can_not_be_created() throws Exception {
        Bid dummyBid = DummyGenerator.getDummyBid();
        common_bid_error_test(dummyBid, BidNew.AUCTION_ID_ERROR);
    }


    @Test
    public void should_create_bid() throws Exception {
        Auction dummyAuction = DBFeeder.createDummyAuction();

        Bid attemptBid = DummyGenerator.getDummyBid();
        attemptBid.ownerId = dummyAuction.ownerId;
        attemptBid.auctionId = dummyAuction.id;
        String attemptBidJson = new Gson().toJson(attemptBid);

        HttpServletStubber stubber = new HttpServletStubber();
        stubber.authenticate(attemptBid.ownerId);
        stubber.body(attemptBidJson).listen();
        new BidNew().doPost(stubber.servletRequest, stubber.servletResponse);

        Bid outputBid = new Gson().fromJson(stubber.gathered(), Bid.class);

        assertEquals(attemptBid.amount, outputBid.amount, 0.0000001);
        assertEquals(attemptBid.ownerId, outputBid.ownerId);
        assertEquals(attemptBid.auctionId, outputBid.auctionId);
        assertNotNull(outputBid.createdAt);
    }

    private void common_bid_error_test(Bid attemptBid, String errorMsg) throws DAOException, IOException, ServletException {
        User dummyUser = DBFeeder.createDummyUser();

        attemptBid.ownerId = dummyUser.id;
        String attemptBidJson = new Gson().toJson(attemptBid);

        HttpServletStubber stubber = new HttpServletStubber();
        stubber.authenticate(dummyUser.id);
        stubber.body(attemptBidJson).listen();
        new BidNew().doPost(stubber.servletRequest, stubber.servletResponse);

        Error error = new Gson().fromJson(stubber.gathered(), Error.class);
        assertEquals(errorMsg, error.error);
    }
}