package com.example.syntaxeventlottery;

/**
 * Represents a general user within the Event Lottery System.
 */
public class User {
    private String userID;
    private String deviceCode;
    private String email;
    private String phoneNumber;
    private String profilePhotoUrl;
    private String username;

    // No-argument constructor required by Firebase
    public User() {}

    // Parameterized constructor
    public User(String deviceCode, String email, String phoneNumber, String profilePhotoUrl, String username) {
        this.deviceCode = deviceCode;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.profilePhotoUrl = profilePhotoUrl;
        this.username = username;
        this.userID = generateUserID(); // Automatically generate userID
    }

    // Method to generate a unique, shorter userID
    private String generateUserID() {
        long timestamp = System.currentTimeMillis() % 10000;
        return username + "_" + timestamp;
    }

    // Getters and Setters
    public String getUserID() {
        return userID;
    }

    // **New Setter for userID**
    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getDeviceCode() {
        return deviceCode;
    }

    public void setDeviceCode(String deviceCode) {
        this.deviceCode = deviceCode;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getProfilePhotoUrl() {
        return profilePhotoUrl;
    }

    public void setProfilePhotoUrl(String profilePhotoUrl) {
        this.profilePhotoUrl = profilePhotoUrl;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String toString() {
        return "User{" +
                "userID='" + userID + '\'' +
                ", deviceCode='" + deviceCode + '\'' +
                ", email='" + email + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", profilePhotoUrl='" + profilePhotoUrl + '\'' +
                ", username='" + username + '\'' +
                '}';
    }
}
