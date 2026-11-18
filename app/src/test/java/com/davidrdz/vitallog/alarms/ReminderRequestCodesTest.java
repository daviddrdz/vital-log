package com.davidrdz.vitallog.alarms;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

public class ReminderRequestCodesTest {

    @Test
    public void codes_doNotCollideForSameId() {
        final long id = 7;
        final Set<Integer> codes = new HashSet<>();
        for (final ReminderType type : ReminderType.values()) {
            codes.add(ReminderRequestCodes.reminder(type, id));
            codes.add(ReminderRequestCodes.snooze(type, id));
        }
        assertEquals(4, codes.size());
    }

    @Test
    public void codes_doNotCollideAcrossIds() {
        final Set<Integer> codes = new HashSet<>();
        int expected = 0;
        for (long id = 1; id <= 50; id++) {
            for (final ReminderType type : ReminderType.values()) {
                codes.add(ReminderRequestCodes.reminder(type, id));
                codes.add(ReminderRequestCodes.snooze(type, id));
                expected += 2;
            }
        }
        assertEquals(expected, codes.size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void unsavedId_isRejected() {
        ReminderRequestCodes.reminder(ReminderType.MEDICATION, 0);
    }
}
