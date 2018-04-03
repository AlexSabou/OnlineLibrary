package com.example.ali.biblioteca.model;

import android.support.annotation.NonNull;

/**
 * Created by Ali on 06.01.2018.
 */

public class Book implements Comparable<Book> {

    public static final String[] stateDesc = new String[]{"Poor", "Fair", "Good", "Very good", "Excellent"};
    public static final int STATE_EXCELLENT = 4;
    public static final int STATE_VERY_GOOD = 3;
    public static final int STATE_GOOD = 2;
    public static final int STATE_FAIR = 1;
    public static final int STATE_POOR = 0;

    private String key, stockKey, location;
    private boolean loaned, reserved;
    private int state;

    public Book() {
        key = location = "";
        loaned = reserved = false;
        state = STATE_EXCELLENT;
    }

    public Book(String stockKey, int state, String location, boolean loaned, boolean reserved) {
        this.stockKey = stockKey;
        this.state = state;
        this.location = location;
        this.loaned = loaned;
        this.reserved = reserved;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getStockKey() {
        return stockKey;
    }

    public void setStockKey(String stockKey) {
        this.stockKey = stockKey;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public boolean isLoaned() {
        return loaned;
    }

    public void setLoaned(boolean loaned) {
        this.loaned = loaned;
    }

    public boolean isReserved() {
        return reserved;
    }

    public void setReserved(boolean reserved) {
        this.reserved = reserved;
    }

    @Override
    public int compareTo(@NonNull Book o) {
        if(this.state < o.getState())
            return 1;
        else if(this.state == o.getState())
            return 0;
        else
            return -1;
    }
}
