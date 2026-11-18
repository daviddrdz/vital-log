package com.davidrdz.vitallog.alarms;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ReminderPolicyTest {

    private static final long TEN_MINUTES_MS = 10L * 60L * 1000L;

    @Test
    public void snooze_isExactlyTenMinutes() {
        final long now = 1_700_000_000_000L;
        assertEquals(now + TEN_MINUTES_MS, ReminderPolicy.snoozeTriggerAt(now));
    }

    @Test
    public void appointmentLead_isTwoHours() {
        assertEquals(120, ReminderPolicy.APPOINTMENT_LEAD_MINUTES);
    }
}
