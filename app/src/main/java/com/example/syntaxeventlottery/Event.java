package com.example.syntaxeventlottery;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Base64;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Event {
    private String eventName;
    private String eventID;
    private String qrCode;  // Base64-encoded QR code string
    private String qrCodeUrl;  // URL for the QR code image in Firebase Storage
    private String posterUrl;  // URL for the poster image in Firebase Storage
    private User organizer;
    private String facility;
    private Date startDate;
    private Date endDate;
    private int capacity;
    private boolean isFull;
    private boolean isDrawed;
    private ArrayList<User> waitingList;
    private ArrayList<User> selectedList;
    private String description; // New attribute for event description

    // Constructors
    public Event(String eventName, User organizer, String facility, Date startDate, Date endDate, int capacity, String description) {
        this.eventName = eventName;
        this.organizer = organizer;
        this.facility = facility;
        this.startDate = startDate;
        this.endDate = endDate;
        this.capacity = capacity;
        this.isFull = false;
        this.isDrawed = false;
        this.waitingList = new ArrayList<>();
        this.selectedList = new ArrayList<>();
        this.description = description; // Initialize description
    }

    public Event() {
        this.waitingList = new ArrayList<>();
        this.selectedList = new ArrayList<>();
    }

    // Getter and Setter methods
    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }
    public String getQrCode() { return qrCode; }
    public void setQrCode(String qrCode) { this.qrCode = qrCode; }
    public String getEventID() { return eventID; }
    public void setEventID(String eventID) { this.eventID = eventID; }
    public User getOrganizer() { return organizer; }
    public void setOrganizer(User organizer) { this.organizer = organizer; }
    public String getFacility() { return facility; }
    public void setFacility(String facility) { this.facility = facility; }
    public Date getStartDate() { return startDate; }
    public void setStartDate(Date startDate) { this.startDate = startDate; }
    public Date getEndDate() { return endDate; }
    public void setEndDate(Date endDate) { this.endDate = endDate; }
    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    public boolean isDrawed() { return isDrawed; }
    public void setDrawed(boolean drawed) { isDrawed = drawed; }
    public boolean isFull() { return isFull; }
    public void setFull(boolean full) { isFull = full; }
    public ArrayList<User> getWaitingList() { return waitingList; }
    public void setWaitingList(ArrayList<User> waitingList) { this.waitingList = waitingList; }
    public ArrayList<User> getSelectedList() { return selectedList; }
    public void setSelectedList(ArrayList<User> selectedList) { this.selectedList = selectedList; }
    public String getPosterUrl() { return posterUrl; }
    public void setPosterUrl(String posterUrl) { this.posterUrl = posterUrl; }
    public String getQrCodeUrl() { return qrCodeUrl; }
    public void setQrCodeUrl(String qrCodeUrl) { this.qrCodeUrl = qrCodeUrl; }

    // Getter and Setter for the new description attribute
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    // Method to generate event ID
    public void generateEventID(String userId) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmm");
        this.eventID = formatter.format(new Date()) + "||" + userId;
    }

    // QR Code Generation and Encoding
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

    public String encodeToBase64(Bitmap image) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    public void generateAndSetQRCode() {
        Bitmap qrCodeBitmap = generateQRCodeBitmap(this.eventID);
        if (qrCodeBitmap != null) {
            this.qrCode = encodeToBase64(qrCodeBitmap);
        }
    }
}
