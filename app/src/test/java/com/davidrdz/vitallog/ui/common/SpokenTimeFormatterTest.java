package com.davidrdz.vitallog.ui.common;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;

public class SpokenTimeFormatterTest {

    private static final ZoneId ZONE = ZoneId.of("America/Mexico_City");

    /** Mismos textos que strings.xml. */
    private static final SpokenTimeFormatter FORMATTER = new SpokenTimeFormatter(ZONE,
            new SpokenTimeFormatter.Labels("%1$d:%2$02d %3$s", "de la madrugada",
                    "de la mañana", "del mediodía", "de la tarde", "de la noche",
                    "hoy", "ayer", "mañana", "EEEE d 'de' MMMM", "%1$s, %2$s"));

    /** Martes 17 de noviembre de 2026, 10:00. */
    private static final long NOW = millis(2026, 11, 17, 10, 0);

    private static long millis(final int year, final int month, final int day, final int hour,
                               final int minute) {
        return LocalDateTime.of(year, month, day, hour, minute)
                .atZone(ZONE).toInstant().toEpochMilli();
    }

    // ===== Horas =====

    @Test
    public void midnight_isTwelveAtNight() {
        assertEquals("12:00 de la noche", FORMATTER.formatTime(0, 0));
    }

    @Test
    public void noon_isTwelveMidday() {
        assertEquals("12:00 del mediodía", FORMATTER.formatTime(12, 0));
    }

    @Test
    public void afterNoon_isAfternoon() {
        assertEquals("12:30 de la tarde", FORMATTER.formatTime(12, 30));
    }

    @Test
    public void eightOhFive_padsMinutes() {
        assertEquals("8:05 de la mañana", FORMATTER.formatTime(8, 5));
    }

    @Test
    public void dayPeriods_changeAtTheirBoundaries() {
        assertEquals("12:30 de la noche", FORMATTER.formatTime(0, 30));
        assertEquals("1:00 de la madrugada", FORMATTER.formatTime(1, 0));
        assertEquals("5:55 de la madrugada", FORMATTER.formatTime(5, 55));
        assertEquals("6:00 de la mañana", FORMATTER.formatTime(6, 0));
        assertEquals("11:55 de la mañana", FORMATTER.formatTime(11, 55));
        assertEquals("6:55 de la tarde", FORMATTER.formatTime(18, 55));
        assertEquals("7:00 de la noche", FORMATTER.formatTime(19, 0));
        assertEquals("11:55 de la noche", FORMATTER.formatTime(23, 55));
    }

    @Test
    public void toTwelveHour_mapsMidnightAndNoonToTwelve() {
        assertEquals(12, SpokenTimeFormatter.toTwelveHour(0));
        assertEquals(1, SpokenTimeFormatter.toTwelveHour(13));
        assertEquals(12, SpokenTimeFormatter.toTwelveHour(12));
        assertEquals(11, SpokenTimeFormatter.toTwelveHour(23));
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidHour_isRejected() {
        FORMATTER.formatTime(24, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidMinute_isRejected() {
        FORMATTER.formatTime(8, 60);
    }

    @Test
    public void formatTime_fromEpoch_usesFormatterZone() {
        assertEquals("8:30 de la mañana", FORMATTER.formatTime(millis(2026, 11, 17, 8, 30)));
    }

    // ===== Días =====

    @Test
    public void sameDay_isToday() {
        assertEquals("hoy", FORMATTER.formatDay(millis(2026, 11, 17, 0, 5), NOW));
        assertEquals("hoy", FORMATTER.formatDay(millis(2026, 11, 17, 23, 55), NOW));
    }

    @Test
    public void previousDay_isYesterday() {
        assertEquals("ayer", FORMATTER.formatDay(millis(2026, 11, 16, 23, 55), NOW));
    }

    @Test
    public void nextDay_isTomorrow() {
        assertEquals("mañana", FORMATTER.formatDay(millis(2026, 11, 18, 0, 5), NOW));
    }

    @Test
    public void calendarDay_countsNotTwentyFourHours() {
        final long lateNight = millis(2026, 11, 17, 23, 59);
        assertEquals("mañana", FORMATTER.formatDay(millis(2026, 11, 18, 0, 1), lateNight));
    }

    @Test
    public void otherDays_useWeekdayAndDate() {
        assertEquals("viernes 13 de noviembre",
                FORMATTER.formatDay(millis(2026, 11, 13, 9, 0), NOW));
        assertEquals("martes 1 de diciembre",
                FORMATTER.formatDay(millis(2026, 12, 1, 9, 0), NOW));
    }

    @Test
    public void dateTime_joinsDayAndTime() {
        assertEquals("hoy, 8:30 de la mañana",
                FORMATTER.formatDateTime(millis(2026, 11, 17, 8, 30), NOW));
        assertEquals("ayer, 12:00 del mediodía",
                FORMATTER.formatDateTime(millis(2026, 11, 16, 12, 0), NOW));
        assertEquals("viernes 13 de noviembre, 7:15 de la noche",
                FORMATTER.formatDateTime(millis(2026, 11, 13, 19, 15), NOW));
    }

    // ===== Encabezados por día (historial) =====

    @Test
    public void dayHeading_today_isCapitalizedWithDate() {
        assertEquals("Hoy, martes 17 de noviembre",
                FORMATTER.formatDayHeading(millis(2026, 11, 17, 8, 30), NOW));
    }

    @Test
    public void dayHeading_yesterday_isCapitalizedWithDate() {
        assertEquals("Ayer, lunes 16 de noviembre",
                FORMATTER.formatDayHeading(millis(2026, 11, 16, 23, 55), NOW));
    }

    @Test
    public void dayHeading_olderDay_capitalizesWeekday() {
        assertEquals("Viernes 13 de noviembre",
                FORMATTER.formatDayHeading(millis(2026, 11, 13, 9, 0), NOW));
        assertEquals("Lunes 19 de octubre",
                FORMATTER.formatDayHeading(millis(2026, 10, 19, 0, 0), NOW));
    }

    @Test
    public void dayHeading_usesCalendarDayAfterMidnight() {
        final long justAfterMidnight = millis(2026, 11, 17, 0, 1);
        assertEquals("Ayer, lunes 16 de noviembre",
                FORMATTER.formatDayHeading(millis(2026, 11, 16, 23, 59), justAfterMidnight));
    }

    @Test
    public void dayWithDate_addsDateToRelativeDay() {
        assertEquals("mañana, miércoles 18 de noviembre",
                FORMATTER.formatDayWithDate(millis(2026, 11, 18, 10, 0), NOW));
        assertEquals("viernes 20 de noviembre",
                FORMATTER.formatDayWithDate(millis(2026, 11, 20, 10, 0), NOW));
    }
}
