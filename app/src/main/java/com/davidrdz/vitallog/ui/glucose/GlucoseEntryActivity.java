package com.davidrdz.vitallog.ui.glucose;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.davidrdz.vitallog.R;
import com.davidrdz.vitallog.analytics.VitalRanges;
import com.davidrdz.vitallog.ui.common.BaseScreenActivity;
import com.davidrdz.vitallog.ui.widget.NumericStepperView;

/** Pantalla para registrar la glucosa con el control +/−. El guardado llega en el Hito 3. */
public class GlucoseEntryActivity extends BaseScreenActivity {

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final NumericStepperView glucose = findViewById(R.id.stepper_glucose);
        glucose.bind(VitalRanges.GLUCOSE);
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
