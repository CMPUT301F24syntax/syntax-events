package com.example.syntaxeventlottery;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class UserTests {

    @Test
    public void testProfileCreationForm() {

        // Launch the UserProfileActivity
        ActivityScenario<UserProfileActivity> scenario = ActivityScenario.launch(UserProfileActivity.class);

        // Verify that the profile form fields are displayed
        onView(withId(R.id.nameTextView)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        onView(withId(R.id.emailTextView)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        onView(withId(R.id.phoneTextView)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        // Close the scenario to clean up the activity
        scenario.close();
    }

    @Test
    public void testDisplayedUserData() {

        // Launch the UserProfileActivity
        ActivityScenario<UserProfileActivity> scenario = ActivityScenario.launch(UserProfileActivity.class);

        // Mock user data
        String expectedName = "User Name";
        String expectedEmail = "User email";
        String expectedPhone = "User telephone number";

        // Verify that the displayed data matches the expected values
        onView(withId(R.id.nameTextView)).check(matches(withText(expectedName)));
        onView(withId(R.id.emailTextView)).check(matches(withText(expectedEmail)));
        onView(withId(R.id.phoneTextView)).check(matches(withText(expectedPhone)));

        // Close the scenario to clean up the activity
        scenario.close();
    }
}
