package com.sagi.supertictactoeonline.entities;

import com.sagi.supertictactoeonline.utilities.Utils;

import java.io.Serializable;

public class User implements Serializable {

    private String firstName;
    private String email;
    private long lastTimeSeen;
    private int rank;

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public void addRank(int rank) {
        this.rank += rank;
    }

    public void removeRank(int rank) {
        this.rank -= rank;
    }

    public String textEmailForFirebase() {
       return  Utils.textEmailForFirebase(email);
    }

    public User(String firstName, String email, long lastTimeSeen, int rank) {
        this.firstName = firstName;
        this.email = email;
        this.lastTimeSeen = lastTimeSeen;
        this.rank = rank;
    }

    public User() {
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public long getLastTimeSeen() {
        return lastTimeSeen;
    }
}
