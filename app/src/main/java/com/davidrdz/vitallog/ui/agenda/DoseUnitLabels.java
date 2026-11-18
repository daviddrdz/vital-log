package com.davidrdz.vitallog.ui.agenda;

import android.content.res.Resources;

import androidx.annotation.NonNull;
import androidx.annotation.PluralsRes;

import com.davidrdz.vitallog.R;
import com.davidrdz.vitallog.data.entity.DoseUnit;

/** Texto de cada {@link DoseUnit} según la cantidad: "1 tableta", "2 tabletas". */
public final class DoseUnitLabels {

    private DoseUnitLabels() {
    }

    @NonNull
    public static String forAmount(@NonNull final Resources resources, @NonNull final DoseUnit unit,
                            final int amount) {
        return resources.getQuantityString(plural(unit), amount);
    }

    @PluralsRes
    private static int plural(@NonNull final DoseUnit unit) {
        switch (unit) {
            case TABLET:
                return R.plurals.dose_unit_tablet;
            case CAPSULE:
                return R.plurals.dose_unit_capsule;
            case MILLILITER:
                return R.plurals.dose_unit_milliliter;
            case UNIT:
                return R.plurals.dose_unit_unit;
            default:
                throw new IllegalArgumentException("Unknown dose unit: " + unit);
        }
    }
}
