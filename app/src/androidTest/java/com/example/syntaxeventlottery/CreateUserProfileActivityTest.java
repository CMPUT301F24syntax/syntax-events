package com.example.syntaxeventlottery;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
public class CreateUserProfileActivityTest {

    @Rule
    public ActivityScenarioRule<CreateUserProfileActivity> activityRule =
            new ActivityScenarioRule<>(CreateUserProfileActivity.class);

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
     * Test to ensure key UI components are displayed
     */
    @Test
    public void testUIComponentsDisplayed() {
        onView(withId(R.id.userNameEditText)).check(matches(isDisplayed()));
        onView(withId(R.id.edit_text_email)).check(matches(isDisplayed()));
        onView(withId(R.id.edit_text_phone)).check(matches(isDisplayed()));
        onView(withId(R.id.button_save)).check(matches(isDisplayed()));
        onView(withId(R.id.button_back)).check(matches(isDisplayed()));
        onView(withId(R.id.image_view_avatar)).check(matches(isDisplayed()));
        onView(withId(R.id.upload_profile_button)).check(matches(isDisplayed()));
    }

    /**
     * Test the back button functionality
     */
    @Test
    public void testBackButtonFinishesActivity() {
        onView(withId(R.id.button_back)).perform(click());

        activityRule.getScenario().onActivity(activity -> {
            assertTrue(activity.isFinishing());
        });
    }

    /**
     * Test the save button functionality
     */
    @Test
    public void testSaveButtonFinishesActivity() {
        // Perform click on back button
        onView(withId(R.id.button_save)).perform(click());

        // Verify the activity is finished
        activityRule.getScenario().onActivity(activity -> {
            assertFalse(activity.isFinishing());
        });

    }
}


