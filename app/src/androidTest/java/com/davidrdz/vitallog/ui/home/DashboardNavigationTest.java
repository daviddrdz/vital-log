package com.davidrdz.vitallog.ui.home;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertTrue;

import android.util.TypedValue;
import android.view.View;

import androidx.annotation.IdRes;
import androidx.annotation.StringRes;
import androidx.test.espresso.ViewAssertion;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.davidrdz.vitallog.R;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class DashboardNavigationTest {

    private static final float MIN_TOUCH_TARGET_DP = 64f;

    @Rule
    public final ActivityScenarioRule<MainActivity> rule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Test
    public void actionButtons_areVisibleAndLargeEnough() {
        for (final int id : new int[]{
                R.id.btn_register_glucose, R.id.btn_register_pressure, R.id.btn_agenda,
                R.id.btn_history}) {
            onView(withId(id)).perform(scrollTo()).check(matches(isDisplayed()));
            onView(withId(id)).check(hasMinimumTouchSize());
        }
    }

    @Test
    public void glucoseButton_opensGlucoseScreen_andBackReturns() {
        assertOpensAndReturns(R.id.btn_register_glucose, R.string.title_glucose_entry);
    }

    @Test
    public void pressureButton_opensPressureScreen_andBackReturns() {
        assertOpensAndReturns(R.id.btn_register_pressure, R.string.title_pressure_entry);
    }

    @Test
    public void agendaButton_opensAgendaScreen_andBackReturns() {
        assertOpensAndReturns(R.id.btn_agenda, R.string.title_agenda);
    }

    @Test
    public void historyButton_opensHistoryScreen_andBackReturns() {
        assertOpensAndReturns(R.id.btn_history, R.string.title_history);
    }

    private void assertOpensAndReturns(@IdRes final int buttonId, @StringRes final int title) {
        onView(withId(buttonId)).perform(scrollTo(), click());
        onView(withId(R.id.txt_screen_title)).check(matches(withText(title)));
        onView(withId(R.id.btn_back)).check(hasMinimumTouchSize()).perform(click());
        onView(withId(R.id.txt_dashboard_title)).check(matches(isDisplayed()));
    }

    private static ViewAssertion hasMinimumTouchSize() {
        return (view, noViewFoundException) -> {
            if (noViewFoundException != null) {
                throw noViewFoundException;
            }
            final float minPx = dpToPx(view, MIN_TOUCH_TARGET_DP);
            assertTrue("Alto < 64dp", view.getHeight() >= minPx);
            assertTrue("Ancho < 64dp", view.getWidth() >= minPx);
        };
    }

    private static float dpToPx(final View view, final float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                view.getResources().getDisplayMetrics());
    }
}
