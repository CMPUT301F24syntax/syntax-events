package com.example.syntaxeventlottery;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.os.SystemClock;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class EditUserProfileActivityTest {

    @Rule
    public ActivityScenarioRule<EditUserProfileActivity> activityRule =
            new ActivityScenarioRule<>(EditUserProfileActivity.class);

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

    @Test
    public void testUIComponentsDisplayed() {
        // Verify key UI components are visible
        onView(withId(R.id.editName)).check(matches(isDisplayed()));
        onView(withId(R.id.editEmail)).check(matches(isDisplayed()));
        onView(withId(R.id.editPhone)).check(matches(isDisplayed()));
        onView(withId(R.id.saveButton)).check(matches(isDisplayed()));
        onView(withId(R.id.backButton)).check(matches(isDisplayed()));
        onView(withId(R.id.profileImageView)).check(matches(isDisplayed()));
        onView(withId(R.id.notificationSwitch)).check(matches(isDisplayed()));
    }

    /**
     * Test the back button functionality
     */
    @Test
    public void testBackButtonFinishesActivity() {

        // Perform click on back button
        onView(withId(R.id.backButton)).perform(click());

        // Verify the activity is finished
        activityRule.getScenario().onActivity(activity -> {
            assertTrue(activity.isFinishing());
        });
    }

    /**
     * Test the save button functionality
     */
    @Test
    public void testSaveButtonFinishesActivity() {
        // Create an activity scenario
        //ActivityScenario<EditUserProfileActivity> scenario = ActivityScenario.launch(EditUserProfileActivity.class);

        // Perform click on back button
        onView(withId(R.id.saveButton)).perform(click());

        // Verify the activity is finished
        activityRule.getScenario().onActivity(activity -> {
            assertFalse(activity.isFinishing());
        });
    }
}
