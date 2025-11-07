package com.example.magpie_wingman;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.action.ViewActions.click;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.magpie_wingman.MainActivity;
import com.example.magpie_wingman.R;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * UI testing for US 02.02.01 WaitingListFragment
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class WaitingListFragmentTest {

    @Rule
    public ActivityScenarioRule<MainActivity> rule = new ActivityScenarioRule<>(MainActivity.class);

    @Test
    public void recyclerView_isVisibleOnLaunch() {
        onView(withId(R.id.recycler_waitlist)).check(matches(isDisplayed()));
    }

    @Test
    public void progressBar_isVisibleInitially() {
        onView(withId(R.id.waitlist_progress)).check(matches(isDisplayed()));
    }
}

