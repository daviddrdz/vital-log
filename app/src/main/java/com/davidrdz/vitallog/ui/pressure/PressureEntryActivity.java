package com.davidrdz.vitallog.ui.pressure;

import com.davidrdz.vitallog.R;
import com.davidrdz.vitallog.ui.common.BaseScreenActivity;

/** Pantalla para registrar la presión arterial. En el Hito 2 recibe los dos controles +/−. */
public class PressureEntryActivity extends BaseScreenActivity {

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_pressure_entry;
    }

    @Override
    protected int getScreenTitleRes() {
        return R.string.title_pressure_entry;
    }
}
