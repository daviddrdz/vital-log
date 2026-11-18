package com.davidrdz.vitallog.alarms;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.os.Build;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.davidrdz.vitallog.R;
import com.davidrdz.vitallog.VitalLogApp;
import com.davidrdz.vitallog.data.entity.Appointment;
import com.davidrdz.vitallog.data.entity.Medication;
import com.davidrdz.vitallog.ui.agenda.DoseUnitLabels;
import com.davidrdz.vitallog.ui.alarm.FullScreenAlarmActivity;
import com.davidrdz.vitallog.ui.common.SpokenTimeFormatter;

/**
 * Notificación de alarma de un recordatorio: canal de importancia alta con sonido de alarma y
 * vibración, categoría {@link NotificationCompat#CATEGORY_ALARM} y persistente hasta que el usuario
 * responde con uno de sus 2 botones.
 */
public final class ReminderNotifier {

    static final String CHANNEL_ID = "reminders";
    /** Vibración larga y repetida, fácil de notar: espera, vibra, pausa, vibra… (ms). */
    private static final long[] VIBRATION_PATTERN = {0, 800, 400, 800, 400, 800};

    @NonNull
    private final Context context;

    public ReminderNotifier(@NonNull final Context context) {
        this.context = context.getApplicationContext();
    }

    /** Crea el canal de alarmas. Es seguro llamarlo varias veces. */
    public static void createChannel(@NonNull final Context context) {
        final NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                context.getString(R.string.reminder_channel_name),
                NotificationManager.IMPORTANCE_HIGH);
        channel.setDescription(context.getString(R.string.reminder_channel_description));
        channel.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM),
                new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build());
        channel.enableVibration(true);
        channel.setVibrationPattern(VIBRATION_PATTERN);
        channel.setLockscreenVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        context.getSystemService(NotificationManager.class).createNotificationChannel(channel);
    }

    public void showMedication(@NonNull final Medication medication,
                               @NonNull final Reminder reminder) {
        final String unit = DoseUnitLabels.forAmount(context.getResources(),
                medication.getDoseUnit(), medication.getDoseAmount());
        final String text = context.getString(R.string.reminder_medication_text,
                medication.getDoseAmount(), unit,
                timeFormatter().formatTime(medication.getHour(), medication.getMinute()));
        final NotificationCompat.Builder builder =
                baseBuilder(R.drawable.ic_medication, medication.getName(), text, reminder)
                        .addAction(R.drawable.ic_check,
                                context.getString(R.string.alarm_btn_taken),
                                FullScreenAlarmActivity.takenIntent(context, reminder))
                        .addAction(R.drawable.ic_snooze,
                                context.getString(R.string.reminder_action_snooze),
                                ReminderActionReceiver.snoozeIntent(context, reminder));
        notify(reminder, builder);
    }

    public void showAppointment(@NonNull final Appointment appointment,
                                @NonNull final Reminder reminder) {
        final String title = context.getString(R.string.reminder_appointment_title,
                appointment.getDoctorOrPlace());
        final String text = context.getString(R.string.reminder_appointment_text,
                timeFormatter().formatDateTime(appointment.getScheduledAt(),
                        System.currentTimeMillis()));
        final NotificationCompat.Builder builder =
                baseBuilder(R.drawable.ic_calendar, title, text, reminder)
                        .addAction(R.drawable.ic_check,
                                context.getString(R.string.reminder_action_understood),
                                ReminderActionReceiver.dismissIntent(context, reminder))
                        .addAction(R.drawable.ic_snooze,
                                context.getString(R.string.reminder_action_snooze),
                                ReminderActionReceiver.snoozeIntent(context, reminder));
        notify(reminder, builder);
    }

    /** Quita la notificación del recordatorio. */
    public void cancel(@NonNull final Reminder reminder) {
        NotificationManagerCompat.from(context).cancel(reminder.notificationId());
    }

    @NonNull
    private NotificationCompat.Builder baseBuilder(@DrawableRes final int icon,
                                                   @NonNull final String title,
                                                   @NonNull final String text,
                                                   @NonNull final Reminder reminder) {
        final PendingIntent showModal = FullScreenAlarmActivity.showIntent(context, reminder);
        return new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(icon)
                .setContentTitle(title)
                .setContentText(text)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentIntent(showModal)
                .setFullScreenIntent(showModal, true)
                .setOngoing(true)
                .setAutoCancel(false);
    }

    /** Se pide en cada uso para respetar un cambio de zona horaria. */
    @NonNull
    private SpokenTimeFormatter timeFormatter() {
        return VitalLogApp.from(context).getTimeFormatter();
    }

    private void notify(@NonNull final Reminder reminder,
                        @NonNull final NotificationCompat.Builder builder) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(context,
                Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        NotificationManagerCompat.from(context).notify(reminder.notificationId(), builder.build());
    }
}
