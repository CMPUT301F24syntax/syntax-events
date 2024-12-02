package com.example.syntaxeventlottery;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.Intents.intending;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.app.Activity;
import android.app.Instrumentation;

import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class UserHomeActivityTest {

    @Rule
    public ActivityScenarioRule<UserHomeActivity> activityRule = new ActivityScenarioRule<>(UserHomeActivity.class);

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

        // Verify buttons
        onView(withId(R.id.organizerButton)).check(matches(isDisplayed()));
        onView(withId(R.id.profileButton)).check(matches(isDisplayed()));
        onView(withId(R.id.newsButton)).check(matches(isDisplayed()));
        onView(withId(R.id.qrScanButton)).check(matches(isDisplayed()));

        // Verify the RecyclerViews
        onView(withId(R.id.waitlistedEventsRecyclerView)).check(matches(isDisplayed()));
        onView(withId(R.id.selectedEventsRecyclerView)).check(matches(isDisplayed()));
        onView(withId(R.id.enrolledEventsRecyclerView)).check(matches(isDisplayed()));
    }

    /**
     * Test that clicking the profile button navigates to UserProfileActivity.
     */
    @Test
    public void testProfileButtonNavigation() {

        // Fake launch Userprofileactivity and force it to return an ok result
        intending(hasComponent(UserProfileActivity.class.getName())).respondWith(new Instrumentation.ActivityResult(Activity.RESULT_OK, null));

        // Perform the button click
        onView(withId(R.id.profileButton)).perform(click());

        // Verify that an intent was sent to open UserProfileActivity.
        intended(hasComponent(UserProfileActivity.class.getName()));
    }

    /**
     * Test that clicking the news button navigates to NotificationCenterActivity.
     */
    @Test
    public void testNewsButtonNavigation() {

        // Perform the button click
        onView(withId(R.id.newsButton)).perform(click());

        // Verify navigation to NotificationCenterActivity
        intended(hasComponent(NotificationCenterActivity.class.getName()));
    }

    /**
     * Test that clicking the scan button navigates to QRScanActivity.
     */
    @Test
    public void testScanButtonNavigation() {

        // Perform the button click
        onView(withId(R.id.qrScanButton)).perform(click());

        // Verify navigation to QRScanActivity
        intended(hasComponent(QRScanActivity.class.getName()));
    }

    /**
     * Verify RecyclerView initialization for waitlisted events.
     */
    @Test
    public void testWaitlistedRecyclerViewInitialization() {

        // Check if the RecyclerView for waitlisted events is displayed
        onView(withId(R.id.waitlistedEventsRecyclerView)).check(matches(isDisplayed()));
    }

    /**
     * Verify RecyclerView initialization for selected events.
     */
    @Test
    public void testSelectedRecyclerViewInitialization() {

        // Check if the RecyclerView for selected events is displayed
        onView(withId(R.id.selectedEventsRecyclerView)).check(matches(isDisplayed()));
    }

    /**
     * Verify RecyclerView initialization for enrolled events.
     */
    @Test
    public void testEnrolledRecyclerViewInitialization() {

        // Check if the RecyclerView for enrolled events is displayed
        onView(withId(R.id.enrolledEventsRecyclerView)).check(matches(isDisplayed()));
    }
}