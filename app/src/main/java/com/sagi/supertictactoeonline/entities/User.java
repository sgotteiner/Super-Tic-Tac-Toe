package com.sagi.supertictactoeonline.entities;

import java.io.Serializable;

public class User implements Serializable {

    private String name;
    private String key;
    private long lastTimeSeen;
    private int rank;

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public User(String name, String key, long lastTimeSeen, int rank) {
        this.name = name;
        this.key = key;
        this.lastTimeSeen = lastTimeSeen;
        this.rank = rank;
    }

    public User() {
    }

    public String getName() {
        return name;
    }

    public String getKey() {
        return key;
    }

    public long getLastTimeSeen() {
        return lastTimeSeen;
    }

    public void setName(String name) {
        this.name = name;
    }
}
