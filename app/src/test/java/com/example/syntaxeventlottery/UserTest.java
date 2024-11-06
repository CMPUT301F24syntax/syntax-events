package com.example.syntaxeventlottery;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests for the User class.
 */
public class UserTest {

    private User user;

    @Before
    public void setUp() {
        // Initialize User with sample data
        user = new User("device1", "test_email@example.com", "1234", "http://example.com", "test");
    }

    @Test
    public void testConstructor() {
        // Verify that constructor initializes fields correctly
        assertEquals("device1", user.getDeviceCode());
        assertEquals("test_email@example.com", user.getEmail());
        assertEquals("1234", user.getPhoneNumber());
        assertEquals("http://example.com", user.getProfilePhotoUrl());
        assertEquals("test", user.getUsername());
    }

    @Test
    public void testSetDeviceCode() {
        user.setDeviceCode("device2");
        assertEquals("device2", user.getDeviceCode());
    }

    @Test
    public void testSetEmail() {
        user.setEmail("test2_email@example.com");
        assertEquals("test2_email@example.com", user.getEmail());
    }

    @Test
    public void testSetPhoneNumber() {
        user.setPhoneNumber("5678");
        assertEquals("5678", user.getPhoneNumber());
    }

    @Test
    public void testSetProfilePhotoUrl() {
        user.setProfilePhotoUrl("http://example2.com");
        assertEquals("http://example2.com", user.getProfilePhotoUrl());
    }

    @Test
    public void testSetUsername() {
        user.setUsername("new_test");
        assertEquals("new_test", user.getUsername());
    }

    @Test
    public void testSetUserID() {
        user.setUserID("new_userID");
        assertEquals("new_userID", user.getUserID());
    }

    @Test
    public void testGenerateUserID() {
        String userID = user.getUserID();
        assertNotNull(userID);
        assertTrue(userID.startsWith("test_")); // Ensure userID is generated with username prefix
    }

    @Test
    public void testToString() {
        String string = "User{userID='" + user.getUserID() + "', deviceCode='device1', email='test_email@example.com', phoneNumber='1234', profilePhotoUrl='http://example.com', username='test'}";
        assertEquals(string, user.toString());
    }
}