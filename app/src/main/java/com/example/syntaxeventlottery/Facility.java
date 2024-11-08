package com.example.syntaxeventlottery;



import java.util.Date;

public class Facility {

    // Attributes
    private String location;
    private User organizer;
    private Date startDate;
    private Date endDate;

    // Getter and Setter for location
    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    // Getter and Setter for organizer
    public User getOrganizer() {
        return organizer;
    }

    public void setOrganizer(User organizer) {
        this.organizer = organizer;
    }

    // Setter for start and end dates
    public void setDates(Date startDate, Date endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }

    // Getter for start and end dates
    public Date getStartDate() {
        return startDate;
    }

    public Date getEndDate() {
        return endDate;
    }
}
