package com.example.ali.biblioteca.model;

import android.support.annotation.NonNull;

import java.util.Date;

/**
 * Created by Ali on 06.01.2018.
 */

public class Reservation implements Comparable<Reservation> {
    private String key, stockKey, bookKey, userKey;
    private Date reserveDate, expireDate;
    private boolean cancelled;

    public Reservation() {
        key = bookKey = stockKey = userKey = "";
        cancelled = false;
    }

    public Reservation(String stockKey, String bookKey, String userKey, Date reserveDate, Date expireDate, boolean cancelled) {
        this.stockKey = stockKey;
        this.bookKey = bookKey;
        this.userKey = userKey;
        this.reserveDate = reserveDate;
        this.expireDate = expireDate;
        this.cancelled = cancelled;
    }

    public String getStockKey() {
        return stockKey;
    }

    public void setStockKey(String stockKey) {
        this.stockKey = stockKey;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getBookKey() {
        return bookKey;
    }

    public void setBookKey(String bookKey) {
        this.bookKey = bookKey;
    }

    public String getUserKey() {
        return userKey;
    }

    public void setUserKey(String userKey) {
        this.userKey = userKey;
    }

    public Date getReserveDate() {
        return reserveDate;
    }

    public void setReserveDate(Date reserveDate) {
        this.reserveDate = reserveDate;
    }

    public Date getExpireDate() {
        return expireDate;
    }

    public void setExpireDate(Date expireDate) {
        this.expireDate = expireDate;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    private long getDateDiff(Reservation res) {
        return this.expireDate.getTime() - res.getExpireDate().getTime();
    }

    @Override
    public int compareTo(@NonNull Reservation o) {
        if(this.getDateDiff(o) > 0)
            return -1;
        else if(this.getDateDiff(o) < 0)
            return 1;
        else
            return 0;

    }
}
