package com.example.doctorapp.models;


public class Doctor {
    private int id;
    private String doctorname;
    private String doctorcategory;
    private String doctordegree;
    private String docotrExp;
    private double rating;
    private int image;

    public int getId() {
        return id;
    }

    public String getDoctorname() {
        return doctorname;
    }

    public String getDoctorcategory() {
        return doctorcategory;
    }

    public String getDoctordegree() {
        return doctordegree;
    }

    public String getDocotrExp() {
        return docotrExp;
    }

    public double getRating() {
        return rating;
    }

    public int getImage() {
        return image;
    }

    public Doctor(int id, String doctorname, String doctorcategory, String doctordegree, String docotrExp, double rating, int image) {
        this.id = id;
        this.doctorname = doctorname;
        this.doctorcategory = doctorcategory;
        this.doctordegree = doctordegree;
        this.docotrExp = docotrExp;
        this.rating = rating;
        this.image = image;
    }
}

