package com.hmproductions.theredstreet;

import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.hmproductions.theredstreet.ui.MainActivity;
import com.hmproductions.theredstreet.ui.LoginActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

@RunWith(AndroidJUnit4.class)
public class LoginBasicTest {

    @Rule
    public IntentsTestRule<LoginActivity> intentTestRule = new IntentsTestRule<>(LoginActivity.class);

    @Test
    public void clickFAB_opensQuizActivity() {

        onView(withId(R.id.email_editText)).perform(typeText("harshmahajan20@yahoo.in"));
        onView(withId(R.id.password_editText)).perform(typeText("deltaforce"));

        // Checking if button is displayed and performing click
        onView(withId(R.id.play_button)).perform(click());

        // Checking if activity has opened
        intended(hasComponent(MainActivity.class.getName()));
    }
}
