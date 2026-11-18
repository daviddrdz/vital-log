package com.davidrdz.vitallog.ui.pressure;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.davidrdz.vitallog.R;
import com.davidrdz.vitallog.VitalLogApp;
import com.davidrdz.vitallog.analytics.VitalRanges;
import com.davidrdz.vitallog.data.entity.BloodPressureReading;
import com.davidrdz.vitallog.data.repository.SaveResult;
import com.davidrdz.vitallog.feedback.SaveFeedback;
import com.davidrdz.vitallog.ui.common.BaseScreenActivity;
import com.davidrdz.vitallog.ui.common.ViewModelFactory;
import com.davidrdz.vitallog.ui.widget.NumericStepperView;
import com.davidrdz.vitallog.ui.widget.VitalUnitLabels;

/**
 * Pantalla para registrar la presión arterial con dos controles +/− (número de arriba y de abajo).
 * Arranca en la última lectura guardada y confirma cada guardado con banner + tono.
 * La regla "arriba &gt; abajo" llega en el Hito 4.
 */
public class PressureEntryActivity extends BaseScreenActivity {

    private NumericStepperView systolicStepper;
    private NumericStepperView diastolicStepper;
    private SaveFeedback feedback;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        systolicStepper = findViewById(R.id.stepper_systolic);
        systolicStepper.bind(VitalRanges.SYSTOLIC);
        diastolicStepper = findViewById(R.id.stepper_diastolic);
        diastolicStepper.bind(VitalRanges.DIASTOLIC);
        feedback = SaveFeedback.attach(this, findViewById(R.id.feedback_banner));

        final PressureEntryViewModel viewModel =
                ViewModelFactory.get(this, PressureEntryViewModel.class);
        viewModel.getInitialReading().observe(this, event -> {
            final BloodPressureReading latest =
                    event == null ? null : event.getContentIfNotHandled();
            if (latest != null) {
                systolicStepper.setValue(latest.getSystolic());
                diastolicStepper.setValue(latest.getDiastolic());
            }
        });
        viewModel.getSaveEvents().observe(this, event -> {
            final SaveResult<BloodPressureReading> result = event.getContentIfNotHandled();
            if (result != null) {
                showSaved(result.getSaved());
            }
        });
        findViewById(R.id.btn_save_pressure).setOnClickListener(view -> viewModel.save(
                systolicStepper.getValue(), diastolicStepper.getValue()));
    }

    private void showSaved(@NonNull final BloodPressureReading reading) {
        final VitalLogApp app = VitalLogApp.from(this);
        final String when = app.getTimeFormatter()
                .formatDateTime(reading.getTakenAt(), app.getClock().millis());
        feedback.showSuccess(getString(R.string.saved_pressure, reading.getSystolic(),
                reading.getDiastolic(),
                getString(VitalUnitLabels.shortLabel(VitalRanges.SYSTOLIC.getUnit())), when));
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_pressure_entry;
    }

    @Override
    protected int getScreenTitleRes() {
        return R.string.title_pressure_entry;
    }
}
