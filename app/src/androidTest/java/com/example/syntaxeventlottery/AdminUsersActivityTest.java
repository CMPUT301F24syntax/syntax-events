package com.example.syntaxeventlottery;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.os.SystemClock;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class AdminUsersActivityTest {
    @Rule
    public ActivityScenarioRule<AdminUsersActivity> activityRule = new ActivityScenarioRule<>(AdminUsersActivity.class);

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
     * Test that UI components are displayed
     */
    @Test
    public void testUIComponentsDisplayed() {
        // Verify all UI components are visible
        onView(withId(R.id.listViewUsers)).check(matches(isDisplayed()));
        onView(withId(R.id.backButton)).check(matches(isDisplayed()));

    }

    /**
     * Test the back button functionality
     */
    @Test
    public void testBackButtonFinishesActivity() {
        // Create an activity scenario
        ActivityScenario<AdminUsersActivity> scenario = ActivityScenario.launch(AdminUsersActivity.class);

        // Perform click on back button
        onView(withId(R.id.backButton)).perform(click());

        // Verify the activity is finished
        scenario.onActivity(activity -> {
            assertTrue(activity.isFinishing());
        });
    }

    /**
     * Test user list population
     */
    @Test
    public void testUserListPopulation() {
        // Create the activity scenario
        ActivityScenario<AdminUsersActivity> scenario = ActivityScenario.launch(AdminUsersActivity.class);

        scenario.onActivity(activity -> {
            // Verify that the user list is not null
            assertNotNull(activity.findViewById(R.id.listViewUsers));

        });
    }
}
