package com.example.syntaxeventlottery;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

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
        ActivityScenario.launch(UserProfileActivity.class);

        // Verify that the profile form fields are displayed
        onView(withId(R.id.nameTextView)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        onView(withId(R.id.emailTextView)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        onView(withId(R.id.phoneTextView)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }
}
