package com.example.syntaxeventlottery;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;

@RunWith(AndroidJUnit4.class)
public class EditEventActivityTest {

    @Before
    public void setUp() {

        // Initialize Intents
        Intents.init();
    }

    @After
    public void tearDown() {
        // Release Intents
        Intents.release();
    }

    /**
     * Test that all UI components are displayed correctly when the activity launches.
     */
    @Test
    public void testUIComponentsDisplayed() {

        // Create mock Event object using helper
        Event mockEvent = createMockEvent();

        // Create an Intent for EditEventActivity
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), EditEventActivity.class);
        intent.putExtra("event", mockEvent);

        // Launch the activity with the explicit Intent
        ActivityScenario<EditEventActivity> scenario = ActivityScenario.launch(intent);

        // Verify that all UI components are displayed
        onView(withId(R.id.backButton)).check(matches(isDisplayed()));
        onView(withId(R.id.updatePosterButton)).check(matches(isDisplayed()));
        onView(withId(R.id.resetPosterButton)).check(matches(isDisplayed()));
        onView(withId(R.id.updatePosterView)).check(matches(isDisplayed()));
        onView(withId(R.id.editEventName)).check(matches(isDisplayed()));
        onView(withId(R.id.editEventDescription)).check(matches(isDisplayed()));
        onView(withId(R.id.editStartDate)).check(matches(isDisplayed()));
        onView(withId(R.id.editEndDate)).check(matches(isDisplayed()));
        onView(withId(R.id.editCapacity)).check(matches(isDisplayed()));
        onView(withId(R.id.locationSwitch)).check(matches(isDisplayed()));
        onView(withId(R.id.saveEventButton)).check(matches(isDisplayed()));
    }

    /**
     * Helper method to create a mock Event object.
     */
    private Event createMockEvent() {

        // Initialize a mock event
        Event mockEvent = new Event();
        mockEvent.setEventID("event123");
        mockEvent.setEventName("Initial Event Name");
        mockEvent.setDescription("Initial Event Description");
        mockEvent.setStartDate(new Date(124, 4, 1, 9, 0));
        mockEvent.setEndDate(new Date(124, 4, 1, 17, 0));
        mockEvent.setCapacity(100);
        mockEvent.setLocationRequired(false);
        mockEvent.setPosterUrl(null);

        // Return the mock event
        return mockEvent;
    }
}