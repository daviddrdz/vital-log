package com.davidrdz.vitallog.ui.widget;

/**
 * Estado de un control numérico +/− (Java puro). El valor siempre queda dentro de [min, max]:
 * cualquier intento de salir del rango se ajusta al límite más cercano.
 */
public final class StepperModel {

    private final int min;
    private final int max;
    private int value;

    /**
     * @param initialValue valor de arranque; si está fuera de rango se ajusta al límite.
     * @throws IllegalArgumentException si {@code min >= max}.
     */
    public StepperModel(final int min, final int max, final int initialValue) {
        if (min >= max) {
            throw new IllegalArgumentException("min must be lower than max");
        }
        this.min = min;
        this.max = max;
        this.value = clamp(initialValue);
    }

    public int getValue() {
        return value;
    }

    /** Fija el valor, ajustándolo al rango. Devuelve {@code true} si el valor cambió. */
    public boolean setValue(final int newValue) {
        return update(clamp(newValue));
    }

    /** Sube {@code step} unidades sin pasar de max. Devuelve {@code true} si el valor cambió. */
    public boolean increment(final int step) {
        requirePositive(step);
        return update(clamp((long) value + step));
    }

    /** Baja {@code step} unidades sin pasar de min. Devuelve {@code true} si el valor cambió. */
    public boolean decrement(final int step) {
        requirePositive(step);
        return update(clamp((long) value - step));
    }

    public boolean isAtMin() {
        return value == min;
    }

    public boolean isAtMax() {
        return value == max;
    }

    private boolean update(final int newValue) {
        final boolean changed = newValue != value;
        value = newValue;
        return changed;
    }

    private int clamp(final long candidate) {
        return (int) Math.max(min, Math.min(max, candidate));
    }

    private static void requirePositive(final int step) {
        if (step <= 0) {
            throw new IllegalArgumentException("step must be positive");
        }
    }
}
