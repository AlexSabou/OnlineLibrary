package com.example.ali.biblioteca.model;

import java.io.Serializable;

/**
 * Created by Ali on 06.01.2018.
 */

public class Stock implements Serializable {
    public static final String[] GENRES = new String[]{"Action and adventure", "Anthology", "Art", "Autobiographies", "Biographies", "Children's", "Comics", "Cookbooks", "Diaries",
            "Dictionaries", "Drama", "Encyclopedias", "Fantasy", "Guide", "Health", "History", "Horror", "IT", "Journals", "Math", "Mystery", "Poetry",
            "Prayer books", "Religion", "Romance", "Satire", "Science", "Science fiction", "Self help", "Series", "Travel", "Trilogy"};

    private String key, title, author, genre, description, imageUrl;
    private int nrOfBooks;
    private long rating, votes;

    public Stock() {
        key = title = author = genre = description = imageUrl = "";
        nrOfBooks = 0;
        rating = votes = 0;
    }

    public Stock(String title, String author, String genre, String description, String imageUrl, int nrOfBooks) {
        this.title = title;
        this.author = author;
        this.genre = genre;
        this.description = description;
        this.imageUrl = imageUrl;
        this.nrOfBooks = nrOfBooks;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public int getNrOfBooks() {
        return nrOfBooks;
    }

    public void setNrOfBooks(int nrOfBooks) {
        this.nrOfBooks = nrOfBooks;
    }

    public long getRating() {
        return rating;
    }

    public void setRating(long rating) {
        this.rating = rating;
    }

    public long getVotes() {
        return votes;
    }

    public void setVotes(long votes) {
        this.votes = votes;
    }
}
