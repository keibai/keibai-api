package main.java.servlets.good;

import com.google.gson.Gson;
import main.java.dao.GoodDAO;
import main.java.dao.sql.AbstractDBTest;
import main.java.dao.sql.GoodDAOSQL;
import main.java.dao.sql.GoodDBTest;
import main.java.gson.BetterGson;
import main.java.mocks.HttpServletStubber;
import main.java.models.Auction;
import main.java.models.Good;
import main.java.models.meta.Error;
import main.java.utils.DBFeeder;
import main.java.utils.DummyGenerator;
import org.junit.Test;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.*;

public class GoodListByAuctionIdTest extends AbstractDBTest {

    @Test
    public void test_when_auction_id_parameter_not_set() throws Exception {
        HttpServletStubber stubber = new HttpServletStubber();
        stubber.listen();
        new GoodListByAuctionId().doGet(stubber.servletRequest, stubber.servletResponse);
        Error error = new BetterGson().newInstance().fromJson(stubber.gathered(), Error.class);

        assertEquals(GoodListByAuctionId.ID_NONE, error.error);
    }

    @Test
    public void test_when_auction_id_parameter_is_empty() throws Exception {
        HttpServletStubber stubber = new HttpServletStubber();
        stubber.parameter("auctionid", "").listen();
        new GoodListByAuctionId().doGet(stubber.servletRequest, stubber.servletResponse);
        Error error = new BetterGson().newInstance().fromJson(stubber.gathered(), Error.class);

        assertEquals(GoodListByAuctionId.ID_INVALID, error.error);
    }

    @Test
    public void test_when_auction_does_not_exist() throws Exception {
        HttpServletStubber stubber = new HttpServletStubber();
        stubber.parameter("auctionid", "1").listen();
        new GoodListByAuctionId().doGet(stubber.servletRequest, stubber.servletResponse);
        Error error = new BetterGson().newInstance().fromJson(stubber.gathered(), Error.class);

        assertEquals(GoodListByAuctionId.AUCTION_NOT_EXIST, error.error);
    }

    @Test
    public void test_get_good_list_returns_list_of_goods() throws Exception {
        GoodDAO goodDAO = GoodDAOSQL.getInstance();
        Auction dummyAuction = DBFeeder.createDummyAuction();

        Good dummyGood1 = DummyGenerator.getDummyGood();
        dummyGood1.auctionId = dummyAuction.id;
        Good insertedGood1 = goodDAO.create(dummyGood1);

        Good dummyGood2 = DummyGenerator.getOtherDummyGood();
        dummyGood2.auctionId = dummyAuction.id;
        Good insertedGood2 = goodDAO.create(dummyGood2);

        List<Good> expectedGoodList = new LinkedList<Good>() {{
            add(insertedGood1);
            add(insertedGood2);
        }};

        HttpServletStubber stubber = new HttpServletStubber();
        stubber.parameter("auctionid", String.valueOf(dummyAuction.id)).listen();
        new GoodListByAuctionId().doGet(stubber.servletRequest, stubber.servletResponse);
        Good[] modelList = new BetterGson().newInstance().fromJson(stubber.gathered(), Good[].class);

        GoodDBTest.assertGoodListEquals(expectedGoodList, Arrays.asList(modelList));

    }
}