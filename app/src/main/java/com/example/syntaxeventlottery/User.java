package com.example.syntaxeventlottery;

import android.util.Pair;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Set;

/**
 * Represents a general user within the Event Lottery System.
 * The {@code User} class stores various details about a user, including their ID, device code,
 * email, phone number, profile photo, roles, associated facility, and location.
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

    /**
     * Default no-argument constructor required for Firebase.
     */
    public User() {}

    /**
     * Constructs a new {@code User} object with the specified parameters.
     *
     * @param deviceCode      The device code associated with the user.
     * @param email           The user's email address.
     * @param phoneNumber     The user's phone number.
     * @param profilePhotoUrl The URL of the user's profile photo.
     * @param username        The user's username.
     * @param roles           The set of roles assigned to the user.
     * @param facility        The {@code Facility} object associated with the user.
     * @param location        The geographical location of the user as a list of doubles.
     */
    public User(String deviceCode, String email, String phoneNumber, String profilePhotoUrl, String username, Set<String> roles, Facility facility, ArrayList<Double> location) {
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

    /**
     * Constructs a new {@code User} object without specifying location.
     *
     * @param deviceCode      The device code associated with the user.
     * @param email           The user's email address.
     * @param phoneNumber     The user's phone number.
     * @param profilePhotoUrl The URL of the user's profile photo.
     * @param username        The user's username.
     * @param roles           The set of roles assigned to the user.
     * @param facility        The {@code Facility} object associated with the user.
     */
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

    /**
     * Generates a unique user ID using the device code.
     *
     * @return A unique user ID string.
     */
    private String generateUserID() {
        return this.deviceCode;
    }

    // Getters and Setters

    /**
     * @return The user's unique ID.
     */
    public String getUserID() {
        return userID;
    }

    /**
     * Sets the user's unique ID.
     *
     * @param userID The user's new unique ID.
     */
    public void setUserID(String userID) {
        this.userID = userID;
    }

    /**
     * @return The device code associated with the user.
     */
    public String getDeviceCode() {
        return deviceCode;
    }

    /**
     * Sets the device code for the user.
     *
     * @param deviceCode The device code to set.
     */
    public void setDeviceCode(String deviceCode) {
        this.deviceCode = deviceCode;
    }

    /**
     * @return The user's email address.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the user's email address.
     *
     * @param email The email address to set.
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * @return The user's phone number.
     */
    public String getPhoneNumber() {
        return phoneNumber;
    }

    /**
     * Sets the user's phone number.
     *
     * @param phoneNumber The phone number to set.
     */
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    /**
     * @return The URL of the user's profile photo.
     */
    public String getProfilePhotoUrl() {
        return profilePhotoUrl;
    }

    /**
     * Sets the URL of the user's profile photo.
     *
     * @param profilePhotoUrl The profile photo URL to set.
     */
    public void setProfilePhotoUrl(String profilePhotoUrl) {
        this.profilePhotoUrl = profilePhotoUrl;
    }

    /**
     * @return The username of the user.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the username of the user.
     *
     * @param username The username to set.
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * @return The roles assigned to the user.
     */
    public Set<String> getRoles() {
        return roles;
    }

    /**
     * Sets the roles assigned to the user.
     *
     * @param roles The set of roles to assign.
     */
    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    /**
     * @return The facility associated with the user.
     */
    public Facility getFacility() {
        return facility;
    }

    /**
     * Sets the facility associated with the user.
     *
     * @param facility The facility to associate with the user.
     */
    public void setFacility(Facility facility) {
        this.facility = facility;
    }

    /**
     * @return The geographical location of the user.
     */
    public ArrayList<Double> getLocation() {
        return location;
    }

    /**
     * Sets the geographical location of the user.
     *
     * @param location The location to set.
     */
    public void setLocation(ArrayList<Double> location) {
        this.location = location;
    }

    /**
     * @return {@code true} if the user receives notifications, {@code false} otherwise.
     */
    public boolean isReceiveNotifications() {
        return receiveNotifications;
    }

    /**
     * Sets whether the user receives notifications.
     *
     * @param receiveNotifications {@code true} to enable notifications, {@code false} otherwise.
     */
    public void setReceiveNotifications(boolean receiveNotifications) {
        this.receiveNotifications = receiveNotifications;
    }

    /**
     * @return {@code true} if the user's profile photo is the default, {@code false} otherwise.
     */
    public boolean isDefaultPhoto() {
        return isDefaultPhoto;
    }

    /**
     * Sets whether the user's profile photo is the default.
     *
     * @param isDefaultPhoto {@code true} to set as default, {@code false} otherwise.
     */
    public void setDefaultPhoto(boolean isDefaultPhoto) {
        this.isDefaultPhoto = isDefaultPhoto;
    }

    /**
     * @return {@code true} if notifications are allowed, {@code false} otherwise.
     */
    public boolean isAllowNotification() {
        return allowNotification;
    }

    /**
     * Sets whether notifications are allowed for the user.
     *
     * @param allowNotification {@code true} to allow notifications, {@code false} otherwise.
     */
    public void setAllowNotification(boolean allowNotification) {
        this.allowNotification = allowNotification;
    }

    /**
     * Provides a string representation of the user object.
     *
     * @return A string containing the user's details.
     */
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
