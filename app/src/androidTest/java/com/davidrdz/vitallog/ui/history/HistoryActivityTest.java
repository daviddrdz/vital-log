package com.davidrdz.vitallog.ui.history;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isChecked;
import static androidx.test.espresso.matcher.ViewMatchers.isNotChecked;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.allOf;

import android.content.Context;
import android.os.SystemClock;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.NoMatchingViewException;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.davidrdz.vitallog.R;
import com.davidrdz.vitallog.data.db.AppDatabase;
import com.davidrdz.vitallog.data.entity.BloodPressureReading;
import com.davidrdz.vitallog.data.entity.DoseLog;
import com.davidrdz.vitallog.data.entity.DoseUnit;
import com.davidrdz.vitallog.data.entity.GlucoseReading;
import com.davidrdz.vitallog.data.entity.Medication;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Usa la base de datos de la app (VitalLogApp crea el singleton), como FullScreenAlarmActivityTest.
 * Cada prueba busca solo la fila con los valores que inserta (valores poco comunes para no chocar
 * con otros datos). connectedAndroidTest desinstala la app al terminar.
 */
@RunWith(AndroidJUnit4.class)
public class HistoryActivityTest {

    /** Las consultas de Room responden en otro hilo: se reintenta la comprobación hasta 5 s. */
    private static final long WAIT_MS = 5000;
    private static final long POLL_MS = 100;

    private AppDatabase database;
    private long medicationId;

    @Before
    public void setUp() {
        final Context context = ApplicationProvider.getApplicationContext();
        database = AppDatabase.getInstance(context);
        medicationId = 0;
    }

    @After
    public void tearDown() {
        if (medicationId != 0) {
            database.medicationDao().deactivate(medicationId);
        }
    }

    @Test
    public void glucoseRow_showsValueUnitAndCategoryInText() {
        database.glucoseReadingDao().insert(new GlucoseReading(0, 137, now()));
        try (ActivityScenario<HistoryActivity> ignored = ActivityScenario.launch(
                HistoryActivity.class)) {
            assertRowShows("137 mg/dL", R.string.history_type_glucose,
                    R.string.category_glucose_normal);
        }
    }

    @Test
    public void pressureRow_showsValueUnitAndCategoryInText() {
        database.bloodPressureReadingDao().insert(new BloodPressureReading(0, 151, 93, now()));
        try (ActivityScenario<HistoryActivity> ignored = ActivityScenario.launch(
                HistoryActivity.class)) {
            assertRowShows("151/93 mmHg", R.string.history_type_pressure,
                    R.string.category_pressure_high);
        }
    }

    @Test
    public void doseRow_showsMedicationAndDose() {
        medicationId = database.medicationDao().insert(
                new Medication(0, "Prueba historial", 2, DoseUnit.TABLET, 8, 30, true));
        database.doseLogDao().insert(new DoseLog(0, medicationId, now(), now()));
        try (ActivityScenario<HistoryActivity> ignored = ActivityScenario.launch(
                HistoryActivity.class)) {
            waitUntil(() -> onView(withId(R.id.list_history)).check(matches(
                    hasDescendant(withText("Prueba historial: 2 tabletas")))));
            onView(allOf(withId(R.id.card_history_entry),
                    hasDescendant(withText("Prueba historial: 2 tabletas"))))
                    .check(matches(hasDescendant(withText(R.string.history_type_dose))));
        }
    }

    @Test
    public void periodButtons_startOnSevenDays_andSwitchToThirty() {
        try (ActivityScenario<HistoryActivity> ignored = ActivityScenario.launch(
                HistoryActivity.class)) {
            onView(withId(R.id.btn_period_7)).check(matches(isChecked()));
            onView(withId(R.id.txt_summary_title))
                    .check(matches(withText(R.string.history_summary_title_7)));
            onView(withId(R.id.btn_period_30)).perform(scrollTo(), click());
            onView(withId(R.id.btn_period_30)).check(matches(isChecked()));
            onView(withId(R.id.btn_period_7)).check(matches(isNotChecked()));
            waitUntil(() -> onView(withId(R.id.txt_summary_title))
                    .check(matches(withText(R.string.history_summary_title_30))));
        }
    }

    /** La tarjeta que contiene {@code value} también dice el tipo y la categoría en texto. */
    private static void assertRowShows(final String value, final int typeText,
                                       final int categoryText) {
        waitUntil(() -> onView(withId(R.id.list_history))
                .check(matches(hasDescendant(withText(value)))));
        onView(allOf(withId(R.id.card_history_entry), hasDescendant(withText(value))))
                .check(matches(allOf(
                        hasDescendant(withText(typeText)),
                        hasDescendant(withText(categoryText)))));
    }

    private static long now() {
        return System.currentTimeMillis();
    }

    /** Reintenta {@code check} hasta que pase o se acabe el tiempo; entonces lanza el último fallo. */
    private static void waitUntil(final Runnable check) {
        final long deadline = SystemClock.uptimeMillis() + WAIT_MS;
        while (true) {
            try {
                check.run();
                return;
            } catch (AssertionError | NoMatchingViewException e) {
                if (SystemClock.uptimeMillis() > deadline) {
                    throw e;
                }
                SystemClock.sleep(POLL_MS);
            }
        }
    }
}
