package com.davidrdz.vitallog.ui.home;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.time.LocalTime;

public class DayPeriodTest {

    private static DayPeriod at(final int hour, final int minute) {
        return DayPeriod.from(LocalTime.of(hour, minute));
    }

    @Test
    public void midnight_isNight() {
        assertEquals(DayPeriod.NIGHT, at(0, 0));
    }

    @Test
    public void beforeFive_isNight() {
        assertEquals(DayPeriod.NIGHT, at(4, 59));
    }

    @Test
    public void five_startsMorning() {
        assertEquals(DayPeriod.MORNING, at(5, 0));
    }

    @Test
    public void beforeNoon_isMorning() {
        assertEquals(DayPeriod.MORNING, at(11, 59));
    }

    @Test
    public void noon_startsAfternoon() {
        assertEquals(DayPeriod.AFTERNOON, at(12, 0));
    }

    @Test
    public void beforeSeven_isAfternoon() {
        assertEquals(DayPeriod.AFTERNOON, at(18, 59));
    }

    @Test
    public void seven_startsNight() {
        assertEquals(DayPeriod.NIGHT, at(19, 0));
    }

    @Test
    public void lastMinuteOfDay_isNight() {
        assertEquals(DayPeriod.NIGHT, at(23, 59));
    }
}
