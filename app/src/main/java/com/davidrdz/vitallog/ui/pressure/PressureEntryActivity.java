package com.davidrdz.vitallog.ui.pressure;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.davidrdz.vitallog.R;
import com.davidrdz.vitallog.analytics.VitalRanges;
import com.davidrdz.vitallog.ui.common.BaseScreenActivity;
import com.davidrdz.vitallog.ui.widget.NumericStepperView;

/**
 * Pantalla para registrar la presión arterial con dos controles +/− (número de arriba y de abajo).
 * El guardado llega en el Hito 3 y la regla "arriba &gt; abajo" en el Hito 4.
 */
public class PressureEntryActivity extends BaseScreenActivity {

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final NumericStepperView systolic = findViewById(R.id.stepper_systolic);
        systolic.bind(VitalRanges.SYSTOLIC);
        final NumericStepperView diastolic = findViewById(R.id.stepper_diastolic);
        diastolic.bind(VitalRanges.DIASTOLIC);
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
