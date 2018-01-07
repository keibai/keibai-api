package main.java.models;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class BidTest {

    @Test
    public void test_maximum_bid_is_retrieved_correctly() throws Exception {
        Bid maxBid = new Bid();
        maxBid.amount = 2000;
        List<Bid> bids = new ArrayList<Bid>() {{
            add(new Bid() {{ amount = 100; }});
            add(maxBid);
            add(new Bid() {{ amount = 300; }});
        }};
        Bid outputMax = Collections.max(bids);
        assertEquals(maxBid, outputMax);
    }
}