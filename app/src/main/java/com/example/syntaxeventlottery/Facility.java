package com.example.syntaxeventlottery;

import com.google.firebase.firestore.auth.User;

import java.util.Date;

/**
 * Represents a facility where events are organized.
 * Contains information about the facility's name, location, and the ID of the organizer.
 */
public class Facility {

    // Attributes
    private String name;
    private String location;
    private String organizerId;

    /**
     * No-argument constructor required by Firestore for deserialization.
     */
    public Facility() {}


    /**
     * Constructs a new Facility object with the specified name, location, and organizer ID.
     *
     * @param name        The name of the facility.
     * @param location    The location of the facility.
     * @param organizerId The ID of the organizer associated with the facility.
     */
    public Facility(String name, String location, String organizerId) {
        this.name = name;
        this.location = location;
        this.organizerId = organizerId;
    }


    /**
     * Retrieves the location of the facility.
     *
     * @return The facility's location.
     */
    public String getLocation() {
        return location;
    }


    /**
     * Sets the location of the facility.
     *
     * @param location The new location of the facility.
     */
    public void setLocation(String location) {
        this.location = location;
    }


    /**
     * Retrieves the name of the facility.
     *
     * @return The facility's name.
     */
    public String getName() {
        return name;
    }


    /**
     * Sets the name of the facility.
     *
     * @param name The new name of the facility.
     */
    public void setName(String name) {
        this.name = name;
    }


    /**
     * Retrieves the organizer ID associated with the facility.
     *
     * @return The organizer ID.
     */
    public String getOrganizerId() {
        return organizerId;
    }


    /**
     * Sets the organizer ID for the facility.
     *
     * @param organizerId The new organizer ID.
     */
    public void setOrganizer(String organizerId) {
        this.organizerId = organizerId;
    }
}
