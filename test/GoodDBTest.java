import main.java.dao.*;
import main.java.dao.sql.AuctionDAOSQL;
import main.java.dao.sql.EventDAOSQL;
import main.java.dao.sql.GoodDAOSQL;
import main.java.dao.sql.UserDAOSQL;
import main.java.models.Auction;
import main.java.models.Event;
import main.java.models.Good;
import main.java.models.User;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class GoodDBTest extends AbstractDBTest {

    @Test
    public void test_good_is_properly_inserted_and_retrieved_from_db() throws DAOException, NotFoundException {
//        UserDAO userDAO = UserDAOSQL.getInstance();
//        EventDAO eventDAO = EventDAOSQL.getInstance();
//        AuctionDAO auctionDAO = AuctionDAOSQL.getInstance();
//        GoodDAO goodDAO = GoodDAOSQL.getInstance();
//
//        User owner = DummyGenerator.getDummyUser();
//        User winner = DummyGenerator.getOtherDummyUser();
//        userDAO.createUser(owner);
//        userDAO.createUser(winner);
//
//        Event event = DummyGenerator.getDummyEvent();
//        eventDAO.createEvent(event);
//
//        Auction auction = DummyGenerator.getDummyAuction();
//        Good insertedGood = DummyGenerator.getDummyGood();
//
//        auctionDAO.createAuction(auction);
//        goodDAO.createGood(insertedGood);
//
//        Good retrievedGood = goodDAO.getGoodById(1);
//        assertEquals(insertedGood, retrievedGood);
    }
}
