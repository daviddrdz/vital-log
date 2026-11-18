package com.davidrdz.vitallog.ui.home;

import androidx.annotation.NonNull;

import java.time.LocalTime;

/**
 * Parte del día para el saludo del inicio (Java puro, sin Android): mañana de 5:00 a 11:59,
 * tarde de 12:00 a 18:59 y noche de 19:00 a 4:59. Solo cambia el texto y el pictograma del
 * saludo; la estructura de la pantalla es siempre la misma (docs/DESIGN_DIRECTION.md).
 */
public enum DayPeriod {
    MORNING,
    AFTERNOON,
    NIGHT;

    private static final LocalTime MORNING_FROM = LocalTime.of(5, 0);
    private static final LocalTime AFTERNOON_FROM = LocalTime.NOON;
    private static final LocalTime NIGHT_FROM = LocalTime.of(19, 0);

    @NonNull
    public static DayPeriod from(@NonNull final LocalTime time) {
        if (time.isBefore(MORNING_FROM) || !time.isBefore(NIGHT_FROM)) {
            return NIGHT;
        }
        return time.isBefore(AFTERNOON_FROM) ? MORNING : AFTERNOON;
    }
}
