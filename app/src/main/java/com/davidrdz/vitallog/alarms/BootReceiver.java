package com.davidrdz.vitallog.alarms;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.davidrdz.vitallog.VitalLogApp;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Reprograma todos los recordatorios activos cuando el sistema pierde o desfasa las alarmas:
 * reinicio, cambio de hora o de zona horaria, actualización de la app o concesión del permiso de
 * alarmas exactas.
 */
public final class BootReceiver extends BroadcastReceiver {

    /** Constante de compilación; antes de API 31 esa difusión simplemente nunca llega. */
    @SuppressLint("InlinedApi")
    private static final Set<String> HANDLED_ACTIONS = new HashSet<>(Arrays.asList(
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED));

    @Override
    public void onReceive(final Context context, final Intent intent) {
        if (!HANDLED_ACTIONS.contains(intent.getAction())) {
            return;
        }
        final VitalLogApp app = VitalLogApp.from(context);
        final PendingResult pending = goAsync();
        app.getMedicationRepository().rescheduleReminders(() ->
                app.getAppointmentRepository().rescheduleReminders(
                        System.currentTimeMillis(), pending::finish));
    }
}
