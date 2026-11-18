package com.davidrdz.vitallog.ui.history;

import androidx.annotation.Nullable;

/**
 * Resumen del periodo elegido: promedio, mínimo y máximo de azúcar y de cada número de la presión,
 * y cuántas tomas se registraron. Una medida sin lecturas queda en {@code null} ("sin datos").
 */
public final class HistorySummary {

    /** Promedio (redondeado), mínimo y máximo de una medida. */
    public static final class Stats {
        private final int average;
        private final int min;
        private final int max;

        public Stats(final int average, final int min, final int max) {
            this.average = average;
            this.min = min;
            this.max = max;
        }

        public int getAverage() {
            return average;
        }

        public int getMin() {
            return min;
        }

        public int getMax() {
            return max;
        }
    }

    @Nullable
    private final Stats glucose;
    @Nullable
    private final Stats systolic;
    @Nullable
    private final Stats diastolic;
    private final int doseCount;

    HistorySummary(@Nullable final Stats glucose, @Nullable final Stats systolic,
                   @Nullable final Stats diastolic, final int doseCount) {
        this.glucose = glucose;
        this.systolic = systolic;
        this.diastolic = diastolic;
        this.doseCount = doseCount;
    }

    /** Azúcar en mg/dL, o {@code null} si no hay lecturas. */
    @Nullable
    public Stats getGlucose() {
        return glucose;
    }

    /** Número de arriba de la presión, o {@code null} si no hay lecturas. */
    @Nullable
    public Stats getSystolic() {
        return systolic;
    }

    /** Número de abajo de la presión, o {@code null} si no hay lecturas. */
    @Nullable
    public Stats getDiastolic() {
        return diastolic;
    }

    public int getDoseCount() {
        return doseCount;
    }

    /** {@code true} si en el periodo no hay ninguna lectura ni toma. */
    public boolean isEmpty() {
        return glucose == null && systolic == null && doseCount == 0;
    }
}
