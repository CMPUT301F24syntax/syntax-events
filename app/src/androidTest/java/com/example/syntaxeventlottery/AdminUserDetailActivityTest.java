package com.example.syntaxeventlottery;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;

import android.content.Intent;
import android.widget.TextView;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;

@RunWith(AndroidJUnit4.class)
public class AdminUserDetailActivityTest {

    private static final String TEST_USER_ID = "testUserId123";

    @Rule
    public ActivityScenarioRule<AdminUserDetailActivity> activityRule;

    public AdminUserDetailActivityTest() {
        // Create an intent with a test user ID
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), AdminUserDetailActivity.class);
        intent.putExtra("userID", TEST_USER_ID);
        activityRule = new ActivityScenarioRule<>(intent);
    }

    @Before
    public void setUp() {
        // Initialize Espresso intents
        Intents.init();
        MockitoAnnotations.initMocks(this);
    }

    @After
    public void tearDown() {
        // Release Espresso intents
        Intents.release();
    }

    /**
     * Test that all UI components are displayed correctly
     */
    @Test
    public void testUIComponentsDisplayed() {
        // Verify key UI components are visible
        onView(withId(R.id.backButton)).check(matches(isDisplayed()));
        onView(withId(R.id.detailUserImage)).check(matches(isDisplayed()));
        onView(withId(R.id.detailUserName)).check(matches(isDisplayed()));
        onView(withId(R.id.detailUserId)).check(matches(isDisplayed()));
        onView(withId(R.id.detailUserEmail)).check(matches(isDisplayed()));
        onView(withId(R.id.detailUserPhone)).check(matches(isDisplayed()));
    }

    /**
     * Test back button functionality
     */
    @Test
    public void testBackButtonFinishesActivity() {
        // Create an activity scenario
        ActivityScenario<AdminUserDetailActivity> scenario = activityRule.getScenario();

        // Perform click on back button
        onView(withId(R.id.backButton)).perform(click());

        // Verify the activity is finishing
        scenario.onActivity(activity -> {
            assertTrue(activity.isFinishing());
        });
    }

    /**
     * Test delete image button functionality when an image exists
     */
    @Test
    public void testDeleteImageButtonVisibility() {
        activityRule.getScenario().onActivity(activity -> {
            // Verify delete image button is present
            assertTrue(activity.findViewById(R.id.deleteImageButton) != null);
        });
    }

    /**
     * Test delete facility button functionality
     */
    @Test
    public void testDeleteFacilityButton() {
        // Verify delete facility button is displayed or handled appropriately
        activityRule.getScenario().onActivity(activity -> {
            // Check if the delete facility button exists
            assertNotNull(activity.findViewById(R.id.deleteFacilityButton));
        });
    }

    /**
     * Test user details loading
     */
    @Test
    public void testUserDetailsLoading() {
        activityRule.getScenario().onActivity(activity -> {
            // Check that text views are not empty after loading
            TextView nameTextView = activity.findViewById(R.id.detailUserName);
            TextView emailTextView = activity.findViewById(R.id.detailUserEmail);

            // Allow some time for async loading
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Verify that name and email are not empty
            assertNotNull(nameTextView.getText());
            assertTrue(nameTextView.getText().length() > 0);
            assertNotNull(emailTextView.getText());
        });
    }
}
