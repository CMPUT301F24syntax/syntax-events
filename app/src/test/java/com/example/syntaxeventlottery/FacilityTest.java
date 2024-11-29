package com.example.syntaxeventlottery;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class FacilityTest {

    // Initialize variable
    private Facility facility;

    @Before
    public void setUp() {

        // Initialize a Facility object with mock data
        facility = new Facility("Test Facility", "123 Test Location", "Organizer123");
    }

    @Test
    public void testFacilityFields() {
        // Verify initial fields of the Facility object
        assertEquals("Test Facility", facility.getName());
        assertEquals("123 Test Location", facility.getLocation());
        assertEquals("Organizer123", facility.getOrganizerId());
    }

    @Test
    public void testSetters() {
        // Update fields using setters
        facility.setLocation("456 New Location");
        facility.setOrganizer("NewOrganizer456");

        // Verify updated fields
        assertEquals("456 New Location", facility.getLocation());
        assertEquals("NewOrganizer456", facility.getOrganizerId());
    }

    @Test
    public void testNoArgumentConstructor() {
        // Test the no-argument constructor
        Facility emptyFacility = new Facility();

        // Ensure all fields are null or empty
        assertNotNull(emptyFacility);
        assertEquals(null, emptyFacility.getName());
        assertEquals(null, emptyFacility.getLocation());
        assertEquals(null, emptyFacility.getOrganizerId());
    }
}
