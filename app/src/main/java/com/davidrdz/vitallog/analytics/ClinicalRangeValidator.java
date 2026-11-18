package com.davidrdz.vitallog.analytics;

/**
 * Valida que una lectura sea biológicamente posible antes de guardarla (CLAUDE.md §4.2).
 * Solo rechaza valores imposibles o incoherentes; un valor posible pero alarmante es válido
 * (la advertencia la da {@link ReadingClassifier}).
 */
public final class ClinicalRangeValidator {

    private ClinicalRangeValidator() {
    }

    public static ValidationResult validateGlucose(final int valueMgDl) {
        if (valueMgDl < VitalRanges.GLUCOSE.getMin()) {
            return ValidationResult.rejected(ValidationError.GLUCOSE_TOO_LOW);
        }
        if (valueMgDl > VitalRanges.GLUCOSE.getMax()) {
            return ValidationResult.rejected(ValidationError.GLUCOSE_TOO_HIGH);
        }
        return ValidationResult.valid();
    }

    /**
     * Revisa, en este orden: rango de la sistólica, rango de la diastólica, sistólica mayor que
     * diastólica y presión de pulso mínima. Devuelve el primer motivo de rechazo encontrado.
     */
    public static ValidationResult validateBloodPressure(final int systolic, final int diastolic) {
        if (systolic < VitalRanges.SYSTOLIC.getMin()) {
            return ValidationResult.rejected(ValidationError.SYSTOLIC_TOO_LOW);
        }
        if (systolic > VitalRanges.SYSTOLIC.getMax()) {
            return ValidationResult.rejected(ValidationError.SYSTOLIC_TOO_HIGH);
        }
        if (diastolic < VitalRanges.DIASTOLIC.getMin()) {
            return ValidationResult.rejected(ValidationError.DIASTOLIC_TOO_LOW);
        }
        if (diastolic > VitalRanges.DIASTOLIC.getMax()) {
            return ValidationResult.rejected(ValidationError.DIASTOLIC_TOO_HIGH);
        }
        if (systolic <= diastolic) {
            return ValidationResult.rejected(ValidationError.SYSTOLIC_NOT_ABOVE_DIASTOLIC);
        }
        if (systolic - diastolic < VitalRanges.MIN_PULSE_PRESSURE) {
            return ValidationResult.rejected(ValidationError.PULSE_PRESSURE_TOO_NARROW);
        }
        return ValidationResult.valid();
    }
}
