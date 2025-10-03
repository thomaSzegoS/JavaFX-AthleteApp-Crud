package com.example.demo;

public class Athlete {

    private int id;
    private String firstName;
    private String lastName;
    private String sport;

    // Athlete Constructor
    public Athlete(int id, String firstName, String lastName, String sport) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.sport = sport;
    }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getSport() { return sport; }
    public void setSport(String sport) { this.sport = sport; }
}
