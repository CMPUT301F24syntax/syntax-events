package com.example.syntaxeventlottery;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class AdminEventDetailActivityTest {

    @Rule
    public ActivityScenarioRule<AdminEventDetailActivity> activityRule =
            new ActivityScenarioRule<>(AdminEventDetailActivity.class);

    @Test
    public void testUIComponentsDisplayed() {
        // Verify key UI components are visible
        onView(withId(R.id.eventName)).check(matches(isDisplayed()));
        onView(withId(R.id.eventDescription)).check(matches(isDisplayed()));
        onView(withId(R.id.eventCapacity)).check(matches(isDisplayed()));
    }
}
