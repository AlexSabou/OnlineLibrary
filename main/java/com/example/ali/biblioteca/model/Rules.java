package com.example.ali.biblioteca.model;

/**
 * Created by Ali on 06.01.2018.
 */

public class Rules {
    private int reservation, loan, tax;

    public Rules() {
        reservation = loan = tax = 0;
    }

    public Rules(int reservation, int loan, int tax) {
        this.reservation = reservation;
        this.loan = loan;
        this.tax = tax;
    }

    public int getReservation() {
        return reservation;
    }

    public void setReservation(int reservation) {
        this.reservation = reservation;
    }

    public int getLoan() {
        return loan;
    }

    public void setLoan(int loan) {
        this.loan = loan;
    }

    public int getTax() {
        return tax;
    }

    public void setTax(int tax) {
        this.tax = tax;
    }

    public String toString() {
        return reservation + "";
    }
}
