package com.davidrdz.vitallog.alarms;

import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/** Guarda y lee un {@link Reminder} en los extras de un {@link Intent}. */
public final class ReminderIntents {

    private static final String EXTRA_TYPE = "com.davidrdz.vitallog.extra.REMINDER_TYPE";
    private static final String EXTRA_ID = "com.davidrdz.vitallog.extra.REMINDER_ID";
    private static final String EXTRA_SCHEDULED_AT =
            "com.davidrdz.vitallog.extra.REMINDER_SCHEDULED_AT";
    private static final long MISSING = -1;

    private ReminderIntents() {
    }

    @NonNull
    public static Intent put(@NonNull final Intent intent, @NonNull final Reminder reminder) {
        return intent.putExtra(EXTRA_TYPE, reminder.getType().name())
                .putExtra(EXTRA_ID, reminder.getId())
                .putExtra(EXTRA_SCHEDULED_AT, reminder.getScheduledAt());
    }

    /** El recordatorio del intent, o null si le faltan datos. */
    @Nullable
    public static Reminder read(@NonNull final Intent intent) {
        final String typeName = intent.getStringExtra(EXTRA_TYPE);
        final long id = intent.getLongExtra(EXTRA_ID, MISSING);
        final long scheduledAt = intent.getLongExtra(EXTRA_SCHEDULED_AT, MISSING);
        if (typeName == null || id == MISSING || scheduledAt == MISSING) {
            return null;
        }
        try {
            return new Reminder(ReminderType.valueOf(typeName), id, scheduledAt);
        } catch (final IllegalArgumentException unknownType) {
            return null;
        }
    }
}
