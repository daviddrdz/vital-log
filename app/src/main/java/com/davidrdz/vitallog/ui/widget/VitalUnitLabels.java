package com.davidrdz.vitallog.ui.widget;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import com.davidrdz.vitallog.R;
import com.davidrdz.vitallog.analytics.VitalUnit;

/** Texto visible ("mg/dL") y hablado ("miligramos por decilitro") de cada {@link VitalUnit}. */
public final class VitalUnitLabels {

    private VitalUnitLabels() {
    }

    @StringRes
    public static int shortLabel(@NonNull final VitalUnit unit) {
        switch (unit) {
            case MG_PER_DL:
                return R.string.unit_mg_dl;
            case MMHG:
                return R.string.unit_mmhg;
            default:
                throw new IllegalArgumentException("Unknown unit: " + unit);
        }
    }

    @StringRes
    public static int spokenLabel(@NonNull final VitalUnit unit) {
        switch (unit) {
            case MG_PER_DL:
                return R.string.unit_mg_dl_spoken;
            case MMHG:
                return R.string.unit_mmhg_spoken;
            default:
                throw new IllegalArgumentException("Unknown unit: " + unit);
        }
    }
}
