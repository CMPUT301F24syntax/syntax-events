package com.example.syntaxeventlottery;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.ServerTimestamp;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.ByteArrayOutputStream;
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
    private String description;
    private String facility;
    private int capacity;
    private boolean isFull;
    private boolean isDrawed;

    @ServerTimestamp
    private Date startDate;

    @ServerTimestamp
    private Date endDate;

    private String organizerId;
    private String posterUrl;
    private String qrCodeUrl;

    /**
     * A list of participant IDs who have joined the event's waiting list.
     */
    private List<String> participants;

    /**
     * A list of participant IDs who have been selected for the event.
     */
    private List<String> selectedParticipants;

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
     * @param facility      Location or facility where the event is held.
     * @param capacity      Maximum capacity of participants.
     * @param startDate     Start date and time of the event.
     * @param endDate       End date and time of the event.
     * @param organizerId   ID of the organizer creating the event.
     */
    public Event(String eventID, String eventName, String description, String facility, int capacity,
                 Date startDate, Date endDate, String organizerId) {
        this.eventID = eventID;
        this.eventName = eventName;
        this.description = description;
        this.facility = facility;
        this.capacity = capacity;
        this.startDate = startDate;
        this.endDate = endDate;
        this.organizerId = organizerId;
        this.participants = new ArrayList<>();
        this.selectedParticipants = new ArrayList<>();
        this.isFull = false;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFacility() {
        return facility;
    }

    public void setFacility(String facility) {
        this.facility = facility;
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

    public String getQrCodeUrl() {
        return qrCodeUrl;
    }

    public void setQrCodeUrl(String qrCodeUrl) {
        this.qrCodeUrl = qrCodeUrl;
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

    // -------------------------------------------------------------------------
    // Participant Management Methods
    // -------------------------------------------------------------------------

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

    // -------------------------------------------------------------------------
    // Utility Methods
    // -------------------------------------------------------------------------

    /**
     * Generates a unique event ID based on the current timestamp and organizer ID.
     *
     * @param organizerId The ID of the organizer.
     */
    public void generateEventID(String organizerId) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        this.eventID = formatter.format(new Date()) + "_" + organizerId;
    }

    /**
     * Generates a QR code bitmap for the event ID.
     *
     * @param content The content to encode in the QR code.
     * @return The generated QR code bitmap.
     */
    @Exclude
    public Bitmap generateQRCodeBitmap(String content) {
        QRCodeWriter writer = new QRCodeWriter();
        try {
            BitMatrix bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, 300, 300);
            Bitmap bmp = Bitmap.createBitmap(300, 300, Bitmap.Config.RGB_565);
            for (int x = 0; x < 300; x++) {
                for (int y = 0; y < 300; y++) {
                    bmp.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            return bmp;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Converts a bitmap image to a byte array.
     *
     * @param bitmap The bitmap image to convert.
     * @return The byte array representation of the bitmap.
     */
    @Exclude
    public byte[] bitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }

    /**
     * Generates and sets the QR code URL for the event.
     */
    @Exclude
    public void generateAndSetQRCodeUrl() {
        Bitmap qrCodeBitmap = generateQRCodeBitmap(this.eventID);
        if (qrCodeBitmap != null) {
            // Code to upload QR code bitmap to Firebase Storage and set qrCodeUrl
            // This should be handled in your repository or storage management code
        }
    }

    /**
     * String representation of the Event object.
     *
     * @return String detailing the event's attributes.
     */
    @Override
    public String toString() {
        return "Event{" +
                "eventID='" + eventID + '\'' +
                ", eventName='" + eventName + '\'' +
                ", description='" + description + '\'' +
                ", facility='" + facility + '\'' +
                ", capacity=" + capacity +
                ", isFull=" + isFull +
                ", isDrawed=" + isDrawed +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", organizerId='" + organizerId + '\'' +
                ", posterUrl='" + posterUrl + '\'' +
                ", qrCodeUrl='" + qrCodeUrl + '\'' +
                ", participants=" + participants +
                ", selectedParticipants=" + selectedParticipants +
                '}';
    }
}