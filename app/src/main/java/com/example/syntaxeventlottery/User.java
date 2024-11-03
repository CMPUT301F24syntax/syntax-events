package com.example.syntaxeventlottery;

public class User {
    private String deviceCode;
    private String email;
    private String phoneNumber;
    private String profilePhotoUrl;
    private String username;
    private RoleManager roleManager;

    // No-argument constructor (required for Firebase reflection)
    public User() {}

    // Parameterized constructor
    public User(String deviceCode, String email, String phoneNumber, String profilePhotoUrl, String username, UserRepository userRepository) {
        this.deviceCode = deviceCode;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.profilePhotoUrl = profilePhotoUrl;
        this.username = username;
        this.roleManager = new RoleManager(deviceCode, userRepository);
    }

    // Getters and Setters
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

    public RoleManager getRoleManager() {
        return roleManager;
    }

    @Override
    public String toString() {
        return "Users{" +
                "deviceCode='" + deviceCode + '\'' +
                ", email='" + email + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", profilePhotoUrl='" + profilePhotoUrl + '\'' +
                ", username='" + username + '\'' +
                ", roles=" + roleManager.getRolesString() +
                '}';
    }
}
