package com.skronawi.rsql.persistence;

import javax.persistence.*;
import java.util.Date;

@Entity
public class Movie {

    @Id
    @GeneratedValue
    private String id;
    private String title;
    private Date year;
    private boolean isRatedM;
    private int costInMillionDollars;

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinColumn(name = "regisseur_id")
    private Person regisseur;

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

    public Person getRegisseur() {
        return regisseur;
    }

    public void setRegisseur(Person regisseur) {
        this.regisseur = regisseur;
    }
}
