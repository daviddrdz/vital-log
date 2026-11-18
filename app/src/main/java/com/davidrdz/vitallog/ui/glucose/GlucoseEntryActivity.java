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
import com.davidrdz.vitallog.ui.common.ViewModelFactory;
import com.davidrdz.vitallog.ui.widget.NumericStepperView;
import com.davidrdz.vitallog.ui.widget.VitalUnitLabels;

/**
 * Pantalla para registrar la glucosa con el control +/−. Arranca en la última lectura guardada y
 * confirma cada guardado con banner + tono.
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

        final GlucoseEntryViewModel viewModel =
                ViewModelFactory.get(this, GlucoseEntryViewModel.class);
        viewModel.getInitialValue().observe(this, event -> {
            final Integer latest = event == null ? null : event.getContentIfNotHandled();
            if (latest != null) {
                glucoseStepper.setValue(latest);
            }
        });
        viewModel.getSaveEvents().observe(this, event -> {
            final SaveResult<GlucoseReading> result = event.getContentIfNotHandled();
            if (result != null) {
                showSaved(result.getSaved());
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

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_glucose_entry;
    }

    @Override
    protected int getScreenTitleRes() {
        return R.string.title_glucose_entry;
    }
}
