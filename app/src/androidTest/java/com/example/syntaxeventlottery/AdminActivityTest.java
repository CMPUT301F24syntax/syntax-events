package com.example.syntaxeventlottery;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.os.SystemClock;

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
public class AdminActivityTest {

    @Rule
    public ActivityScenarioRule<AdminActivity> activityRule = new ActivityScenarioRule<>(AdminActivity.class);

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
        onView(withId(R.id.browseEventsButton)).check(matches(isDisplayed()));
        onView(withId(R.id.browseUsersButton)).check(matches(isDisplayed()));
    }

    /**
     * Test that AdminUsersActivity is launched
     */
    @Test
    public void testbrowseUsers() {
        // Click create event button
        Espresso.onView(withId(R.id.browseUsersButton)).perform(click());

        // Wait for processing to solve synchronous error
        SystemClock.sleep(2000);

        // Verify navigation to NotificationCenterActivity
        intended(hasComponent(AdminUsersActivity.class.getName()));
    }

    /**
     * Test that AdminUsersActivity is launched
     */
    @Test
    public void testBrowseEvents() {
        // Click create event button
        Espresso.onView(withId(R.id.browseEventsButton)).perform(click());

        // Wait for processing to solve synchronous error
        SystemClock.sleep(2000);

        // Verify navigation to NotificationCenterActivity
        intended(hasComponent(AdminEventsActivity.class.getName()));
    }
}
