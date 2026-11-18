package com.davidrdz.vitallog.analytics;

/**
 * Clasificación informativa de lecturas válidas (CLAUDE.md §4.2). Los umbrales son
 * <b>orientativos</b>, tomados de guías generales para adultos; no sustituyen el criterio del
 * médico ni se usan para dar consejos o dosis.
 */
public final class ReadingClassifier {

    /** Glucosa por debajo de este valor (mg/dL): baja. */
    public static final int GLUCOSE_LOW_BELOW = 70;
    /** Glucosa hasta este valor inclusive (mg/dL): en rango. */
    public static final int GLUCOSE_NORMAL_MAX = 180;
    /** Glucosa desde este valor (mg/dL): muy alta. Entre el rango y este valor: alta. */
    public static final int GLUCOSE_CRITICAL_FROM = 250;

    /** Sistólica por debajo de este valor (mmHg): baja. */
    public static final int SYSTOLIC_LOW_BELOW = 90;
    /** Diastólica por debajo de este valor (mmHg): baja. */
    public static final int DIASTOLIC_LOW_BELOW = 60;
    /** Sistólica desde este valor (mmHg), con diastólica normal: elevada. */
    public static final int SYSTOLIC_ELEVATED_FROM = 120;
    /** Sistólica desde este valor (mmHg): alta. */
    public static final int SYSTOLIC_HIGH_FROM = 130;
    /** Diastólica desde este valor (mmHg): alta. */
    public static final int DIASTOLIC_HIGH_FROM = 80;
    /** Sistólica desde este valor (mmHg): muy alta. */
    public static final int SYSTOLIC_CRITICAL_FROM = 180;
    /** Diastólica desde este valor (mmHg): muy alta. */
    public static final int DIASTOLIC_CRITICAL_FROM = 120;

    private ReadingClassifier() {
    }

    /** &lt; 70 baja; 70–180 en rango; 181–249 alta; ≥ 250 muy alta. */
    public static ReadingCategory classifyGlucose(final int valueMgDl) {
        if (valueMgDl < GLUCOSE_LOW_BELOW) {
            return ReadingCategory.LOW;
        }
        if (valueMgDl <= GLUCOSE_NORMAL_MAX) {
            return ReadingCategory.NORMAL;
        }
        if (valueMgDl < GLUCOSE_CRITICAL_FROM) {
            return ReadingCategory.HIGH;
        }
        return ReadingCategory.CRITICAL;
    }

    /**
     * Se evalúa de la categoría más grave a la más leve, así que el número más preocupante manda
     * (p. ej. 150/55 es alta, no baja): ≥ 180 o ≥ 120 muy alta; ≥ 130 o ≥ 80 alta; 120–129 con
     * &lt; 80 elevada; &lt; 90 o &lt; 60 baja; el resto, normal.
     */
    public static ReadingCategory classifyBloodPressure(final int systolic, final int diastolic) {
        if (systolic >= SYSTOLIC_CRITICAL_FROM || diastolic >= DIASTOLIC_CRITICAL_FROM) {
            return ReadingCategory.CRITICAL;
        }
        if (systolic >= SYSTOLIC_HIGH_FROM || diastolic >= DIASTOLIC_HIGH_FROM) {
            return ReadingCategory.HIGH;
        }
        if (systolic >= SYSTOLIC_ELEVATED_FROM) {
            return ReadingCategory.ELEVATED;
        }
        if (systolic < SYSTOLIC_LOW_BELOW || diastolic < DIASTOLIC_LOW_BELOW) {
            return ReadingCategory.LOW;
        }
        return ReadingCategory.NORMAL;
    }
}
