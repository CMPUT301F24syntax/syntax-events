package com.example.syntaxeventlottery;

import static org.junit.Assert.assertTrue;

import android.content.Intent;
import android.os.SystemClock;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class EventDetailActivityTest {

    @Rule
    public ActivityTestRule<EventDetailActivity> activityRule = new ActivityTestRule<>(EventDetailActivity.class, true, false);

    /**
     * Test that EventDetailActivity launches without an event ID
     */
    @Test
    public void testActivityLaunchWithoutEventId() {

        // Create new Intent
        Intent intent = new Intent();

        // Launch the activity with the intent
        activityRule.launchActivity(intent);

        // Wait for the activity to process, this prevents error
        SystemClock.sleep(1000);

        // Check if activity is finishing
        assertTrue("Activity should finish when eventID is missing", activityRule.getActivity().isFinishing());
    }

    /**
     * Test that EventDetailActivity launches successfully with a null event ID
     */
    @Test
    public void testActivityLaunchWithNullEventId() {

        // Create a new Intent with a null "eventID" extra explicitly
        Intent intent = new Intent();

        // Set event ID to null
        intent.putExtra("eventID", (String) null);

        // Launch the activity with the intent
        activityRule.launchActivity(intent);

        // Wait for the activity to process, this prevents error
        SystemClock.sleep(1000);

        // Check if activity is finishing
        assertTrue("Activity should finish when eventID is null", activityRule.getActivity().isFinishing());
    }
}
