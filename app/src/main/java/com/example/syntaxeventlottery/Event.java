package com.example.syntaxeventlottery;

import com.google.firebase.firestore.ServerTimestamp;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Represents an event in the Event Lottery System.
 */
public class Event implements Serializable {

    // -------------------------------------------------------------------------
    // Attributes
    // -------------------------------------------------------------------------

    private String eventID;
    private String eventName;
    private String facility;
    private String description;
    private int capacity;
    private Integer waitingListLimit; // Integer class so that it can be null;
    private boolean capacityFull;
    private boolean waitingListFull;
    private boolean isDrawed;

    @ServerTimestamp
    private Date startDate;

    @ServerTimestamp
    private Date endDate;

    private String organizerId;
    private String posterUrl;
    private String qrCode;
    private ArrayList<String> participants; // those who have joined waiting list
    private ArrayList<String> selectedParticipants; // those who have been selected by lottery
    private ArrayList<String> confirmedParticipants; // those who have confirmed to take part of event

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Default constructor required by Firebase.
     */
    public Event() {
        this.participants = new ArrayList<>();
        this.selectedParticipants = new ArrayList<>();
        this.capacityFull = false;
        this.isDrawed = false;
    }

    /**
     * Parameterized constructor to create an Event with specific details.
     *
     * @param eventName     Name of the event.
     * @param description   Description of the event.
     * @param capacity      Maximum capacity of participants.
     * @param startDate     Start date and time of the event.
     * @param endDate       End date and time of the event.
     * @param organizerId   ID of the organizer creating the event.
     * @param waitingListLimit Limit of the waiting list (i.e., participants list). No limit if null
     */
    public Event(String eventName, String facility, String description, int capacity,
                 Date startDate, Date endDate, String organizerId, Integer waitingListLimit) {
        this.eventID = null;
        this.eventName = eventName;
        this.facility = facility;
        this.description = description;
        this.capacity = capacity;
        this.startDate = startDate;
        this.endDate = endDate;
        this.organizerId = organizerId;
        this.participants = new ArrayList<>();
        this.selectedParticipants = new ArrayList<>();
        this.confirmedParticipants = new ArrayList<>();
        this.waitingListLimit = waitingListLimit;
        this.capacityFull = false;
        this.waitingListFull = false;
        this.isDrawed = false;
    }

    // -------------------------------------------------------------------------
    // Getters and Setters
    // -------------------------------------------------------------------------

    public String getEventID() {
        return eventID;
    }

    public void setEventID(String eventID) {
        this.eventID = eventID;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getFacility() {
        return facility;
    }

    public void setFacility(String facilityName) {
        this.facility = facilityName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public boolean getCapacityFull() {
        return confirmedParticipants.size() >= capacity;
    }

    public void setCapacityFull(boolean full) {
        this.capacityFull = full;
    }

    public Integer getWaitingListLimit() {
        return waitingListLimit;
    }

    public void setWaitingListLimit(Integer waitingListLimit) {
        this.waitingListLimit = waitingListLimit;
    }

    public boolean getWaitingListFull() {
        if (waitingListLimit == null) { // if limit is null, cannot be full
            return false;
        }
        return participants.size() >= waitingListLimit;
    }

    public void setWaitingListFull(boolean full) {
        this.waitingListFull = full;
    }

    public boolean isDrawed() {
        return isDrawed;
    }

    public void setDrawed(boolean drawed) {
        isDrawed = drawed;
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

    public String getOrganizerId() {
        return organizerId;
    }

    public void setOrganizerId(String organizerId) {
        this.organizerId = organizerId;
    }

    public String getPosterUrl() {
        return posterUrl;
    }

    public void setPosterUrl(String posterUrl) {
        this.posterUrl = posterUrl;
    }

    public String getQrCode() {
        return qrCode;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }

    public ArrayList<String> getParticipants() {
        return participants;
    }

    /**
     * Generates a unique event ID based on the current timestamp and organizer ID.
     *
     * @param organizerId The ID of the organizer.
     */
    public void generateEventID(String organizerId) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        this.eventID = formatter.format(new Date()) + "_" + organizerId;
    }

    public void setParticipants(ArrayList<String> participants) {
        this.participants = participants;
    }

    public ArrayList<String> getSelectedParticipants() {
        return selectedParticipants;
    }

    public void setSelectedParticipants(ArrayList<String> selectedParticipants) {
        this.selectedParticipants = selectedParticipants;
    }

    public ArrayList<String> getConfirmedParticipants() {
        return confirmedParticipants;
    }

    public void setConfirmedParticipants(ArrayList<String> confirmedParticipants) {
        this.confirmedParticipants = confirmedParticipants;
    }


    @Override
    public String toString() {
        return "Event{" +
                "eventID='" + eventID + '\'' +
                ", eventName='" + eventName + '\'' +
                ", description='" + description + '\'' +
                ", capacity=" + capacity +
                ", capacityFull=" + capacityFull +
                ", waitingListLimit=" + (waitingListLimit == null ? "No limit set" : waitingListLimit) +
                ", waitingListFull=" + waitingListFull +
                ", isDrawed=" + isDrawed +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", organizerId='" + organizerId + '\'' +
                ", posterUrl='" + posterUrl + '\'' +
                ", qrCode='" + qrCode + '\'' +
                ", participants=" + participants +
                ", selectedParticipants=" + selectedParticipants +
                '}';
    }
}