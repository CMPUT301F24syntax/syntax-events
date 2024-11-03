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
    private String qrCode; // Base64-encoded QR code string
    private User organizer;
    private String facility;
    private Date startDate;
    private Date endDate;
    private int capacity;
    private boolean isFull;
    private boolean isDrawed;
    private ArrayList<User> waitingList;
    private ArrayList<User> selectedList;
    private String posterUrl; // URL for the event poster or background image

    // Constructor to initialize an event with all parameters
    public Event(String eventName, User organizer, String facility, Date startDate, Date endDate, int capacity) {
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
    }

    // Default constructor
    public Event() {
        this.waitingList = new ArrayList<>();
        this.selectedList = new ArrayList<>();
    }

    // Getter and Setter methods
    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
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

    public String getPosterUrl() {
        return posterUrl;
    }

    public void setPosterUrl(String posterUrl) {
        this.posterUrl = posterUrl;
    }

    // Method to generate a unique event ID based on the organizer's user ID and the current timestamp
    public void generateEventID(String userId) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmm");
        String formattedDateTime = formatter.format(new Date());
        this.eventID = formattedDateTime + "||" + userId;
    }

    // Method to generate a QR code as a Bitmap
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

    // Method to encode Bitmap to Base64 string if needed
    public String encodeToBase64(Bitmap image) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    // Generate and set QR code as a Base64 string
    public void generateAndSetQRCode() {
        Bitmap qrCodeBitmap = generateQRCodeBitmap(this.eventID);
        if (qrCodeBitmap != null) {
            this.qrCode = encodeToBase64(qrCodeBitmap); // Store as Base64-encoded string
        }
    }
}
