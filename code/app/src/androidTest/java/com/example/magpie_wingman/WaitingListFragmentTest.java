package com.example.magpie_wingman;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.magpie_wingman.R;
import com.example.magpie_wingman.organizer.OrganizerEventDetailsFragment;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * intent test for US 02.02.01
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class WaitingListFragmentTest {

    // Replace this with whatever Activity launches the organizer flow
    @Rule
    public ActivityScenarioRule<MainActivity> rule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Test
    public void waitingListRecycler_isVisible() {
        //verify the RecyclerView is displayed when the fragment loads
        onView(withId(R.id.recycler_waitlist)).check(matches(isDisplayed()));
    }

    @Test
    public void toolbarBackButton_navigatesUp() {
        // Click the toolbar to simulate a back/up action
        onView(withId(R.id.toolbar_waitlist)).perform(click());
        // Optional confirmation (depends on what text appears after navigating)
        onView(withText("Event Details")).check(matches(isDisplayed()));
    }
}
