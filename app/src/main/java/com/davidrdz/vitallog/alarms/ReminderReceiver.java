package com.davidrdz.vitallog.alarms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.davidrdz.vitallog.VitalLogApp;

/**
 * Recibe la alarma de un recordatorio y muestra su notificación de alarma. Si es la alarma diaria
 * de un medicamento, además programa la del día siguiente. Si el medicamento o la cita ya se
 * borró, no hace nada.
 */
public final class ReminderReceiver extends BroadcastReceiver {

    private static final String ACTION_REMINDER = "com.davidrdz.vitallog.action.REMINDER";
    private static final String EXTRA_SNOOZE = "com.davidrdz.vitallog.extra.REMINDER_SNOOZE";

    /** Intent explícito que dispara este receiver para el recordatorio indicado. */
    @NonNull
    static Intent intent(@NonNull final Context context, @NonNull final Reminder reminder,
                         final boolean snooze) {
        return ReminderIntents.put(new Intent(context, ReminderReceiver.class)
                .setAction(ACTION_REMINDER)
                .putExtra(EXTRA_SNOOZE, snooze), reminder);
    }

    @Override
    public void onReceive(final Context context, final Intent intent) {
        final Reminder reminder = ReminderIntents.read(intent);
        if (!ACTION_REMINDER.equals(intent.getAction()) || reminder == null) {
            return;
        }
        final boolean snooze = intent.getBooleanExtra(EXTRA_SNOOZE, false);
        final VitalLogApp app = VitalLogApp.from(context);
        final ReminderNotifier notifier = new ReminderNotifier(context);
        final PendingResult pending = goAsync();
        if (reminder.getType() == ReminderType.MEDICATION) {
            app.getMedicationRepository().findActive(reminder.getId(), medication -> {
                try {
                    if (medication != null) {
                        if (!snooze) {
                            app.getAlarmScheduler().scheduleMedication(medication);
                        }
                        notifier.showMedication(medication, reminder);
                    }
                } finally {
                    pending.finish();
                }
            });
        } else {
            app.getAppointmentRepository().findActive(reminder.getId(), appointment -> {
                try {
                    if (appointment != null) {
                        notifier.showAppointment(appointment, reminder);
                    }
                } finally {
                    pending.finish();
                }
            });
        }
    }
}
