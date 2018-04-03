package com.example.ali.biblioteca.model;

import android.support.annotation.NonNull;

import java.util.Date;

/**
 * Created by Ali on 06.01.2018.
 */

public class Loan implements Comparable<Loan> {
    private String key, stockKey, bookKey, userKey;
    private Date loanDate, returnDate;
    private boolean returned;

    public Loan() {
        key = stockKey = bookKey = userKey = "";
        returned = false;
    }

    public Loan(String stockKey, String bookKey, String userKey, Date loanDate, Date returnDate, boolean returned) {
        this.stockKey = stockKey;
        this.bookKey = bookKey;
        this.userKey = userKey;
        this.loanDate = loanDate;
        this.returnDate = returnDate;
        this.returned = returned;
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

    public Date getLoanDate() {
        return loanDate;
    }

    public void setLoanDate(Date loanDate) {
        this.loanDate = loanDate;
    }

    public Date getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(Date returnDate) {
        this.returnDate = returnDate;
    }

    public boolean isReturned() {
        return returned;
    }

    public void setReturned(boolean returned) {
        this.returned = returned;
    }

    private long getDateDiff(Loan loan) {
        return this.returnDate.getTime() - loan.getReturnDate().getTime();
    }

    @Override
    public int compareTo(@NonNull Loan o) {
        if(this.getDateDiff(o) > 0)
            return -1;
        else if(this.getDateDiff(o) < 0)
            return 1;
        else
            return 0;

    }
}
