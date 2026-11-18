package com.davidrdz.vitallog.analytics;

/**
 * Motivo por el que el Gestor Analítico rechaza una lectura imposible (CLAUDE.md §4.2).
 * Es solo un código: el texto para el usuario lo resuelve la capa de UI desde strings.xml.
 */
public enum ValidationError {
    GLUCOSE_TOO_LOW,
    GLUCOSE_TOO_HIGH,
    SYSTOLIC_TOO_LOW,
    SYSTOLIC_TOO_HIGH,
    DIASTOLIC_TOO_LOW,
    DIASTOLIC_TOO_HIGH,
    /** El número de arriba es igual o menor que el de abajo. */
    SYSTOLIC_NOT_ABOVE_DIASTOLIC,
    /** La diferencia entre ambos números es menor que {@link VitalRanges#MIN_PULSE_PRESSURE}. */
    PULSE_PRESSURE_TOO_NARROW
}
