package com.example.ali.biblioteca.model;

import java.io.Serializable;

/**
 * Created by Ali on 06.01.2018.
 */

public class User implements Serializable {
    public static final int ROLE_USER = 0;
    public static final int ROLE_LIBRARIAN = 1;
    public static final int ROLE_ADMIN = 2;

    private String key, name, email, cnp;
    private int role, booksLoaned, booksReserved;
    private boolean blocked;

    public User() {
        key = name = email = cnp = "";
        role = booksLoaned = booksReserved = 0;
        blocked = false;
    }

    public User(String name, String email, String cnp, int role, int booksLoaned, int booksReserved, boolean blocked) {
        this.name = name;
        this.email = email;
        this.cnp = cnp;
        this.role = role;
        this.booksLoaned = booksLoaned;
        this.booksReserved = booksReserved;
        this.blocked = blocked;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCnp() {
        return cnp;
    }

    public void setCnp(String cnp) {
        this.cnp = cnp;
    }

    public int getRole() {
        return role;
    }

    public void setRole(int role) {
        this.role = role;
    }

    public int getBooksLoaned() {
        return booksLoaned;
    }

    public void setBooksLoaned(int booksLoaned) {
        this.booksLoaned = booksLoaned;
    }

    public String toString() {
        return "Email: " + email;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }

    public int getBooksReserved() {
        return booksReserved;
    }

    public void setBooksReserved(int booksReserved) {
        this.booksReserved = booksReserved;
    }
}
