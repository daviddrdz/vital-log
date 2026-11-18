package com.davidrdz.vitallog.ui.glucose;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.davidrdz.vitallog.R;
import com.davidrdz.vitallog.VitalLogApp;
import com.davidrdz.vitallog.analytics.VitalRanges;
import com.davidrdz.vitallog.data.entity.GlucoseReading;
import com.davidrdz.vitallog.data.repository.SaveResult;
import com.davidrdz.vitallog.feedback.SaveFeedback;
import com.davidrdz.vitallog.ui.common.BaseScreenActivity;
import com.davidrdz.vitallog.ui.common.ValidationErrorMessages;
import com.davidrdz.vitallog.ui.common.ViewModelFactory;
import com.davidrdz.vitallog.ui.widget.NumericStepperView;
import com.davidrdz.vitallog.ui.widget.ReadingStatusView;
import com.davidrdz.vitallog.ui.widget.VitalUnitLabels;

import java.util.Objects;

/**
 * Pantalla para registrar la glucosa con el control +/−. Arranca en la última lectura guardada,
 * muestra la categoría del valor mientras se ajusta y confirma cada guardado (o rechazo) con
 * banner + tono.
 */
public class GlucoseEntryActivity extends BaseScreenActivity {

    private NumericStepperView glucoseStepper;
    private SaveFeedback feedback;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        glucoseStepper = findViewById(R.id.stepper_glucose);
        glucoseStepper.bind(VitalRanges.GLUCOSE);
        feedback = SaveFeedback.attach(this, findViewById(R.id.feedback_banner));
        final ReadingStatusView statusView = findViewById(R.id.view_glucose_status);

        final GlucoseEntryViewModel viewModel =
                ViewModelFactory.get(this, GlucoseEntryViewModel.class);
        viewModel.getStatus().observe(this, statusView::render);
        glucoseStepper.setOnValueChangeListener(viewModel::onValueChanged);
        viewModel.onValueChanged(glucoseStepper.getValue());
        viewModel.getInitialValue().observe(this, event -> {
            final Integer latest = event == null ? null : event.getContentIfNotHandled();
            if (latest != null) {
                glucoseStepper.setValue(latest);
            }
        });
        viewModel.getSaveEvents().observe(this, event -> {
            final SaveResult<GlucoseReading> result = event.getContentIfNotHandled();
            if (result == null) {
                return;
            }
            if (result.isSuccess()) {
                showSaved(result.getSaved());
            } else {
                showRejected(result);
            }
        });
        findViewById(R.id.btn_save_glucose).setOnClickListener(
                view -> viewModel.save(glucoseStepper.getValue()));
    }

    private void showSaved(@NonNull final GlucoseReading reading) {
        final VitalLogApp app = VitalLogApp.from(this);
        final String when = app.getTimeFormatter()
                .formatDateTime(reading.getTakenAt(), app.getClock().millis());
        feedback.showSuccess(getString(R.string.saved_glucose, reading.getValueMgDl(),
                getString(VitalUnitLabels.shortLabel(VitalRanges.GLUCOSE.getUnit())), when));
    }

    private void showRejected(@NonNull final SaveResult<GlucoseReading> result) {
        feedback.showError(ValidationErrorMessages.notSavedMessage(getResources(),
                Objects.requireNonNull(result.getError())));
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_glucose_entry;
    }

    @Override
    protected int getScreenTitleRes() {
        return R.string.title_glucose_entry;
    }

    /** El mismo plato del botón del inicio: continuidad visual hasta esta pantalla. */
    @Override
    protected int getScreenIconRes() {
        return R.drawable.ic_plate_glucose;
    }
}
