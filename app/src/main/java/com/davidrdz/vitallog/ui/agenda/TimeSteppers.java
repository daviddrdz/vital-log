package com.davidrdz.vitallog.ui.agenda;

import android.content.res.Resources;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.davidrdz.vitallog.R;
import com.davidrdz.vitallog.ui.common.SpokenTimeFormatter;
import com.davidrdz.vitallog.ui.widget.NumericStepperView;

/**
 * Maneja {@code view_time_steppers.xml}: hora en formato de 12 horas con su periodo del día
 * ("8" + "de la mañana") y minutos de 5 en 5, con la hora completa escrita debajo.
 */
final class TimeSteppers {

    private static final int MIN_HOUR = 0;
    private static final int MAX_HOUR = 23;
    private static final int MIN_MINUTE = 0;
    /** Último minuto alcanzable de 5 en 5. */
    private static final int MAX_MINUTE = 55;
    private static final int HOUR_STEP = 1;
    private static final int MINUTE_STEP = 5;

    private final NumericStepperView hourStepper;
    private final NumericStepperView minuteStepper;
    private final TextView summary;
    private final SpokenTimeFormatter formatter;
    private final Runnable onTimeChanged;

    /**
     * @param onTimeChanged se llama cada vez que cambia la hora o los minutos.
     */
    TimeSteppers(@NonNull final NumericStepperView hourStepper,
                 @NonNull final NumericStepperView minuteStepper,
                 @NonNull final TextView summary, @NonNull final SpokenTimeFormatter formatter,
                 final int initialHour, final int initialMinute,
                 @NonNull final Runnable onTimeChanged) {
        this.hourStepper = hourStepper;
        this.minuteStepper = minuteStepper;
        this.summary = summary;
        this.formatter = formatter;
        this.onTimeChanged = onTimeChanged;

        final Resources resources = summary.getResources();
        // Los minutos van primero: el periodo que muestra la hora depende de ellos (12:00 mediodía).
        minuteStepper.bind(MIN_MINUTE, MAX_MINUTE, initialMinute, MINUTE_STEP,
                new MinuteFormatter(resources));
        hourStepper.bind(MIN_HOUR, MAX_HOUR, initialHour, HOUR_STEP, new HourFormatter(resources));
        minuteStepper.setOnValueChangeListener(value -> {
            hourStepper.refresh();
            onChanged();
        });
        hourStepper.setOnValueChangeListener(value -> onChanged());
        renderSummary();
    }

    int getHour() {
        return hourStepper.getValue();
    }

    int getMinute() {
        return minuteStepper.getValue();
    }

    private void onChanged() {
        renderSummary();
        onTimeChanged.run();
    }

    private void renderSummary() {
        summary.setText(summary.getResources().getString(R.string.time_summary,
                formatter.formatTime(getHour(), getMinute())));
    }

    /** "8" con "de la mañana" debajo. */
    private final class HourFormatter implements NumericStepperView.ValueFormatter {

        private final Resources resources;

        HourFormatter(@NonNull final Resources resources) {
            this.resources = resources;
        }

        @NonNull
        @Override
        public CharSequence formatValue(final int value) {
            return resources.getString(R.string.stepper_value,
                    SpokenTimeFormatter.toTwelveHour(value));
        }

        @NonNull
        @Override
        public CharSequence formatUnit(final int value) {
            return formatter.dayPeriod(value, minuteStepper.getValue());
        }

        @NonNull
        @Override
        public CharSequence formatSpokenUnit(final int value) {
            return formatUnit(value);
        }
    }

    /** "05" con "minutos" debajo. */
    private static final class MinuteFormatter implements NumericStepperView.ValueFormatter {

        private final Resources resources;

        MinuteFormatter(@NonNull final Resources resources) {
            this.resources = resources;
        }

        @NonNull
        @Override
        public CharSequence formatValue(final int value) {
            return resources.getString(R.string.minute_value, value);
        }

        @NonNull
        @Override
        public CharSequence formatUnit(final int value) {
            return resources.getQuantityString(R.plurals.unit_minutes, value);
        }

        @NonNull
        @Override
        public CharSequence formatSpokenUnit(final int value) {
            return formatUnit(value);
        }
    }
}
