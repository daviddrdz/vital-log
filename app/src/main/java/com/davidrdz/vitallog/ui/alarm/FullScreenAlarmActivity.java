package com.davidrdz.vitallog.ui.alarm;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.davidrdz.vitallog.R;
import com.davidrdz.vitallog.VitalLogApp;
import com.davidrdz.vitallog.alarms.Reminder;
import com.davidrdz.vitallog.alarms.ReminderIntents;
import com.davidrdz.vitallog.data.entity.Appointment;
import com.davidrdz.vitallog.data.entity.DoseLog;
import com.davidrdz.vitallog.data.entity.Medication;
import com.davidrdz.vitallog.data.repository.SaveResult;
import com.davidrdz.vitallog.feedback.FeedbackDurations;
import com.davidrdz.vitallog.feedback.SaveFeedback;
import com.davidrdz.vitallog.ui.agenda.DoseUnitLabels;
import com.davidrdz.vitallog.ui.common.SpokenTimeFormatter;
import com.davidrdz.vitallog.ui.common.SystemBarInsets;
import com.davidrdz.vitallog.ui.common.ViewModelFactory;
import com.google.android.material.button.MaterialButton;

import java.util.Objects;

/**
 * Modal a pantalla completa de un recordatorio. Enciende la pantalla y se muestra sobre la
 * pantalla de bloqueo, con exactamente 2 botones: "Ya lo tomé" / "Entendido" y "Recordar más
 * tarde". El botón Atrás del sistema solo cierra el modal: la notificación sigue activa.
 */
public final class FullScreenAlarmActivity extends AppCompatActivity {

    private static final String ACTION_SHOW = "com.davidrdz.vitallog.action.SHOW_ALARM";
    private static final String ACTION_TAKEN = "com.davidrdz.vitallog.action.TAKEN";

    /** Cierra el modal cuando termina el banner de "Guardado". */
    private final Runnable closeAfterFeedback = this::finish;

    private AlarmViewModel viewModel;
    private SaveFeedback feedback;
    private View root;
    private ImageView pictogram;
    private TextView headingText;
    private TextView nameText;
    private TextView detailText;
    private TextView timeText;
    private MaterialButton confirmButton;

    /** Abre el modal (Full-Screen Intent y toque en la notificación). */
    @NonNull
    public static PendingIntent showIntent(@NonNull final Context context,
                                           @NonNull final Reminder reminder) {
        return activityIntent(context, ACTION_SHOW, reminder);
    }

    /** Acción "Ya lo tomé" de la notificación: abre el modal, registra la toma y confirma. */
    @NonNull
    public static PendingIntent takenIntent(@NonNull final Context context,
                                            @NonNull final Reminder reminder) {
        return activityIntent(context, ACTION_TAKEN, reminder);
    }

    @NonNull
    private static PendingIntent activityIntent(@NonNull final Context context,
                                                @NonNull final String action,
                                                @NonNull final Reminder reminder) {
        final Intent intent = ReminderIntents.put(
                new Intent(context, FullScreenAlarmActivity.class)
                        .setAction(action)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK), reminder);
        return PendingIntent.getActivity(context, reminder.notificationId(), intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showOverLockScreen();
        setContentView(R.layout.activity_full_screen_alarm);
        root = findViewById(R.id.screen_root);
        SystemBarInsets.applyAsPadding(root);
        pictogram = findViewById(R.id.img_alarm_pictogram);
        headingText = findViewById(R.id.txt_alarm_heading);
        nameText = findViewById(R.id.txt_alarm_name);
        detailText = findViewById(R.id.txt_alarm_detail);
        timeText = findViewById(R.id.txt_alarm_time);
        confirmButton = findViewById(R.id.btn_alarm_confirm);
        feedback = SaveFeedback.attach(this, findViewById(R.id.feedback_banner));

        viewModel = ViewModelFactory.get(this, AlarmViewModel.class);
        viewModel.getContent().observe(this, this::render);
        viewModel.getTakenEvents().observe(this, event -> {
            final SaveResult<DoseLog> result = event.getContentIfNotHandled();
            if (result != null && result.isSuccess()) {
                showTakenAndClose();
            }
        });
        viewModel.getCloseEvents().observe(this, event -> {
            if (event.getContentIfNotHandled() != null) {
                finish();
            }
        });
        confirmButton.setOnClickListener(view -> viewModel.onConfirm());
        findViewById(R.id.btn_alarm_snooze).setOnClickListener(view -> viewModel.onSnooze());

        // Tras rotar, el ViewModel conserva el recordatorio: no se vuelve a cargar ni a registrar.
        if (savedInstanceState == null) {
            handleIntent(getIntent());
        }
    }

    @Override
    protected void onNewIntent(@NonNull final Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        root.removeCallbacks(closeAfterFeedback);
        handleIntent(intent);
    }

    @Override
    protected void onDestroy() {
        root.removeCallbacks(closeAfterFeedback);
        super.onDestroy();
    }

    private void handleIntent(@NonNull final Intent intent) {
        final Reminder reminder = ReminderIntents.read(intent);
        if (reminder == null) {
            finish();
            return;
        }
        viewModel.load(reminder, ACTION_TAKEN.equals(intent.getAction()));
    }

    /**
     * Enciende la pantalla y muestra el modal sobre la pantalla de bloqueo. Los flags de ventana
     * están obsoletos desde API 27, pero son la única forma en API 26 (minSdk).
     */
    @SuppressWarnings("deprecation")
    private void showOverLockScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
        } else {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                    | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void render(@NonNull final AlarmViewModel.AlarmContent content) {
        final Medication medication = content.getMedication();
        if (medication != null) {
            renderMedication(medication);
        } else {
            renderAppointment(Objects.requireNonNull(content.getAppointment()));
        }
    }

    private void renderMedication(@NonNull final Medication medication) {
        final SpokenTimeFormatter formatter = VitalLogApp.from(this).getTimeFormatter();
        pictogram.setImageResource(R.drawable.ic_medication);
        pictogram.setContentDescription(getString(R.string.alarm_pictogram_medication));
        headingText.setText(R.string.alarm_heading_medication);
        nameText.setText(medication.getName());
        detailText.setText(getString(R.string.medication_dose_line, medication.getDoseAmount(),
                DoseUnitLabels.forAmount(getResources(), medication.getDoseUnit(),
                        medication.getDoseAmount())));
        timeText.setText(getString(R.string.medication_time_line,
                formatter.formatTime(medication.getHour(), medication.getMinute())));
        timeText.setVisibility(View.VISIBLE);
        confirmButton.setText(R.string.alarm_btn_taken);
    }

    private void renderAppointment(@NonNull final Appointment appointment) {
        final VitalLogApp app = VitalLogApp.from(this);
        pictogram.setImageResource(R.drawable.ic_calendar);
        pictogram.setContentDescription(getString(R.string.alarm_pictogram_appointment));
        headingText.setText(R.string.alarm_heading_appointment);
        nameText.setText(appointment.getDoctorOrPlace());
        detailText.setText(getString(R.string.reminder_appointment_text,
                app.getTimeFormatter().formatDateTime(appointment.getScheduledAt(),
                        app.getClock().millis())));
        timeText.setVisibility(View.GONE);
        confirmButton.setText(R.string.reminder_action_understood);
    }

    private void showTakenAndClose() {
        final VitalLogApp app = VitalLogApp.from(this);
        final Medication medication = Objects.requireNonNull(Objects.requireNonNull(
                viewModel.getContent().getValue()).getMedication());
        final long now = app.getClock().millis();
        feedback.showSuccess(getString(R.string.saved_dose, medication.getName(),
                app.getTimeFormatter().formatDateTime(now, now)));
        root.postDelayed(closeAfterFeedback, FeedbackDurations.BANNER_VISIBLE_MS);
    }
}
