package com.example.syntaxeventlottery;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class UserProfileActivityTest {

    @Rule
    public ActivityScenarioRule<UserProfileActivity> activityRule = new ActivityScenarioRule<>(UserProfileActivity.class);

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
        onView(withId(R.id.profileImageView)).check(matches(isDisplayed()));
        onView(withId(R.id.nameTextView)).check(matches(isDisplayed()));
        onView(withId(R.id.emailTextView)).check(matches(isDisplayed()));
        onView(withId(R.id.phoneTextView)).check(matches(isDisplayed()));
        onView(withId(R.id.backButton)).check(matches(isDisplayed()));
        onView(withId(R.id.editButton)).check(matches(isDisplayed()));
    }

    /**
     * Test that clicking the back button closes the activity.
     */
    @Test
    public void testBackButtonFunctionality() {

        // Perform back button click
        onView(withId(R.id.backButton)).perform(click());

        // Verify that the activity ends after clicking the back button
        assert(activityRule.getScenario().getState().isAtLeast(androidx.lifecycle.Lifecycle.State.DESTROYED));
    }

    /**
     * Test that the user profile data is displayed correctly.
     */
    @Test
    public void testDisplayUserProfileData() {

        // Mock user data
        User mockUser = new User("12345", "test@example.com", "1234567890", null, "Test User", null, null);

        // Access the running activity instance via the ActivityScenario
        activityRule.getScenario().onActivity(activity -> {

            // Pass the mockUser object to simulate updating the user interface
            activity.displayUserDetails(mockUser);
        });

        // Verify that the displayed data matches the mock user data
        onView(withId(R.id.nameTextView)).check(matches(withText("Test User")));
        onView(withId(R.id.emailTextView)).check(matches(withText("test@example.com")));
        onView(withId(R.id.phoneTextView)).check(matches(withText("1234567890")));
    }

    /**
     * Test that the profile image placeholder is displayed.
     */
    @Test
    public void testProfileImagePlaceholder() {

        // Verify that the profile image placeholder is displayed by default
        onView(withId(R.id.profileImageView)).check(matches(isDisplayed()));
    }
}
