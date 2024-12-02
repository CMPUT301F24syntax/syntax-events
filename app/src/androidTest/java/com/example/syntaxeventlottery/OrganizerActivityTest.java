package com.example.syntaxeventlottery;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class OrganizerActivityTest {

    @Before
    public void setUp() {

        // Initialize Espresso intents
        Intents.init();
    }

    @After
    public void tearDown() {

        // Release Espresso intents
        Intents.release();
    }

    /**
     * Test that all critical UI components are displayed.
     */
    @Test
    public void testUIComponentsDisplayed() {

        // Launch Organizer Activity
        ActivityScenario.launch(OrganizerActivity.class);

        // Check that all UI components are displayed
        onView(withId(R.id.createEventButton)).check(matches(isDisplayed()));
        onView(withId(R.id.backButton)).check(matches(isDisplayed()));
        onView(withId(R.id.manageFacilityButton)).check(matches(isDisplayed()));
        onView(withId(R.id.eventRecyclerView)).check(matches(isDisplayed()));
        onView(withId(R.id.organizerEventDetailTextView)).check(matches(isDisplayed()));
    }

    /**
     * Test that clicking the create event button navigates to OrganizerCreateEvent.
     */
    @Test
    public void testCreateEventButtonNavigation() {

        // Launch Organizer Activity
        ActivityScenario.launch(OrganizerActivity.class);

        // Click on the create event button
        onView(withId(R.id.createEventButton)).perform(click());

        // Verify navigation to OrganizerCreateEvent
        intended(hasComponent(OrganizerCreateEvent.class.getName()));
    }

    /**
     * Test that clicking the back button navigates to UserHomeActivity.
     */
    @Test
    public void testBackButtonNavigation() {

        // Launch Organizer Activity and assign to scenario
        ActivityScenario<OrganizerActivity> scenario = ActivityScenario.launch(OrganizerActivity.class);

        // Create Mock user
        scenario.onActivity(activity -> {activity.currentUser = new User("mockUserId", "mock@example.com", "1234567890", "Mock User", null, null, new Facility("Mock Facility", "Mock Location", "mockOrganizerId"));});

        // Perform click on the back button
        onView(withId(R.id.backButton)).perform(click());

        // Verify navigation to UserHomeActivity
        intended(hasComponent(UserHomeActivity.class.getName()));
    }

    /**
     * Test that clicking the manage facility button navigates to ManageFacilityProfileActivity.
     */
    @Test
    public void testManageFacilityButtonNavigation() {

        // Launch Organizer Activity and assign to scenario
        ActivityScenario<OrganizerActivity> scenario = ActivityScenario.launch(OrganizerActivity.class);

        // Create Mock user
        scenario.onActivity(activity -> {activity.currentUser = new User("mockUserId", "mock@example.com", "1234567890", "Mock User", null, null, new Facility("Mock Facility", "Mock Location", "mockOrganizerId"));});

        // Perform click on the manage facility button
        onView(withId(R.id.manageFacilityButton)).perform(click());

        // Verify navigation to ManageFacilityProfileActivity
        intended(hasComponent(ManageFacilityProfileActivity.class.getName()));
    }
}
