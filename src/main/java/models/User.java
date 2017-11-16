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
        this.updatedAt = timestamp;
    }

    @Override
    public String toString() {
        return "Name: '" + name + "', Last Name: '" + lastName + "', Password: '" + password +
                "', Email: '" + email + "', Credit: '" + credit + "', Created At: '" + createdAt +
                "', Updated At: '" + updatedAt + "'";

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;

        User user = (User) o;

        if (getId() != user.getId()) return false;
        if (Double.compare(user.getCredit(), getCredit()) != 0) return false;
        if (!getName().equals(user.getName())) return false;
        if (getLastName() != null ? !getLastName().equals(user.getLastName()) : user.getLastName() != null)
            return false;
        if (!getPassword().equals(user.getPassword())) return false;
        if (!getEmail().equals(user.getEmail())) return false;
        if (!getCreatedAt().equals(user.getCreatedAt())) return false;
        return getUpdatedAt().equals(user.getUpdatedAt());
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = getId();
        result = 31 * result + getName().hashCode();
        result = 31 * result + (getLastName() != null ? getLastName().hashCode() : 0);
        result = 31 * result + getPassword().hashCode();
        result = 31 * result + getEmail().hashCode();
        temp = Double.doubleToLongBits(getCredit());
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + getCreatedAt().hashCode();
        result = 31 * result + getUpdatedAt().hashCode();
        return result;
    }
}
