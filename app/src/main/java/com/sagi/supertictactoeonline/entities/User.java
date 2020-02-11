package com.sagi.supertictactoeonline.entities;

import com.sagi.supertictactoeonline.utilities.Utils;

import java.io.Serializable;

public class User implements Serializable {

    private String firstName;
    private String lastName;
    private String email;
    private long birthDay;
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

    public User(String firstName, String lastName, String email, long birthDay, long lastTimeSeen, int rank) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.birthDay = birthDay;
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

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public long getBirthDay() {
        return birthDay;
    }

    public void setBirthDay(long birthDay) {
        this.birthDay = birthDay;
    }

    public long getLastTimeSeen() {
        return lastTimeSeen;
    }
}
