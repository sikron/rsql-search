package com.skronawi.rsql.persistence;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.Date;

@Entity
public class Movie {

    @Id
    @GeneratedValue
    String id;
    String title;
    Date year;
    boolean isRatedM;
    int costInMillionDollars;

    public Movie() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Date getYear() {
        return year;
    }

    public void setYear(Date year) {
        this.year = year;
    }

    public boolean isRatedM() {
        return isRatedM;
    }

    public void setRatedM(boolean ratedM) {
        isRatedM = ratedM;
    }

    public int getCostInMillionDollars() {
        return costInMillionDollars;
    }

    public void setCostInMillionDollars(int costInMillionDollars) {
        this.costInMillionDollars = costInMillionDollars;
    }
}
