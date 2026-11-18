package com.davidrdz.vitallog.ui.alarm;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.davidrdz.vitallog.R;
import com.davidrdz.vitallog.alarms.Reminder;
import com.davidrdz.vitallog.alarms.ReminderIntents;
import com.davidrdz.vitallog.alarms.ReminderType;
import com.davidrdz.vitallog.data.db.AppDatabase;
import com.davidrdz.vitallog.data.entity.DoseLog;
import com.davidrdz.vitallog.data.entity.DoseUnit;
import com.davidrdz.vitallog.data.entity.Medication;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Usa la base de datos de la app (VitalLogApp crea el singleton); cada prueba trabaja solo con el
 * medicamento que inserta. connectedAndroidTest desinstala la app al terminar.
 */
@RunWith(AndroidJUnit4.class)
public class FullScreenAlarmActivityTest {

    private static final long WAIT_SECONDS = 5;

    private Context context;
    private AppDatabase database;
    private long medicationId;
    private long scheduledAt;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        database = AppDatabase.getInstance(context);
        medicationId = database.medicationDao().insert(
                new Medication(0, "Prueba alarma", 1, DoseUnit.TABLET, 8, 30, true));
        scheduledAt = System.currentTimeMillis();
    }

    @After
    public void tearDown() {
        database.medicationDao().deactivate(medicationId);
    }

    @Test
    public void medicationAlarm_showsExactlyTwoButtons() {
        try (ActivityScenario<FullScreenAlarmActivity> scenario =
                     ActivityScenario.launch(medicationIntent())) {
            onView(withId(R.id.txt_alarm_name)).check(matches(withText("Prueba alarma")));
            scenario.onActivity(activity ->
                    assertEquals(2, countVisibleButtons(activity.getWindow().getDecorView())));
            onView(withId(R.id.btn_alarm_confirm)).check(matches(withText(R.string.alarm_btn_taken)));
            onView(withId(R.id.btn_alarm_snooze))
                    .check(matches(withText(R.string.reminder_action_snooze)));
        }
    }

    @Test
    public void takenButton_createsDoseLog() throws InterruptedException {
        try (ActivityScenario<FullScreenAlarmActivity> ignored =
                     ActivityScenario.launch(medicationIntent())) {
            onView(withId(R.id.txt_alarm_name)).check(matches(isDisplayed()));
            // Los botones están fijos al pie, fuera del ScrollView: siempre visibles sin desplazar.
            onView(withId(R.id.btn_alarm_confirm)).check(matches(isDisplayed())).perform(click());

            final DoseLog log = awaitDoseLog();
            assertEquals(medicationId, log.getMedicationId());
            assertEquals(scheduledAt, log.getScheduledAt());
        }
    }

    private Intent medicationIntent() {
        return ReminderIntents.put(new Intent(context, FullScreenAlarmActivity.class),
                new Reminder(ReminderType.MEDICATION, medicationId, scheduledAt));
    }

    private static int countVisibleButtons(final View view) {
        if (view.getVisibility() != View.VISIBLE) {
            return 0;
        }
        if (view instanceof Button) {
            return 1;
        }
        int count = 0;
        if (view instanceof ViewGroup) {
            final ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                count += countVisibleButtons(group.getChildAt(i));
            }
        }
        return count;
    }

    /** Espera a que Room emita la toma de este medicamento (se guarda en segundo plano). */
    private DoseLog awaitDoseLog() throws InterruptedException {
        final LiveData<List<DoseLog>> logs = database.doseLogDao()
                .observeTakenBetween(scheduledAt, Long.MAX_VALUE);
        final List<DoseLog> found = new ArrayList<>();
        final CountDownLatch latch = new CountDownLatch(1);
        final Observer<List<DoseLog>> observer = list -> {
            for (final DoseLog log : list) {
                if (log.getMedicationId() == medicationId && found.isEmpty()) {
                    found.add(log);
                    latch.countDown();
                }
            }
        };
        InstrumentationRegistry.getInstrumentation()
                .runOnMainSync(() -> logs.observeForever(observer));
        final boolean saved = latch.await(WAIT_SECONDS, TimeUnit.SECONDS);
        InstrumentationRegistry.getInstrumentation()
                .runOnMainSync(() -> logs.removeObserver(observer));
        if (!saved) {
            fail("No se registró la toma");
        }
        return found.get(0);
    }
}
