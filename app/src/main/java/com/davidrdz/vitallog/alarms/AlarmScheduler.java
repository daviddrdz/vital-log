package com.davidrdz.vitallog.alarms;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.davidrdz.vitallog.data.entity.Appointment;
import com.davidrdz.vitallog.data.entity.Medication;
import com.davidrdz.vitallog.ui.agenda.AgendaActivity;

import java.time.ZoneId;
import java.util.OptionalLong;

/**
 * Programa los recordatorios con {@link AlarmManager#setAlarmClock} (exacto, despierta al
 * teléfono y aparece en el reloj del sistema). Si el usuario no ha concedido el permiso de alarmas
 * exactas, programa una alarma aproximada para no perder el aviso; la pantalla de permisos pide
 * concederlo y {@link BootReceiver} reprograma todo en cuanto se concede.
 */
public final class AlarmScheduler implements ReminderScheduler {

    /** Código del PendingIntent que abre la agenda desde el reloj del sistema. */
    private static final int SHOW_AGENDA_REQUEST_CODE = 0;

    @NonNull
    private final Context context;
    @NonNull
    private final AlarmManager alarmManager;

    public AlarmScheduler(@NonNull final Context context) {
        this.context = context.getApplicationContext();
        this.alarmManager = context.getSystemService(AlarmManager.class);
    }

    @Override
    public void scheduleMedication(@NonNull final Medication medication) {
        if (!medication.isActive()) {
            cancel(ReminderType.MEDICATION, medication.getId());
            return;
        }
        final long triggerAt = calculator().nextDailyTrigger(
                medication.getHour(), medication.getMinute(), System.currentTimeMillis());
        setAt(triggerAt, alarmIntent(
                new Reminder(ReminderType.MEDICATION, medication.getId(), triggerAt), false));
    }

    @Override
    public void scheduleAppointment(@NonNull final Appointment appointment) {
        final OptionalLong triggerAt = calculator().appointmentTrigger(
                appointment.getScheduledAt(), System.currentTimeMillis());
        if (!appointment.isActive() || !triggerAt.isPresent()) {
            cancel(ReminderType.APPOINTMENT, appointment.getId());
            return;
        }
        setAt(triggerAt.getAsLong(), alarmIntent(new Reminder(ReminderType.APPOINTMENT,
                appointment.getId(), triggerAt.getAsLong()), false));
    }

    /**
     * "Recordar más tarde": alarma única dentro de 10 min, sin tocar la alarma diaria. Conserva la
     * hora programada original del recordatorio.
     */
    public void snooze(@NonNull final Reminder reminder) {
        setAt(ReminderPolicy.snoozeTriggerAt(System.currentTimeMillis()),
                alarmIntent(reminder, true));
    }

    @Override
    public void cancel(@NonNull final ReminderType type, final long id) {
        cancelIntent(existingAlarmIntent(type, id, false));
        cancelIntent(existingAlarmIntent(type, id, true));
    }

    private void cancelIntent(@Nullable final PendingIntent intent) {
        if (intent != null) {
            alarmManager.cancel(intent);
            intent.cancel();
        }
    }

    private void setAt(final long triggerAt, @NonNull final PendingIntent operation) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S
                || alarmManager.canScheduleExactAlarms()) {
            alarmManager.setAlarmClock(
                    new AlarmManager.AlarmClockInfo(triggerAt, showAgendaIntent()), operation);
        } else {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, operation);
        }
    }

    /** La zona se lee en cada cálculo para respetar un cambio de zona horaria. */
    @NonNull
    private static NextTriggerCalculator calculator() {
        return new NextTriggerCalculator(ZoneId.systemDefault());
    }

    @NonNull
    private PendingIntent alarmIntent(@NonNull final Reminder reminder, final boolean snooze) {
        return PendingIntent.getBroadcast(context,
                requestCode(reminder.getType(), reminder.getId(), snooze),
                ReminderReceiver.intent(context, reminder, snooze),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    /** El PendingIntent ya programado, o null. Los extras no cuentan para encontrarlo. */
    @Nullable
    private PendingIntent existingAlarmIntent(@NonNull final ReminderType type, final long id,
                                              final boolean snooze) {
        return PendingIntent.getBroadcast(context, requestCode(type, id, snooze),
                ReminderReceiver.intent(context, new Reminder(type, id, 0), snooze),
                PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE);
    }

    private static int requestCode(@NonNull final ReminderType type, final long id,
                                   final boolean snooze) {
        return snooze ? ReminderRequestCodes.snooze(type, id)
                : ReminderRequestCodes.reminder(type, id);
    }

    @NonNull
    private PendingIntent showAgendaIntent() {
        return PendingIntent.getActivity(context, SHOW_AGENDA_REQUEST_CODE,
                new Intent(context, AgendaActivity.class), PendingIntent.FLAG_IMMUTABLE);
    }
}
