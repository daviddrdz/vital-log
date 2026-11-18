package com.davidrdz.vitallog.alarms;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.davidrdz.vitallog.VitalLogApp;

/**
 * Acciones de la notificación que no registran nada: "Recordar más tarde" (pospone 10 min) y
 * "Entendido" (cierra el aviso de una cita).
 */
public final class ReminderActionReceiver extends BroadcastReceiver {

    private static final String ACTION_SNOOZE = "com.davidrdz.vitallog.action.SNOOZE";
    private static final String ACTION_DISMISS = "com.davidrdz.vitallog.action.DISMISS";

    @NonNull
    static PendingIntent snoozeIntent(@NonNull final Context context,
                                      @NonNull final Reminder reminder) {
        return broadcast(context, ACTION_SNOOZE, reminder);
    }

    @NonNull
    static PendingIntent dismissIntent(@NonNull final Context context,
                                       @NonNull final Reminder reminder) {
        return broadcast(context, ACTION_DISMISS, reminder);
    }

    @NonNull
    private static PendingIntent broadcast(@NonNull final Context context,
                                           @NonNull final String action,
                                           @NonNull final Reminder reminder) {
        final Intent intent = ReminderIntents.put(
                new Intent(context, ReminderActionReceiver.class).setAction(action), reminder);
        return PendingIntent.getBroadcast(context, reminder.notificationId(), intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    @Override
    public void onReceive(final Context context, final Intent intent) {
        final Reminder reminder = ReminderIntents.read(intent);
        if (reminder == null) {
            return;
        }
        if (ACTION_SNOOZE.equals(intent.getAction())) {
            VitalLogApp.from(context).getAlarmScheduler().snooze(reminder);
            new ReminderNotifier(context).cancel(reminder);
        } else if (ACTION_DISMISS.equals(intent.getAction())) {
            new ReminderNotifier(context).cancel(reminder);
        }
    }
}
