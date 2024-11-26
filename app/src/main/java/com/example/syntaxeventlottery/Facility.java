package com.example.syntaxeventlottery;

import com.google.firebase.firestore.auth.User;

import java.util.Date;

public class Facility {

    // Attributes
    private String name;
    private String location;
    private String organizerId;

    // No-argument constructor required by Firestore
    public Facility() {}

    public Facility(String name, String location, String organizerId) {
        this.name = name;
        this.location = location;
        this.organizerId = organizerId;
    }

    // Getter and Setter for location
    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    // Getter and Setter for organizer
    public String getName() {
        return name;
    }

    public void setName() {
        this.name = name;
    }

    // Getter and Setter for organizer
    public String getOrganizerId() {
        return organizerId;
    }

    public void setOrganizer(String organizerId) {
        this.organizerId = organizerId;
    }
}
