package com.example.syntaxeventlottery;

import com.google.firebase.firestore.ServerTimestamp;

import java.io.Serializable;
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
    private String description;
    private int capacity;
    private boolean isFull;
    private boolean isDrawed;

    @ServerTimestamp
    private Date startDate;

    @ServerTimestamp
    private Date endDate;

    private String organizerId;
    private String posterUrl;
    private String qrCode;

    /**
     * A list of participant IDs who have joined the event's waiting list.
     */
    private List<String> participants;

    /**
     * A list of participant IDs who have been selected for the event.
     */
    private List<String> selectedParticipants;

    private List<String> confirmedParticipants;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Default constructor required by Firebase.
     */
    public Event() {
        this.participants = new ArrayList<>();
        this.selectedParticipants = new ArrayList<>();
        this.isFull = false;
        this.isDrawed = false;
    }

    /**
     * Parameterized constructor to create an Event with specific details.
     *
     * @param eventID       Unique identifier for the event.
     * @param eventName     Name of the event.
     * @param description   Description of the event.
     * @param capacity      Maximum capacity of participants.
     * @param startDate     Start date and time of the event.
     * @param endDate       End date and time of the event.
     * @param organizerId   ID of the organizer creating the event.
     */
    public Event(String eventID, String eventName, String description, int capacity,
                 Date startDate, Date endDate, String organizerId) {
        this.eventID = eventID;
        this.eventName = eventName;
        this.description = description;
        this.capacity = capacity;
        this.startDate = startDate;
        this.endDate = endDate;
        this.organizerId = organizerId;
        this.participants = new ArrayList<>();
        this.selectedParticipants = new ArrayList<>();
        this.isFull = false;
        this.isDrawed = false;
        this.confirmedParticipants = new ArrayList<>();
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

    public boolean isFull() {
        return isFull;
    }

    public void setFull(boolean full) {
        isFull = full;
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

    public List<String> getParticipants() {
        return participants;
    }

    public void setParticipants(List<String> participants) {
        this.participants = participants;
    }

    public List<String> getSelectedParticipants() {
        return selectedParticipants;
    }

    public void setSelectedParticipants(List<String> selectedParticipants) {
        this.selectedParticipants = selectedParticipants;
    }

    public List<String> getConfirmedParticipants() {
        return confirmedParticipants;
    }

    public void setConfirmedParticipants(List<String> confirmedParticipants) {
        this.confirmedParticipants = confirmedParticipants;
    }
    /**
     * Adds a participant's ID to the event's participants list.
     *
     * @param participantId The ID of the participant to add.
     */
    public void addParticipant(String participantId) {
        if (!this.participants.contains(participantId) && !this.isFull) {
            this.participants.add(participantId);
            checkIfFull();
        }
    }
    /**
     * Removes a participant's ID from the event's participants list.
     *
     * @param participantId The ID of the participant to remove.
     */
    public void removeParticipant(String participantId) {
        this.participants.remove(participantId);
        checkIfFull();
    }
    /**
     * Checks if the event is full based on capacity and updates the isFull flag.
     */
    private void checkIfFull() {
        if (this.participants.size() >= this.capacity) {
            this.isFull = true;
        } else {
            this.isFull = false;
        }
    }

    @Override
    public String toString() {
        return "Event{" +
                "eventID='" + eventID + '\'' +
                ", eventName='" + eventName + '\'' +
                ", description='" + description + '\'' +
                ", capacity=" + capacity +
                ", isFull=" + isFull +
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