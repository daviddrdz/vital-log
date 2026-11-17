package com.davidrdz.vitallog.ui.widget;

/**
 * Aceleración al mantener presionado + o − (Java puro, CLAUDE.md §3.1): el paso crece con las
 * repeticiones para no obligar a cientos de toques.
 *
 * <ul>
 *   <li>Repeticiones 0–9: paso {@value #SLOW_STEP}.</li>
 *   <li>Repeticiones 10–19: paso {@value #MEDIUM_STEP}.</li>
 *   <li>Repetición 20 en adelante: paso {@value #FAST_STEP}.</li>
 * </ul>
 * Al soltar el botón se llama a {@link #reset()} y se vuelve a empezar con paso 1.
 */
public final class AccelerationPolicy {

    public static final int SLOW_STEP = 1;
    public static final int MEDIUM_STEP = 5;
    public static final int FAST_STEP = 10;

    /** Índice (desde 0) de la primera repetición con paso medio. */
    public static final int MEDIUM_FROM_REPEAT = 10;
    /** Índice (desde 0) de la primera repetición con paso rápido. */
    public static final int FAST_FROM_REPEAT = 20;

    private int repeatCount;

    /** Paso que corresponde a la repetición {@code repeatIndex} (empezando en 0). */
    public static int stepForRepeat(final int repeatIndex) {
        if (repeatIndex < 0) {
            throw new IllegalArgumentException("repeatIndex must not be negative");
        }
        if (repeatIndex >= FAST_FROM_REPEAT) {
            return FAST_STEP;
        }
        if (repeatIndex >= MEDIUM_FROM_REPEAT) {
            return MEDIUM_STEP;
        }
        return SLOW_STEP;
    }

    /** Devuelve el paso de la siguiente repetición y avanza el contador. */
    public int nextStep() {
        final int step = stepForRepeat(repeatCount);
        if (repeatCount < FAST_FROM_REPEAT) {
            repeatCount++;
        }
        return step;
    }

    /** Vuelve a paso 1; se llama al soltar el botón. */
    public void reset() {
        repeatCount = 0;
    }
}
