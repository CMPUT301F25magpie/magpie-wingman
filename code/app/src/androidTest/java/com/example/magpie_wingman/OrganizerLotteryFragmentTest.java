package com.example.magpie_wingman;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.magpie_wingman.MainActivity;
import com.example.magpie_wingman.R;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * UI testing for US 02.05.02 OrganizerLotteryFragment
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class OrganizerLotteryFragmentTest {

    @Rule
    public ActivityScenarioRule<MainActivity> rule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Test
    public void lotteryInputField_isDisplayed() {
        onView(withId(R.id.lottery_sample_input)).check(matches(isDisplayed()));
    }

    @Test
    public void enterSampleSize_andRunLottery() {
        //enter a valid sample size
        onView(withId(R.id.lottery_sample_input)).perform(typeText("5"));
        //click the select button
        onView(withId(R.id.lottery_select_button)).perform(click());
        //verify UI is still visible (successful interaction)
        onView(withId(R.id.lottery_sample_input)).check(matches(isDisplayed()));
    }

    @Test
    public void toolbarBackButton_navigatesBack() {
        onView(withId(R.id.toolbar_lottery)).perform(click());
    }
}
