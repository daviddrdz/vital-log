package com.davidrdz.vitallog.analytics;

/**
 * Unidad de medida de un signo vital. El texto visible ("mg/dL", "mmHg") lo resuelve la capa de UI
 * desde strings.xml, para que el Gestor Analítico no dependa de Android.
 */
public enum VitalUnit {
    /** Miligramos por decilitro (glucosa). */
    MG_PER_DL,
    /** Milímetros de mercurio (presión arterial). */
    MMHG
}
