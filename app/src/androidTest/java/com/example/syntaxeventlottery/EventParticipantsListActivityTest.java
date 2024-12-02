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
public class EventParticipantsListActivityTest {

    @Rule
    public ActivityTestRule<EventParticipantsListActivity> activityRule = new ActivityTestRule<>(EventParticipantsListActivity.class, true, false);

    /**
     * Test that EventParticipantsListActivity launches without an event ID
     */
    @Test
    public void testActivityLaunchWithoutEventId() {

        // Create new Intent
        Intent intent = new Intent();

        // Launch the activity with the intent
        activityRule.launchActivity(intent);

        // Wait for the activity to process, this prevents error
        SystemClock.sleep(1000);

        // Check if activity is finished
        assertTrue(activityRule.getActivity().isFinishing());
    }

    /**
     * Test that EventParticipantsListActivity launches successfully with a null event ID
     */
    @Test
    public void testActivityHandlesNullEventId() {

        // Create new Intent
        Intent intent = new Intent();

        // Set event ID to null
        intent.putExtra("eventID", (String) null);

        // Launch the activity with the intent
        activityRule.launchActivity(intent);

        // Wait for the activity to process, this prevents error
        SystemClock.sleep(1000);

        // Check if activity is finished
        assertTrue(activityRule.getActivity().isFinishing());
    }
}