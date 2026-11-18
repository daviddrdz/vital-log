package com.davidrdz.vitallog.ui.home;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import com.davidrdz.vitallog.R;

/** Texto y pictograma del saludo para cada {@link DayPeriod}; siempre se muestran juntos. */
final class DayPeriodStyle {

    private DayPeriodStyle() {
    }

    @StringRes
    static int greeting(@NonNull final DayPeriod period) {
        switch (period) {
            case MORNING:
                return R.string.greeting_morning;
            case AFTERNOON:
                return R.string.greeting_afternoon;
            case NIGHT:
                return R.string.greeting_night;
            default:
                throw new IllegalArgumentException("Unknown day period: " + period);
        }
    }

    @DrawableRes
    static int icon(@NonNull final DayPeriod period) {
        switch (period) {
            case MORNING:
                return R.drawable.ic_greeting_morning;
            case AFTERNOON:
                return R.drawable.ic_greeting_afternoon;
            case NIGHT:
                return R.drawable.ic_greeting_night;
            default:
                throw new IllegalArgumentException("Unknown day period: " + period);
        }
    }
}
