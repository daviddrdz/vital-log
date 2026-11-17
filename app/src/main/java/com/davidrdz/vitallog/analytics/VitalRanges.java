package com.davidrdz.vitallog.analytics;

/**
 * Rangos biológicamente posibles de captura (CLAUDE.md §4.2). Fuente única de los límites:
 * los usan tanto los controles numéricos de la UI como el validador del Gestor Analítico.
 */
public final class VitalRanges {

    /** Glucosa en mg/dL: 20–600, arranca en 100. */
    public static final VitalRange GLUCOSE = new VitalRange(20, 600, 100, VitalUnit.MG_PER_DL);

    /** Presión sistólica ("número de arriba") en mmHg: 60–260, arranca en 120. */
    public static final VitalRange SYSTOLIC = new VitalRange(60, 260, 120, VitalUnit.MMHG);

    /** Presión diastólica ("número de abajo") en mmHg: 30–160, arranca en 80. */
    public static final VitalRange DIASTOLIC = new VitalRange(30, 160, 80, VitalUnit.MMHG);

    /** Diferencia mínima entre sistólica y diastólica (presión de pulso), en mmHg. */
    public static final int MIN_PULSE_PRESSURE = 10;

    private VitalRanges() {
    }
}
