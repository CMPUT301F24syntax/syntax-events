package com.example.syntaxeventlottery;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class AdminEventsActivityTest {

    @Rule
    public ActivityScenarioRule<AdminEventsActivity> activityRule =
            new ActivityScenarioRule<>(AdminEventsActivity.class);

    @Test
    public void testUIComponentsDisplayed() {
        // Verify key UI components are visible
        onView(withId(R.id.listViewEvents)).check(matches(isDisplayed()));
        onView(withId(R.id.backButton)).check(matches(isDisplayed()));
    }
}
