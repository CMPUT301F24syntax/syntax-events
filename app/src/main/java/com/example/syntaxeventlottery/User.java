// Refactor Complete
package com.example.syntaxeventlottery;


import android.util.Pair;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Set;

/**
 * Represents a general user within the Event Lottery System.
 */
public class User implements Serializable {
    private String userID;
    private String deviceCode;
    private String email;
    private String phoneNumber;
    private String profilePhotoUrl;
    private String username;
    private Set<String> roles;
    private Facility facility;
    private ArrayList<Double> location;
    private boolean receiveNotifications = true;
    private boolean allowNotification;
    private boolean isDefaultPhoto;

    // No-argument constructor required by Firebase
    public User() {}

    // Parameterized constructor
    public User(String deviceCode, String email, String phoneNumber, String profilePhotoUrl, String username, Set<String> roles, Facility facility,ArrayList<Double> location) {
        this.deviceCode = deviceCode;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.profilePhotoUrl = profilePhotoUrl;
        this.username = username;
        this.userID = generateUserID();
        this.roles = roles;
        this.facility = facility;
        this.location = location;
    }
    public User(String deviceCode, String email, String phoneNumber, String profilePhotoUrl, String username, Set<String> roles, Facility facility) {
        this.deviceCode = deviceCode;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.profilePhotoUrl = profilePhotoUrl;
        this.username = username;
        this.userID = generateUserID();
        this.roles = roles;
        this.facility = facility;

    }

    // Method to generate a unique, shorter userID
    private String generateUserID() {
        // long timestamp = System.currentTimeMillis() % 10000;
        // return username + "_" + timestamp;
        return this.deviceCode;
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

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    public Facility getFacility(){ return facility;}

    public ArrayList<Double> getLocation(){return location;}

    public void setFacility(Facility facility){this.facility = facility;}

    public void setLocation(ArrayList<Double> location){this.location = location;}

    public boolean isReceiveNotifications() {
        return receiveNotifications;
    }

    public void setReceiveNotifications(boolean receiveNotifications) {
        this.receiveNotifications = receiveNotifications;
    }
    public boolean isDefaultPhoto() {
        return isDefaultPhoto;
    }

    public void setDefaultPhoto(boolean isDefaultPhoto) {
        this.isDefaultPhoto = isDefaultPhoto;
    }

    public boolean isAllowNotification() {
        return allowNotification;
    }

    public void setAllowNotification(boolean allowNotification) {
        this.allowNotification = allowNotification;
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
                ", facility='" + facility + '\'' +
                ", roles='" + roles +
                '}';
    }
}
