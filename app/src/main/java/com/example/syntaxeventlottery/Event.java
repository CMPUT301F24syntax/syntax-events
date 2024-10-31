package com.example.syntaxeventlottery;

import java.util.ArrayList;

public class Event {

    // Attributes
    private int eventID;
    private String qrCode;
    private String poster;
    private Facility facility;
    private String startDate;
    private String endDate;
    private int capacity;
    private boolean status; // true for full, false for empty
    private ArrayList<String> waitingList;
    private ArrayList<String> participant;

    // Constructor
    public Event() {
        waitingList = new ArrayList<>();
        participant = new ArrayList<>();
    }

    // Getter and Setter for eventID
    public int getEventID() {
        return eventID;
    }

    public void setEventID(int eventID) {
        this.eventID = eventID;
    }

    // Getter and Setter for qrCode
    public String getQrCode() {
        return qrCode;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }

    // Getter and Setter for poster
    public String getPoster() {
        return poster;
    }

    public void setPoster(String poster) {
        this.poster = poster;
    }

    // Additional methods based on UML diagram
    public void createEvent() {
        // Implementation for creating an event
    }

    public void publishEvent() {
        // Implementation for publishing an event
    }

    public void delEvent() {
        // Implementation for deleting an event
    }

    public void updateEvent() {
        // Implementation for updating an event
    }

    public void editParticipantLimit() {
        // Implementation for editing participant limit
    }
}
