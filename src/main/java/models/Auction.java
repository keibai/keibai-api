package main.java.models;

import java.sql.Timestamp;

public class Auction {

    public int id;
    public String name;
    public double startingPrice;
    public Timestamp startTime;
    public boolean isValid;
    public int eventId;
    public int ownerId;
    public String status;
    public int winnerId;

}
