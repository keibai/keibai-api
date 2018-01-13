package main.java.combinatorial;

import java.util.*;

public class KBid {

    public int id;
    public int value;
    public int[] goodsIds;

    public KBid(int id, int value, int[] goodIds) {
        this.id = id;
        this.value = value;
        this.goodsIds = goodIds;
    }

    public static Map<Integer, List<Integer>> getBidConflicts(KBid[] bids) {
        // Map of goodId <-> list of conflicting bids
        Map<Integer, List<Integer>> conflicts = new HashMap<>();

        for (int bid = 0; bid < bids.length; bid++) {
            for (int good: bids[bid].goodsIds) {
                if (!conflicts.containsKey(good)) {
                    conflicts.put(good, new ArrayList<>());
                }
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
