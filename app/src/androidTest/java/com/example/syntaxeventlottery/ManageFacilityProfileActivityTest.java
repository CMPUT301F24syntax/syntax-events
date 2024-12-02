package com.example.syntaxeventlottery;

import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertTrue;

import android.os.SystemClock;

import androidx.test.espresso.Espresso;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ManageFacilityProfileActivityTest {

    @Rule
    public ActivityTestRule<ManageFacilityProfileActivity> activityRule = new ActivityTestRule<>(ManageFacilityProfileActivity.class);

    /**
     * Test updating facility details with valid input.
     */
    @Test
    public void testUpdateFacilityDetailsWithValidInput() {

        // Wait for the activity to load and buttons, this stops error
        SystemClock.sleep(2000);

        // Type facility name
        Espresso.onView(withId(R.id.editFacilityNameEditText)).perform(replaceText("New Facility Name"), closeSoftKeyboard());

        // Type facility location
        Espresso.onView(withId(R.id.editFacilityLocationEditText)).perform(replaceText("New Facility Location"), closeSoftKeyboard());

        // Click save button
        Espresso.onView(withId(R.id.save_Button)).perform(click());

        // Wait for processing, this stops error
        SystemClock.sleep(2000);

        // Check if the activity finishes
        assertTrue(activityRule.getActivity().isFinishing());
    }

    /**
     * Test updating facility details with empty input fields.
     */
    @Test
    public void testUpdateFacilityDetailsWithEmptyInput() {

        // Wait for the activity to load buttons, this stops error
        SystemClock.sleep(2000);

        // Clear facility name
        Espresso.onView(withId(R.id.editFacilityNameEditText)).perform(clearText(), closeSoftKeyboard());

        // Clear facility location
        Espresso.onView(withId(R.id.editFacilityLocationEditText)).perform(clearText(), closeSoftKeyboard());

        // Click save button
        Espresso.onView(withId(R.id.save_Button)).perform(click());

        // Wait for processing, this stops error
        SystemClock.sleep(1000);

        // Check that the activity is still running
        assertTrue(!activityRule.getActivity().isFinishing());
    }
}
