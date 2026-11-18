package com.davidrdz.vitallog.ui.widget;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import com.davidrdz.vitallog.R;
import com.davidrdz.vitallog.analytics.ReadingCategory;
import com.davidrdz.vitallog.ui.common.ReadingStatus;

/**
 * Icono, color y texto de cada {@link ReadingCategory} (CLAUDE.md §4.2): siempre se muestran
 * juntos, nunca solo el color. Lo usan el recuadro de captura y las filas del historial.
 */
public final class ReadingCategoryStyle {

    private ReadingCategoryStyle() {
    }

    @DrawableRes
    public static int icon(@NonNull final ReadingCategory category) {
        switch (category) {
            case LOW:
                return R.drawable.ic_arrow_down;
            case NORMAL:
                return R.drawable.ic_check_circle;
            case ELEVATED:
            case HIGH:
                return R.drawable.ic_arrow_up;
            case CRITICAL:
                return R.drawable.ic_warning;
            default:
                throw new IllegalArgumentException("Unknown category: " + category);
        }
    }

    @ColorRes
    public static int color(@NonNull final ReadingCategory category) {
        switch (category) {
            case NORMAL:
                return R.color.status_ok;
            case LOW:
            case ELEVATED:
                return R.color.status_warning;
            case HIGH:
            case CRITICAL:
                return R.color.status_danger;
            default:
                throw new IllegalArgumentException("Unknown category: " + category);
        }
    }

    @StringRes
    public static int label(@NonNull final ReadingStatus.Measure measure,
                            @NonNull final ReadingCategory category) {
        if (measure == ReadingStatus.Measure.GLUCOSE) {
            switch (category) {
                case LOW:
                    return R.string.category_glucose_low;
                case NORMAL:
                    return R.string.category_glucose_normal;
                case ELEVATED: // La glucosa no usa "elevada"; se muestra como alta.
                case HIGH:
                    return R.string.category_glucose_high;
                case CRITICAL:
                    return R.string.category_glucose_critical;
                default:
                    throw new IllegalArgumentException("Unknown category: " + category);
            }
        }
        switch (category) {
            case LOW:
                return R.string.category_pressure_low;
            case NORMAL:
                return R.string.category_pressure_normal;
            case ELEVATED:
                return R.string.category_pressure_elevated;
            case HIGH:
                return R.string.category_pressure_high;
            case CRITICAL:
                return R.string.category_pressure_critical;
            default:
                throw new IllegalArgumentException("Unknown category: " + category);
        }
    }
}
