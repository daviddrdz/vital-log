package com.davidrdz.vitallog.ui.widget;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;

import com.davidrdz.vitallog.R;

/**
 * Color de los botones Subir/Bajar según la pantalla (atributo {@code app:stepperAccent}):
 * glucosa en cobalto, presión en ciruela y el resto (dosis, hora, fecha) en cacao sobre arena.
 * Solo es apariencia: el texto "Subir"/"Bajar" y el signo dicen siempre qué hace cada botón.
 */
enum StepperAccent {
    NEUTRAL(R.color.brand_primary_container, R.color.brand_primary),
    GLUCOSE(R.color.vital_glucose_container, R.color.vital_glucose),
    PRESSURE(R.color.vital_pressure_container, R.color.vital_pressure);

    /** Relleno del botón en estado normal. */
    @ColorRes
    final int container;
    /** Borde y signo del botón en estado normal. */
    @ColorRes
    final int accent;

    StepperAccent(@ColorRes final int container, @ColorRes final int accent) {
        this.container = container;
        this.accent = accent;
    }

    /** Valor del enum {@code stepperAccent} de attrs.xml (0, 1, 2) → acento. */
    @NonNull
    static StepperAccent fromAttr(final int value) {
        final StepperAccent[] values = values();
        if (value < 0 || value >= values.length) {
            throw new IllegalArgumentException("Unknown stepperAccent: " + value);
        }
        return values[value];
    }
}
