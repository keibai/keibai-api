package main.java.models;

import java.util.Calendar;

public class Event {
    private int id;
    private String name;
    private int auction_time;
    private String location;
    private Calendar created_at;
    private Calendar updated_at;

    private AuctionType auction_type;
    private Category category;
    private User owner;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAuction_time() {
        return auction_time;
    }

    public void setAuction_time(int auction_time) {
        this.auction_time = auction_time;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Calendar getCreated_at() {
        return created_at;
    }

    public void setCreated_at(Calendar created_at) {
        this.created_at = created_at;
    }

    public Calendar getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(Calendar updated_at) {
        this.updated_at = updated_at;
    }

    public AuctionType getAuction_type() {
        return auction_type;
    }

    public void setAuction_type(AuctionType auction_type) {
        this.auction_type = auction_type;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }
}
