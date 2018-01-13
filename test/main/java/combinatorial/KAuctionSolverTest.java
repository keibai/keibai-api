package main.java.combinatorial;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class KAuctionSolverTest {
    @Test
    public void solve() throws Exception {
        // Simple example where we know that the winner is the bidder in position 1

        KBid[] bids = {new KBid(1, 15, new int[]{0, 3}),
                new KBid(2, 20, new int[]{1, 2, 3}),
                new KBid(3, 10, new int[]{0, 2})};

        KAuctionSolver kAuctionSolver = new KAuctionSolver(bids);

        List<KBid> winners = kAuctionSolver.solve();

        assertEquals(1, winners.size());
        assertEquals(new KBid(2, 20, new int[]{1, 2, 3}), winners.get(0));
    }

    @Test
    public void solve_with_other_items() {
        // Same as solve() test, but goods are mapped differently
        // 0 -> 1
        // 1 -> 2
        // 2 -> 4
        // 3 -> 3
        KBid[] bids = {new KBid(1, 15, new int[]{1, 3}),
                new KBid(2, 20, new int[]{2, 4, 3}),
                new KBid(3, 10, new int[]{1, 4})};

        KAuctionSolver kAuctionSolver = new KAuctionSolver(bids);

        List<KBid> winners = kAuctionSolver.solve();

        assertEquals(1, winners.size());
        assertEquals(new KBid(2, 20, new int[]{2, 4, 3}), winners.get(0));
    }
}