package com.example.syntaxeventlottery;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for the User class.
 */
public class UserTest {
    private User user;

    @Before
    public void setUp() {
        // Initialize a User object with test data
        user = new User(
                "device12345",      // deviceCode
                "testuser@example.com", // email
                "123-456-7890",     // phoneNumber
                "http://example.com/profile.jpg", // profilePhotoUrl
                "testuser",         // username
                "testFacility"      // facility
        );
    }

    @Test
    public void testUserInitialization() {
        // Verify that the attributes are initialized correctly
        assertEquals("device12345", user.getDeviceCode());
        assertEquals("testuser@example.com", user.getEmail());
        assertEquals("123-456-7890", user.getPhoneNumber());
        assertEquals("http://example.com/profile.jpg", user.getProfilePhotoUrl());
        assertEquals("testuser", user.getUsername());
        assertEquals("testFacility", user.getFacility());
    }

    @Test
    public void testGenerateUserID() {
        // Verify that userID is generated correctly
        String expectedUserID = "device12345"; // userID should match the deviceCode
        assertEquals(expectedUserID, user.getUserID());
    }

    @Test
    public void testSettersAndGetters() {
        // Test setters and getters for all attributes
        user.setDeviceCode("newDevice456");
        assertEquals("newDevice456", user.getDeviceCode());

        user.setEmail("newemail@example.com");
        assertEquals("newemail@example.com", user.getEmail());

        user.setPhoneNumber("987-654-3210");
        assertEquals("987-654-3210", user.getPhoneNumber());

        user.setProfilePhotoUrl("http://example.com/newprofile.jpg");
        assertEquals("http://example.com/newprofile.jpg", user.getProfilePhotoUrl());

        user.setUsername("newuser");
        assertEquals("newuser", user.getUsername());

        user.setFacility("newFacility");
        assertEquals("newFacility", user.getFacility());

        user.setUserID("customUserID");
        assertEquals("customUserID", user.getUserID());
    }

}
