package main.java.combinatorial;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class KBid {

    public int id;
    public int value;
    public int[] goodsIds;

    public KBid(int id, int value, int[] goodIds) {
        this.id = id;
        this.value = value;
        this.goodsIds = goodIds;
    }

    public static List<List<Integer>> getBidConflicts(int nGoods, KBid[] bids) {
        // Initialize list
        // Outer list is the list of goods, inner list are list of conflicting bids for that good
        List<List<Integer>> conflicts = new ArrayList<>(nGoods);
        for (int g = 0; g < nGoods; g++) {
            conflicts.add(new ArrayList<>());
        }

        // Populate list
        for (int bid = 0; bid < bids.length; bid++) {
            for (int good: bids[bid].goodsIds) {
                conflicts.get(good).add(bid);
            }
        }
        return conflicts;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        KBid kBid = (KBid) o;

        if (id != kBid.id) return false;
        if (value != kBid.value) return false;
        return Arrays.equals(goodsIds, kBid.goodsIds);
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + value;
        result = 31 * result + Arrays.hashCode(goodsIds);
        return result;
    }
}
