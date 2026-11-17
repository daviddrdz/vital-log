package com.davidrdz.vitallog.analytics;

/**
 * Rango biológicamente posible de un signo vital: límites de captura y valor típico de arranque.
 * Inmutable; las instancias oficiales viven en {@link VitalRanges}.
 */
public final class VitalRange {

    private final int min;
    private final int max;
    private final int typical;
    private final VitalUnit unit;

    /**
     * @throws IllegalArgumentException si {@code min >= max} o si {@code typical} no está
     *                                  estrictamente entre {@code min} y {@code max}.
     */
    public VitalRange(final int min, final int max, final int typical, final VitalUnit unit) {
        if (min >= max) {
            throw new IllegalArgumentException("min must be lower than max");
        }
        if (typical <= min || typical >= max) {
            throw new IllegalArgumentException("typical must lie strictly between min and max");
        }
        if (unit == null) {
            throw new IllegalArgumentException("unit is required");
        }
        this.min = min;
        this.max = max;
        this.typical = typical;
        this.unit = unit;
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }

    /** Valor con el que arranca el control cuando no hay una lectura previa. */
    public int getTypical() {
        return typical;
    }

    public VitalUnit getUnit() {
        return unit;
    }
}
