package com.example.syntaxeventlottery;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

public class Event {

    private String eventID;
    private String qrCode;
    private User organizer;
    private Facility facility;
    private Date startDate;
    private Date endDate;
    private int capacity;
    private boolean isFull; // if capacity is full
    private boolean isDrawn; // if lottery draw has taken place
    private ArrayList<User> waitingList;
    private ArrayList<User> selectedList;
    private String poster;

    public Event(User organizer, Facility facility, Date startDate, Date endDate, int capacity) {
        this.eventID = null;
        this.qrCode = null;
        this.organizer = organizer;
        this.facility = facility;
        this.startDate = startDate;
        this.endDate = endDate;
        this.capacity = capacity;
        this.isFull = false;
        this.isDrawn = false;
        this.waitingList = new ArrayList<>();
        this.selectedList = new ArrayList<>();
        this.poster = null;
    }

    public String getQrCode() {
        return qrCode;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }

    public String getEventID() {
        return eventID;
    }

    public void setEventID(String eventID) {
        this.eventID = eventID;
    }

    public User getOrganizer() {
        return organizer;
    }

    public void setOrganizer(User organizer) {
        this.organizer = organizer;
    }

    public Facility getFacility() {
        return facility;
    }

    public void setFacility(Facility facility) {
        this.facility = facility;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public boolean getIsDrawn() {
        return isDrawn;
    }

    public void setIsDrawn(boolean drawn) {
        isDrawn = drawn;
    }

    public boolean getIsFull() {
        return isFull;
    }

    public void setFull(boolean full) {
        isFull = full;
    }

    public ArrayList<User> getWaitingList() {
        return waitingList;
    }

    public void setWaitingList(ArrayList<User> waitingList) {
        this.waitingList = waitingList;
    }

    public ArrayList<User> getSelectedList() {
        return selectedList;
    }

    public void setSelectedList(ArrayList<User> selectedList) {
        this.selectedList = selectedList;
    }
    public String getPoster() {
        return poster;
    }

    public void setPoster(String Poster) {
        this.poster = Poster;
    }

    public void EventIdGenerator(){
        // unique event id will be a combination of system time, organizer device code, facility location, start/end dates
        if (this.eventID == null) {
            StringBuilder idString = new StringBuilder(); // StringBuilder: Class which can manipulate strings
            long currSystemTime = System.currentTimeMillis();
            idString.append(currSystemTime).append('-');
            idString.append(organizer.getDeviceCode()).append('-');
            idString.append(facility.getLocation()).append('-');
            idString.append(String.valueOf(startDate)).append('-');
            idString.append(String.valueOf(endDate));

            this.eventID = idString.toString();
        }
    }

}
