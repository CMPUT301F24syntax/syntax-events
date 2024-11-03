package com.example.syntaxeventlottery;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;

public class Event {
    private String eventName;
    private String eventID;
    private String qrCode;
    private User organizer;
    private String facility;
    private Date startDate;
    private Date endDate;
    private int capacity;
    private boolean isFull; // True if capacity is full
    private boolean isDrawed; // True if lottery draw has taken place
    private ArrayList<User> waitingList; // List of users on the waiting list
    private ArrayList<User> selectedList; // List of selected users
    private String Poster;
    private URL backgroundImageUrl; // URL for the background image of the event

    // Constructor to initialize an event with all parameters
    public Event(String eventName, User organizer, String facility, Date startDate, Date endDate, int capacity) {
        this.eventName = eventName;
        this.qrCode = null;
        this.organizer = organizer;
        this.facility = facility;
        this.startDate = startDate;
        this.endDate = endDate;
        this.capacity = capacity;
        this.isFull = false;
        this.isDrawed = false;
        this.waitingList = new ArrayList<>(); // Initialize the waiting list
        this.selectedList = new ArrayList<>(); // Initialize the selected list
    }

    // Default constructor
    public Event() {
        this.waitingList = new ArrayList<>();
        this.selectedList = new ArrayList<>();
    }

    // Additional constructor with basic event details (for loading from database)
    public Event(String eventID, String eventName, Date startDate, Date endDate, String facility, int capacity) {
        this.eventID = eventID;
        this.eventName = eventName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.facility = facility;
        this.capacity = capacity;
        this.waitingList = new ArrayList<>();
        this.selectedList = new ArrayList<>();
    }

    // Getter and Setter methods for eventName
    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    // Getter and Setter methods for other properties

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

    public String getFacility() {
        return facility;
    }

    public void setFacility(String facility) {
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
        return Poster;
    }

    public void setPoster(String Poster) {
        this.Poster = Poster;
    }

    public URL getBackgroundImageUrl() {
        return backgroundImageUrl;
    }

    public void setBackgroundImageUrl(URL backgroundImageUrl) {
        this.backgroundImageUrl = backgroundImageUrl;
    }

    // Method to set background image URL from a file path
    public boolean setBackgroundImageFromFilePath(String filePath) {
        try {
            File file = new File(filePath);
            this.backgroundImageUrl = file.toURI().toURL();
            return true;
        } catch (MalformedURLException e) {
            System.out.println("Invalid file path: " + e.getMessage());
            return false;
        }
    }

    // Method to generate a unique event ID based on the organizer's user ID and the current timestamp
    public void EventIdGenerator(String userId) {
        if (this.eventID == null) {
            LocalDateTime now = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                now = LocalDateTime.now();
            }
            DateTimeFormatter formatter = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
            }
            String formattedDateTime = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                formattedDateTime = now.format(formatter);
            }
            this.eventID = formattedDateTime + "||" + userId;
        }
    }

    // Method to add a user to the waiting list
    public void addToWaitingList(User user) {
        if (waitingList == null) {
            waitingList = new ArrayList<>();
        }
        waitingList.add(user);

        // Check if the event has reached its capacity
        if (waitingList.size() >= capacity) {
            isFull = true;
        }
    }

    // Method to remove a user from the waiting list
    public void removeFromWaitingList(User user) {
        if (waitingList != null && waitingList.contains(user)) {
            waitingList.remove(user);
            isFull = waitingList.size() >= capacity;
        }
    }

    // Method to retrieve the details of all users in the waiting list
    public String getWaitingListDetails() {
        StringBuilder details = new StringBuilder("Waiting List:\n");

        if (waitingList != null && !waitingList.isEmpty()) {
            for (User user : waitingList) {
                details.append("Name: ").append(user.getUsername()).append(", ");
                details.append("Email: ").append(user.getEmail()).append(", ");
                details.append("Phone: ").append(user.getPhoneNumber()).append("\n");
            }
        } else {
            details.append("No users in the waiting list.");
        }
        return details.toString();
    }

    // Method to select users for the event (lottery draw)
    public void drawLottery(int numberOfWinners) {
        if (waitingList.size() < numberOfWinners) {
            selectedList = new ArrayList<>(waitingList); // Select all if fewer than winners
        } else {
            selectedList = new ArrayList<>();
            while (selectedList.size() < numberOfWinners) {
                int index = (int) (Math.random() * waitingList.size());
                User selectedUser = waitingList.get(index);
                if (!selectedList.contains(selectedUser)) {
                    selectedList.add(selectedUser);
                }
            }
        }
        isDrawed = true;
    }
}