package com.example.syntaxeventlottery;

import com.google.firebase.firestore.auth.User;

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
    private boolean isDrawed; // if lottery draw has taken place
    private ArrayList<Users> waitingList;
    private ArrayList<Users> selectedList;

    public Event(String eventID, Users organizer, Facility facility, Date startDate, Date endDate, int capacity) {
        this.eventID = eventID;
        this.
        this.organizer = organizer;
        this.facility = facility;
        this.startDate = startDate;
        this.endDate = endDate;
        this.capacity = capacity;
        this.isFull = false;
        this.isDrawed = false;
    }

    public String getQrCode() {
        return qrCode;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }

    public int getEventID() {
        return eventID;
    }

    public void setEventID(int eventID) {
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

    public boolean isDrawed() {
        return isDrawed;
    }

    public void setDrawed(boolean drawed) {
        isDrawed = drawed;
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
}
