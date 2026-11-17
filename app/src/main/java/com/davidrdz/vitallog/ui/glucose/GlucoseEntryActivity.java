package com.davidrdz.vitallog.ui.glucose;

import com.davidrdz.vitallog.R;
import com.davidrdz.vitallog.ui.common.BaseScreenActivity;

/** Pantalla para registrar la glucosa. En el Hito 2 recibe el control numérico +/−. */
public class GlucoseEntryActivity extends BaseScreenActivity {

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_glucose_entry;
    }

    @Override
    protected int getScreenTitleRes() {
        return R.string.title_glucose_entry;
    }
}
