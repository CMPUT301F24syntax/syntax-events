package com.example.syntaxeventlottery;


import static org.junit.Assert.assertNotNull;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

    @Rule
    public ActivityTestRule<MainActivity> activityRule = new ActivityTestRule<>(MainActivity.class);

    /**
     * Test that MainActivity launches successfully.
     */
    @Test
    public void testMainActivityLaunches() {

        // Get the activity instance
        MainActivity activity = activityRule.getActivity();

        // Assert that the activity is not null
        assertNotNull("MainActivity is null", activity);
    }
}
