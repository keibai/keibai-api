package main.java.combinatorial;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;

import java.util.ArrayList;
import java.util.List;

public class KAuctionSolver {

    private Model model;
    private BoolVar[] selectedBids;
    private IntVar[] selectedBidsWeights;
    private IntVar objective;
    private KBid[] bids;

    public KAuctionSolver(int nGoods, KBid[] bids) {
        this.model = new Model();
        this.bids = bids;
        this.createModel(nGoods);
    }

    public List<KBid> solve() {
        Solver solver = model.getSolver();
        Solution solution = solver.findOptimalSolution(objective, Model.MAXIMIZE);

        List<KBid> winnerBids = new ArrayList<>();
        for (int i = 0; i < selectedBids.length; i++) {
            if (solution.getIntVal(selectedBids[i]) == 1) {
                // Selected bid
                winnerBids.add(bids[i]);
            }
        }
        return winnerBids;
    }

    private void createModel(int nGoods) {
        selectedBids = new BoolVar[bids.length];
        selectedBidsWeights = new IntVar[bids.length];

        // Variable initialization
        int totalWeight = 0;
        for (int i = 0; i < bids.length; i++) {
            BoolVar selectedBid = model.boolVar("bid" + i);
            IntVar selectedBidWeight = model.intScaleView(selectedBid, bids[i].value);
            totalWeight += bids[i].value;
            selectedBids[i] = selectedBid;
            selectedBidsWeights[i] = selectedBidWeight;
        }
        objective = model.intVar("objective", 0, totalWeight);

        // Objective function
        model.sum(selectedBidsWeights, "=", objective).post();

        // Subject to
        List<List<Integer>> conflicts = KBid.getBidConflicts(nGoods, bids);
        for (List<Integer> conflictingBids: conflicts) {
            IntVar[] c = new IntVar[conflictingBids.size()];
            for (int i = 0; i < conflictingBids.size(); i++) {
                c[i] = selectedBids[conflictingBids.get(i)];
            }
            model.sum(c, "<=", 1).post();
        }
    }

}
