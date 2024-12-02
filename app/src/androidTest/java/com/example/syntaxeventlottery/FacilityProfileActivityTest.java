package com.example.syntaxeventlottery;

import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertTrue;

import android.content.Intent;
import android.os.SystemClock;

import androidx.test.espresso.Espresso;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class FacilityProfileActivityTest {

    @Rule
    public ActivityTestRule<FacilityProfileActivity> activityRule = new ActivityTestRule<>(FacilityProfileActivity.class, true, false);

    /**
     * Test saving facility details with valid input.
     */
    @Test
    public void testSaveFacilityDetailsWithValidInput() {

        // Create a mock User
        User mockUser = new User();

        // Assign a test user ID to the mock user
        mockUser.setUserID("testUserID");

        // Create an Intent to launch the activity with the mock user
        Intent intent = new Intent();
        intent.putExtra("currentUser", mockUser);

        // Launch the activity with the intent
        activityRule.launchActivity(intent);

        // Wait for the activity to load, this stops error
        SystemClock.sleep(1000);

        // Enter facility name
        Espresso.onView(withId(R.id.facilityNameEditText)).perform(typeText("Test Facility Name"), closeSoftKeyboard());

        // Enter facility location
        Espresso.onView(withId(R.id.facilityLocationEditText)).perform(typeText("Test Facility Location"), closeSoftKeyboard());

        // Click save button
        Espresso.onView(withId(R.id.button_save)).perform(click());

        // Wait for processing, this stops error
        SystemClock.sleep(2000);

        // Check if the activity finishes
        assertTrue(activityRule.getActivity().isFinishing());
    }

    /**
     * Test saving facility details with empty input.
     */
    @Test
    public void testSaveFacilityDetailsWithEmptyInput() {

        // Create a mock User
        User mockUser = new User();

        // Assign a test user ID to the mock user
        mockUser.setUserID("testUserID");

        // Create an Intent to launch the activity with the mock user
        Intent intent = new Intent();
        intent.putExtra("currentUser", mockUser);

        // Launch the activity with the intent
        activityRule.launchActivity(intent);

        // Wait for the activity to load, this stops error
        SystemClock.sleep(1000);

        // Leave facility name empty
        Espresso.onView(withId(R.id.facilityNameEditText)).perform(clearText(), closeSoftKeyboard());

        // Leave facility location empty
        Espresso.onView(withId(R.id.facilityLocationEditText)).perform(clearText(), closeSoftKeyboard());

        // Click save button
        Espresso.onView(withId(R.id.button_save)).perform(click());

        // Wait for processing, this stops error
        SystemClock.sleep(1000);

        // Check that the activity is still running
        assertTrue(!activityRule.getActivity().isFinishing());
    }
}
