package com.example.syntaxeventlottery;

import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class UserTest {

    // Initialize variables
    private User user;
    private Facility facility;

    @Before
    public void setUp() {
        // Create a mock Facility object
        facility = new Facility("Test Facility", "123 Test Location", "Organizer123");

        // Create a User object
        user = new User(
                "DeviceCode123",
                "test@example.com",
                "1234567890",
                "https://example.com/profile.jpg",
                "TestUser",
                Collections.singleton("Entrant"),
                facility
        );
    }

    @Test
    public void testUserFields() {
        // Verify basic user fields
        assertEquals("DeviceCode123", user.getDeviceCode());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("1234567890", user.getPhoneNumber());
        assertEquals("https://example.com/profile.jpg", user.getProfilePhotoUrl());
        assertEquals("TestUser", user.getUsername());
        assertEquals("DeviceCode123", user.getUserID());
    }

    @Test
    public void testUserRoles() {
        // Verify user roles
        Set<String> expectedRoles = Collections.singleton("Entrant");
        assertEquals(expectedRoles, user.getRoles());
    }

    @Test
    public void testUserFacility() {
        // Verify the facility is correctly associated with the user
        assertNotNull(user.getFacility());
        assertEquals("Test Facility", user.getFacility().getName());
        assertEquals("123 Test Location", user.getFacility().getLocation());
        assertEquals("Organizer123", user.getFacility().getOrganizerId());
    }

    @Test
    public void testSetters() {
        // Update user fields
        user.setDeviceCode("NewDeviceCode");
        user.setEmail("newemail@example.com");
        user.setPhoneNumber("0987654321");
        user.setProfilePhotoUrl("https://example.com/newprofile.jpg");
        user.setUsername("NewTestUser");
        user.setRoles(Collections.singleton("Admin"));

        // Verify updated fields
        assertEquals("NewDeviceCode", user.getDeviceCode());
        assertEquals("newemail@example.com", user.getEmail());
        assertEquals("0987654321", user.getPhoneNumber());
        assertEquals("https://example.com/newprofile.jpg", user.getProfilePhotoUrl());
        assertEquals("NewTestUser", user.getUsername());
        assertEquals(Collections.singleton("Admin"), user.getRoles());
    }
}
