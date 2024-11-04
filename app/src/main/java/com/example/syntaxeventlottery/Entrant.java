package com.example.syntaxeventlottery;

import com.google.firebase.firestore.Exclude;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents an Entrant in the Event Lottery System.
 * Extends the User class to include entrant-specific functionalities.
 */
public class Entrant extends User implements Serializable {

    // -------------------------------------------------------------------------
    // Attributes
    // -------------------------------------------------------------------------

    /**
     * A list of event IDs that the entrant has joined the waiting list for.
     */
    private List<String> waitingListEvents;

    /**
     * A list of event IDs that the entrant has been selected for through the lottery.
     */
    private List<String> selectedEvents;

    /**
     * A list of event IDs that the entrant has been invited to register or sign up for.
     */
    private List<String> invitations;

    /**
     * Indicates whether the entrant opts to receive notifications.
     */
    private boolean receiveNotifications;

    /**
     * (Optional) Stores the geolocation data of the entrant.
     */
    private String geoLocation;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Default constructor required by Firebase.
     */
    public Entrant() {
        super(); // Calls the default constructor of the User class
        this.waitingListEvents = new ArrayList<>();
        this.selectedEvents = new ArrayList<>();
        this.invitations = new ArrayList<>();
        this.receiveNotifications = true; // Default to receiving notifications
        this.geoLocation = null; // Default to no geolocation
    }

    /**
     * Parameterized constructor to create an Entrant with specific user details.
     *
     * @param deviceCode      Unique device identifier.
     * @param email           Entrant's email address.
     * @param phoneNumber     Entrant's phone number.
     * @param profilePhotoUrl URL to the entrant's profile photo.
     * @param username        Entrant's chosen username.
     */
    public Entrant(String deviceCode, String email, String phoneNumber, String profilePhotoUrl, String username) {
        super(deviceCode, email, phoneNumber, profilePhotoUrl, username); // Calls the parameterized constructor of User
        this.waitingListEvents = new ArrayList<>();
        this.selectedEvents = new ArrayList<>();
        this.invitations = new ArrayList<>();
        this.receiveNotifications = true; // Default to receiving notifications
        this.geoLocation = null; // Default to no geolocation
    }

    // -------------------------------------------------------------------------
    // Getters and Setters
    // -------------------------------------------------------------------------

    public List<String> getWaitingListEvents() {
        return waitingListEvents;
    }

    public void setWaitingListEvents(List<String> waitingListEvents) {
        this.waitingListEvents = waitingListEvents;
    }

    public void addWaitingListEvent(String eventId) {
        if (!this.waitingListEvents.contains(eventId)) {
            this.waitingListEvents.add(eventId);
        }
    }

    public void removeWaitingListEvent(String eventId) {
        this.waitingListEvents.remove(eventId);
    }

    public List<String> getSelectedEvents() {
        return selectedEvents;
    }

    public void setSelectedEvents(List<String> selectedEvents) {
        this.selectedEvents = selectedEvents;
    }

    public void addSelectedEvent(String eventId) {
        if (!this.selectedEvents.contains(eventId)) {
            this.selectedEvents.add(eventId);
        }
    }

    public void removeSelectedEvent(String eventId) {
        this.selectedEvents.remove(eventId);
    }

    public List<String> getInvitations() {
        return invitations;
    }

    public void setInvitations(List<String> invitations) {
        this.invitations = invitations;
    }

    public void addInvitation(String eventId) {
        if (!this.invitations.contains(eventId)) {
            this.invitations.add(eventId);
        }
    }

    public void removeInvitation(String eventId) {
        this.invitations.remove(eventId);
    }

    public boolean isReceiveNotifications() {
        return receiveNotifications;
    }

    public void setReceiveNotifications(boolean receiveNotifications) {
        this.receiveNotifications = receiveNotifications;
    }

    public String getGeoLocation() {
        return geoLocation;
    }

    public void setGeoLocation(String geoLocation) {
        this.geoLocation = geoLocation;
    }



    // -------------------------------------------------------------------------
    // Utility Methods
    // -------------------------------------------------------------------------

    /**
     * Generates a deterministic profile photo URL based on the entrant's username.
     * Excluded from Firebase serialization.
     *
     * @return URL to the generated profile photo.
     */
    @Exclude
    public String generateDeterministicProfilePicture() {
        if (this.getProfilePhotoUrl() == null || this.getProfilePhotoUrl().isEmpty()) {
            // Placeholder for actual implementation
            return "https://example.com/default_profile_picture.png";
        }
        return this.getProfilePhotoUrl();
    }

    /**
     * String representation of the Entrant object.
     *
     * @return String detailing the entrant's attributes.
     */
    @Override
    public String toString() {
        return "Entrant{" +
                "userID='" + getUserID() + '\'' +
                ", deviceCode='" + getDeviceCode() + '\'' +
                ", email='" + getEmail() + '\'' +
                ", phoneNumber='" + getPhoneNumber() + '\'' +
                ", profilePhotoUrl='" + getProfilePhotoUrl() + '\'' +
                ", username='" + getUsername() + '\'' +
                ", waitingListEvents=" + waitingListEvents +
                ", selectedEvents=" + selectedEvents +
                ", invitations=" + invitations +
                ", receiveNotifications=" + receiveNotifications +
                ", geoLocation='" + geoLocation + '\'' +
                '}';
    }

    /**
     * Adds the Entrant to the waiting list of the specified event.
     *
     * @param event The event to join.
     */
    public void joinEvent(Event event) {
        if (!event.getWaitingList().contains(this)) {
            event.getWaitingList().add(this); // Add this Entrant to the event's waiting list
            addWaitingListEvent(event.getEventID()); // Add the event ID to this Entrant's waiting list
        }
    }

    /**
     * Removes the Entrant from the waiting list of the specified event.
     *
     * @param event The event to leave.
     */
    public void leaveEvent(Event event) {
        if (event.getWaitingList().contains(this)) {
            event.getWaitingList().remove(this); // Remove this Entrant from the event's waiting list
            removeWaitingListEvent(event.getEventID()); // Remove the event ID from this Entrant's waiting list
        }
    }

}
