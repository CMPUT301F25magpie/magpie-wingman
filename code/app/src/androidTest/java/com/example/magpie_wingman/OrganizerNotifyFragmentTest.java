package com.example.magpie_wingman;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.magpie_wingman.MainActivity;
import com.example.magpie_wingman.R;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * UI tests for US 02.05.01 & US 02.07.01 & US 02.07.02 & US 02.07.03 OrganizerNotifyFragment
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class OrganizerNotifyFragmentTest {

    @Rule
    public ActivityScenarioRule<MainActivity> rule = new ActivityScenarioRule<>(MainActivity.class);

    @Test
    public void notifyUI_elementsAreVisible() {
        onView(withId(R.id.notify_title_input)).check(matches(isDisplayed()));
        onView(withId(R.id.notify_message_input)).check(matches(isDisplayed()));
        onView(withId(R.id.notify_send_button)).check(matches(isDisplayed()));
    }

    @Test
    public void sendNotification_withValidInputs() {
        //type title + msg
        onView(withId(R.id.notify_title_input)).perform(typeText("Lottery Result"));
        onView(withId(R.id.notify_message_input)).perform(typeText("You have been selected!"));
        //select a recipient group
        onView(withId(R.id.checkbox_selected)).perform(click());
        //click the send button
        onView(withId(R.id.notify_send_button)).perform(click());
    }

    @Test
    public void toolbarBackButton_navigatesBack() {
        onView(withId(R.id.toolbar_notify)).perform(click());
    }
}
