package com.davidrdz.vitallog.analytics;

/**
 * Categoría informativa (no diagnóstica) de una lectura válida. La UI la muestra siempre con
 * texto + icono + color, nunca solo con color.
 */
public enum ReadingCategory {
    LOW,
    NORMAL,
    /** Solo presión arterial: sistólica 120–129 con diastólica &lt; 80. */
    ELEVATED,
    HIGH,
    /** Muy alta: la UI muestra un aviso en lenguaje llano, pero el valor se puede guardar. */
    CRITICAL
}
