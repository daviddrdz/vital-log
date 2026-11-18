package com.davidrdz.vitallog.ui.agenda;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.davidrdz.vitallog.R;
import com.davidrdz.vitallog.VitalLogApp;
import com.davidrdz.vitallog.data.entity.Appointment;
import com.davidrdz.vitallog.data.repository.SaveResult;
import com.davidrdz.vitallog.feedback.FeedbackTonePlayer;
import com.davidrdz.vitallog.ui.common.BaseScreenActivity;
import com.davidrdz.vitallog.ui.common.SpokenTimeFormatter;
import com.davidrdz.vitallog.ui.common.ViewModelFactory;
import com.davidrdz.vitallog.ui.widget.NumericStepperView;

/**
 * Alta de una cita: médico o lugar (teclado; no es un signo vital), día con un control
 * "dentro de N días" y la fecha escrita, y hora con dos controles +/−. Al guardar vuelve a la
 * agenda con el mensaje.
 */
public class AppointmentFormActivity extends BaseScreenActivity {

    private static final int MIN_DAYS_AHEAD = 0;
    private static final int DEFAULT_DAYS_AHEAD = 1;
    private static final int DAYS_STEP = 1;
    private static final int DEFAULT_HOUR = 9;
    private static final int DEFAULT_MINUTE = 0;

    private AgendaViewModel viewModel;
    private SpokenTimeFormatter formatter;
    private FeedbackTonePlayer tones;
    private EditText placeField;
    private View placeError;
    private View timeError;
    private TextView dateSummary;
    private NumericStepperView daysStepper;
    private TimeSteppers time;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = ViewModelFactory.get(this, AgendaViewModel.class);
        formatter = VitalLogApp.from(this).getTimeFormatter();
        tones = FeedbackTonePlayer.attach(this);
        placeField = findViewById(R.id.edit_appointment_place);
        placeError = findViewById(R.id.txt_name_error);
        timeError = findViewById(R.id.txt_time_error);
        dateSummary = findViewById(R.id.txt_appointment_date);

        daysStepper = findViewById(R.id.stepper_days);
        daysStepper.bind(MIN_DAYS_AHEAD, Appointment.MAX_DAYS_AHEAD, DEFAULT_DAYS_AHEAD,
                DAYS_STEP, new DaysFormatter());
        time = new TimeSteppers(findViewById(R.id.stepper_hour), findViewById(R.id.stepper_minute),
                findViewById(R.id.txt_time_summary), formatter, DEFAULT_HOUR, DEFAULT_MINUTE,
                this::renderDate);
        daysStepper.setOnValueChangeListener(value -> renderDate());
        renderDate();

        viewModel.getAppointmentSaved().observe(this, event -> {
            final SaveResult<Appointment> result = event.getContentIfNotHandled();
            if (result != null) {
                returnToAgenda(result.getSaved());
            }
        });
        findViewById(R.id.btn_save_appointment).setOnClickListener(view -> save());
    }

    private void save() {
        final AgendaViewModel.FormProblem problem = viewModel.saveAppointment(
                placeField.getText().toString(), daysStepper.getValue(),
                time.getHour(), time.getMinute());
        placeError.setVisibility(
                problem == AgendaViewModel.FormProblem.MISSING_NAME ? View.VISIBLE : View.GONE);
        timeError.setVisibility(problem == AgendaViewModel.FormProblem.TIME_ALREADY_PASSED
                ? View.VISIBLE : View.GONE);
        if (problem == AgendaViewModel.FormProblem.MISSING_NAME) {
            placeField.requestFocus();
        }
        if (problem != AgendaViewModel.FormProblem.NONE) {
            tones.playError();
        }
    }

    /** "Fecha: mañana, miércoles 18 de noviembre"; se actualiza con cada cambio de día u hora. */
    private void renderDate() {
        final long scheduledAt = viewModel.scheduledAtFor(daysStepper.getValue(),
                time == null ? DEFAULT_HOUR : time.getHour(),
                time == null ? DEFAULT_MINUTE : time.getMinute());
        dateSummary.setText(getString(R.string.appointment_date_summary,
                formatter.formatDayWithDate(scheduledAt, viewModel.now())));
        timeError.setVisibility(View.GONE);
    }

    private void returnToAgenda(@NonNull final Appointment saved) {
        final String message = getString(R.string.saved_appointment, saved.getDoctorOrPlace(),
                formatter.formatDateTime(saved.getScheduledAt(), viewModel.now()));
        setResult(RESULT_OK, new Intent().putExtra(AgendaActivity.EXTRA_SAVED_MESSAGE, message));
        finish();
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_appointment_form;
    }

    @Override
    protected int getScreenTitleRes() {
        return R.string.title_appointment_form;
    }

    /** "1" + "día", "3" + "días". */
    private final class DaysFormatter implements NumericStepperView.ValueFormatter {

        @NonNull
        @Override
        public CharSequence formatValue(final int value) {
            return getString(R.string.stepper_value, value);
        }

        @NonNull
        @Override
        public CharSequence formatUnit(final int value) {
            return getResources().getQuantityString(R.plurals.unit_days, value);
        }

        @NonNull
        @Override
        public CharSequence formatSpokenUnit(final int value) {
            return formatUnit(value);
        }
    }
}
