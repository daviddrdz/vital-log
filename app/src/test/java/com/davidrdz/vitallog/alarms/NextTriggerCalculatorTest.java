package com.davidrdz.vitallog.alarms;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.OptionalLong;

public class NextTriggerCalculatorTest {

    private static final ZoneId MEXICO = ZoneId.of("America/Mexico_City");
    private static final ZoneId NEW_YORK = ZoneId.of("America/New_York");

    private static long millis(final ZoneId zone, final int month, final int day, final int hour,
                               final int minute) {
        return LocalDateTime.of(2026, month, day, hour, minute)
                .atZone(zone).toInstant().toEpochMilli();
    }

    private static ZonedDateTime at(final ZoneId zone, final long epochMillis) {
        return java.time.Instant.ofEpochMilli(epochMillis).atZone(zone);
    }

    // ===== Toma diaria =====

    @Test
    public void futureTime_isToday() {
        final NextTriggerCalculator calc = new NextTriggerCalculator(MEXICO);
        assertEquals(millis(MEXICO, 11, 17, 20, 30),
                calc.nextDailyTrigger(20, 30, millis(MEXICO, 11, 17, 10, 0)));
    }

    @Test
    public void pastTime_isTomorrow() {
        final NextTriggerCalculator calc = new NextTriggerCalculator(MEXICO);
        assertEquals(millis(MEXICO, 11, 18, 8, 0),
                calc.nextDailyTrigger(8, 0, millis(MEXICO, 11, 17, 10, 0)));
    }

    @Test
    public void sameTimeAsNow_isTomorrow() {
        final NextTriggerCalculator calc = new NextTriggerCalculator(MEXICO);
        assertEquals(millis(MEXICO, 11, 18, 10, 0),
                calc.nextDailyTrigger(10, 0, millis(MEXICO, 11, 17, 10, 0)));
    }

    @Test
    public void crossesMidnight() {
        final NextTriggerCalculator calc = new NextTriggerCalculator(MEXICO);
        assertEquals(millis(MEXICO, 11, 18, 0, 10),
                calc.nextDailyTrigger(0, 10, millis(MEXICO, 11, 17, 23, 50)));
    }

    @Test
    public void crossesEndOfYear() {
        final NextTriggerCalculator calc = new NextTriggerCalculator(MEXICO);
        final long now = LocalDateTime.of(2026, 12, 31, 22, 0)
                .atZone(MEXICO).toInstant().toEpochMilli();
        final long expected = LocalDateTime.of(2027, 1, 1, 7, 0)
                .atZone(MEXICO).toInstant().toEpochMilli();
        assertEquals(expected, calc.nextDailyTrigger(7, 0, now));
    }

    // ===== Horario de verano (America/New_York, 2026: 8 de marzo y 1 de noviembre) =====

    @Test
    public void springForward_keepsLocalHour() {
        final NextTriggerCalculator calc = new NextTriggerCalculator(NEW_YORK);
        final long trigger = calc.nextDailyTrigger(8, 0, millis(NEW_YORK, 3, 7, 22, 0));
        final ZonedDateTime local = at(NEW_YORK, trigger);
        assertEquals(LocalDateTime.of(2026, 3, 8, 8, 0), local.toLocalDateTime());
    }

    @Test
    public void springForward_missingHour_movesToNextValidHour() {
        final NextTriggerCalculator calc = new NextTriggerCalculator(NEW_YORK);
        final long trigger = calc.nextDailyTrigger(2, 30, millis(NEW_YORK, 3, 7, 22, 0));
        assertEquals(LocalDateTime.of(2026, 3, 8, 3, 30), at(NEW_YORK, trigger).toLocalDateTime());
    }

    @Test
    public void fallBack_keepsLocalHour() {
        final NextTriggerCalculator calc = new NextTriggerCalculator(NEW_YORK);
        final long trigger = calc.nextDailyTrigger(8, 0, millis(NEW_YORK, 10, 31, 9, 0));
        assertEquals(LocalDateTime.of(2026, 11, 1, 8, 0), at(NEW_YORK, trigger).toLocalDateTime());
    }

    // ===== Citas =====

    @Test
    public void appointment_isRemindedTwoHoursBefore() {
        final NextTriggerCalculator calc = new NextTriggerCalculator(MEXICO);
        final OptionalLong trigger = calc.appointmentTrigger(
                millis(MEXICO, 11, 20, 11, 0), millis(MEXICO, 11, 17, 10, 0));
        assertEquals(millis(MEXICO, 11, 20, 9, 0), trigger.getAsLong());
    }

    @Test
    public void appointment_whoseReminderAlreadyPassed_isNotScheduled() {
        final NextTriggerCalculator calc = new NextTriggerCalculator(MEXICO);
        assertFalse(calc.appointmentTrigger(
                millis(MEXICO, 11, 17, 11, 0), millis(MEXICO, 11, 17, 10, 0)).isPresent());
    }

    @Test
    public void appointment_reminderExactlyNow_isNotScheduled() {
        final NextTriggerCalculator calc = new NextTriggerCalculator(MEXICO);
        assertFalse(calc.appointmentTrigger(
                millis(MEXICO, 11, 17, 12, 0), millis(MEXICO, 11, 17, 10, 0)).isPresent());
    }
}
