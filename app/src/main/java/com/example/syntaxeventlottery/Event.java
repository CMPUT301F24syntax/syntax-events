package com.example.syntaxeventlottery;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;

public class Event {

    private String eventID;
    private String qrCode;
    private Users organizer;
    private Facility facility;
    private Date startDate;
    private Date endDate;
    private int capacity;
    private boolean isFull; // if capacity is full
    private boolean isDrawn; // if lottery draw has taken place
    private ArrayList<Users> waitingList;
    private ArrayList<Users> selectedList;
    private String poster;

    public Event(Users organizer, Facility facility, Date startDate, Date endDate, int capacity) {
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

    public Users getOrganizer() {
        return organizer;
    }

    public void setOrganizer(Users organizer) {
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

    public boolean isDrawn() {
        return isDrawn;
    }

    public void setDrawn(boolean drawn) {
        isDrawn = drawn;
    }

    public boolean isFull() {
        return isFull;
    }

    public void setFull(boolean full) {
        isFull = full;
    }

    public ArrayList<Users> getWaitingList() {
        return waitingList;
    }

    public void setWaitingList(ArrayList<Users> waitingList) {
        this.waitingList = waitingList;
    }

    public ArrayList<Users> getSelectedList() {
        return selectedList;
    }

    public void setSelectedList(ArrayList<Users> selectedList) {
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
