package main.java.models;

import java.sql.Timestamp;

public class Bid extends ModelAbstract implements Comparable<Bid> {

    public double amount;
    public Timestamp createdAt;
    public int auctionId;
    public int goodId;
    public int ownerId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Bid bid = (Bid) o;

        if (Double.compare(bid.amount, amount) != 0) return false;
        if (auctionId != bid.auctionId) return false;
        if (goodId != bid.goodId) return false;
        if (ownerId != bid.ownerId) return false;
        return createdAt != null ? createdAt.equals(bid.createdAt) : bid.createdAt == null;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(amount);
        result = (int) (temp ^ (temp >>> 32));
        result = 31 * result + (createdAt != null ? createdAt.hashCode() : 0);
        result = 31 * result + auctionId;
        result = 31 * result + goodId;
        result = 31 * result + ownerId;
        return result;
    }

    @Override
    public String toString() {
        return "Bid{" +
                "amount=" + amount +
                ", createdAt=" + createdAt +
                ", auctionId=" + auctionId +
                ", goodId=" + goodId +
                ", ownerId=" + ownerId +
                ", id=" + id +
                '}';
    }

    @Override
    public int compareTo(Bid bid) {
        return Double.compare(this.amount, bid.amount);
    }
}
