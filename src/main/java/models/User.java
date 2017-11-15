package main.java.models;

import java.sql.Timestamp;

public class User {

    private int id;
    private String name;
    private String lastName;
    private String password;
    private String email;
    private double credit;
    private Timestamp createdAt;
    private Timestamp updatedAt;

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

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public double getCredit() {
        return credit;
    }

    public void setCredit(double credit) {
        this.credit = credit;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp timestamp) {
        this.createdAt = timestamp;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp timestamp) {
        this.createdAt = timestamp;
    }

    @Override
    public String toString() {
        return "Name: '" + name + "', Last Name: '" + lastName + "', Password: '" + password +
                "', Email: '" + email + "', Credit: '" + credit + "', Created At: '" + createdAt +
                "', Updated At: '" + updatedAt + "'";

    }
}
