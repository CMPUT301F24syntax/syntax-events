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
        user = new User("device1", "test_email@example.com", "1234", "http://example.com/photo.jpg", "test");
    }

    @Test
    public void testConstructor() {
        // Verify that constructor initializes fields correctly
        assertEquals("device1", user.getDeviceCode());
        assertEquals("test_email@example.com", user.getEmail());
        assertEquals("1234", user.getPhoneNumber());
        assertEquals("http://example.com/photo.jpg", user.getProfilePhotoUrl());
        assertEquals("test", user.getUsername());
    }
}
