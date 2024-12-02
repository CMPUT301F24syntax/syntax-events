package com.example.syntaxeventlottery;

import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertTrue;

import android.os.SystemClock;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class OrganizerCreateEventTest {

    @Rule
    public ActivityTestRule<OrganizerCreateEvent> activityRule = new ActivityTestRule<>(OrganizerCreateEvent.class);


    /**
     * Test creating an event with invalid date input.
     */
    @Test
    public void testCreateEventWithInvalidDate() {

        // Type event name
        Espresso.onView(withId(R.id.eventNameEditText)).perform(typeText("Sample Event"), ViewActions.closeSoftKeyboard());

        // Type event description
        Espresso.onView(withId(R.id.eventDescriptionEditText)).perform(typeText("This is a sample event."), ViewActions.closeSoftKeyboard());

        // Type invalid start date
        Espresso.onView(withId(R.id.eventStartDateEditText)).perform(typeText("invalid date"), ViewActions.closeSoftKeyboard());

        // Type end date
        Espresso.onView(withId(R.id.eventEndDateEditText)).perform(typeText("2024-12-01 12:00"), ViewActions.closeSoftKeyboard());

        // Type capacity
        Espresso.onView(withId(R.id.capacityEditText)).perform(typeText("100"), ViewActions.closeSoftKeyboard());

        // Click create event button
        Espresso.onView(withId(R.id.createEventButton)).perform(click());

        // Wait for processing to solve synchronous error
        SystemClock.sleep(2000);

        // Check that the activity is still running
        assertTrue(!activityRule.getActivity().isFinishing());
    }

    /**
     * Test creating an event with waiting list limit enabled and valid input.
     */
    @Test
    public void testCreateEventWithWaitingListLimit() {

        // Type event name
        Espresso.onView(withId(R.id.eventNameEditText)).perform(typeText("Event with Waiting List"), ViewActions.closeSoftKeyboard());

        // Type event description
        Espresso.onView(withId(R.id.eventDescriptionEditText)).perform(typeText("Event with waiting list enabled."), ViewActions.closeSoftKeyboard());

        // Type start date
        Espresso.onView(withId(R.id.eventStartDateEditText)).perform(typeText("2020-12-05 09:00"), ViewActions.closeSoftKeyboard());

        // Type end date
        Espresso.onView(withId(R.id.eventEndDateEditText)).perform(typeText("2020-12-05 11:00"), ViewActions.closeSoftKeyboard());

        // Type capacity
        Espresso.onView(withId(R.id.capacityEditText)).perform(typeText("50"), ViewActions.closeSoftKeyboard());

        // Enable waiting list limit switch
        Espresso.onView(withId(R.id.waitingListLimitSwitch)).perform(ViewActions.scrollTo(), click());

        // Type waiting list limit
        Espresso.onView(withId(R.id.waitingListLimitEditText)).perform(typeText("75"), ViewActions.closeSoftKeyboard());

        // Click create event button
        Espresso.onView(withId(R.id.createEventButton)).perform(click());

        // Wait for processing to solve synchronous error
        SystemClock.sleep(2000);

        // Check if the activity finishes
        assertTrue(activityRule.getActivity().isFinishing());
    }

    /**
     * Test creating an event with invalid waiting list limit.
     */
    @Test
    public void testCreateEventWithInvalidWaitingListLimit() {

        // Type event name
        Espresso.onView(withId(R.id.eventNameEditText)).perform(typeText("Event with Invalid Waiting List"), ViewActions.closeSoftKeyboard());

        // Type event description
        Espresso.onView(withId(R.id.eventDescriptionEditText)).perform(typeText("Event with invalid waiting list limit."), ViewActions.closeSoftKeyboard());

        // Type start date
        Espresso.onView(withId(R.id.eventStartDateEditText)).perform(typeText("2024-12-10 14:00"), ViewActions.closeSoftKeyboard());

        // Type end date
        Espresso.onView(withId(R.id.eventEndDateEditText)).perform(typeText("2024-12-10 16:00"), ViewActions.closeSoftKeyboard());

        // Type capacity
        Espresso.onView(withId(R.id.capacityEditText)).perform(typeText("50"), ViewActions.closeSoftKeyboard());

        // Enable waiting list limit switch
        Espresso.onView(withId(R.id.waitingListLimitSwitch)).perform(ViewActions.scrollTo(), click());

        // Type invalid waiting list limit (less than capacity)
        Espresso.onView(withId(R.id.waitingListLimitEditText)).perform(typeText("30"), ViewActions.closeSoftKeyboard());

        // Click create event button
        Espresso.onView(withId(R.id.createEventButton)).perform(click());

        // Wait for processing to solve synchronous error
        SystemClock.sleep(2000);

        // Check that the activity is still running
        assertTrue(!activityRule.getActivity().isFinishing());
    }
}