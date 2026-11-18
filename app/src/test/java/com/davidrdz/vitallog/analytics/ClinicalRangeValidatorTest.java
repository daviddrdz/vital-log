package com.davidrdz.vitallog.analytics;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ClinicalRangeValidatorTest {

    private static final int VALID_SYSTOLIC = 120;
    private static final int VALID_DIASTOLIC = 80;

    // ===== Glucosa: 19 ✗ / 20 ✓ / 600 ✓ / 601 ✗ =====

    @Test
    public void glucose_belowMin_isRejectedAsTooLow() {
        assertRejected(ValidationError.GLUCOSE_TOO_LOW, ClinicalRangeValidator.validateGlucose(19));
    }

    @Test
    public void glucose_atMin_isValid() {
        assertValid(ClinicalRangeValidator.validateGlucose(20));
    }

    @Test
    public void glucose_atMax_isValid() {
        assertValid(ClinicalRangeValidator.validateGlucose(600));
    }

    @Test
    public void glucose_aboveMax_isRejectedAsTooHigh() {
        assertRejected(ValidationError.GLUCOSE_TOO_HIGH,
                ClinicalRangeValidator.validateGlucose(601));
    }

    @Test
    public void glucose_alarmingButPossible_isNeverBlocked() {
        assertValid(ClinicalRangeValidator.validateGlucose(320));
        assertValid(ClinicalRangeValidator.validateGlucose(40));
    }

    // ===== Sistólica: 59 ✗ / 60 ✓ / 260 ✓ / 261 ✗ =====

    @Test
    public void systolic_belowMin_isRejectedAsTooLow() {
        assertRejected(ValidationError.SYSTOLIC_TOO_LOW,
                ClinicalRangeValidator.validateBloodPressure(59, 40));
    }

    @Test
    public void systolic_atMin_isValid() {
        assertValid(ClinicalRangeValidator.validateBloodPressure(60, 40));
    }

    @Test
    public void systolic_atMax_isValid() {
        assertValid(ClinicalRangeValidator.validateBloodPressure(260, VALID_DIASTOLIC));
    }

    @Test
    public void systolic_aboveMax_isRejectedAsTooHigh() {
        assertRejected(ValidationError.SYSTOLIC_TOO_HIGH,
                ClinicalRangeValidator.validateBloodPressure(261, VALID_DIASTOLIC));
    }

    // ===== Diastólica: 29 ✗ / 30 ✓ / 160 ✓ / 161 ✗ =====

    @Test
    public void diastolic_belowMin_isRejectedAsTooLow() {
        assertRejected(ValidationError.DIASTOLIC_TOO_LOW,
                ClinicalRangeValidator.validateBloodPressure(VALID_SYSTOLIC, 29));
    }

    @Test
    public void diastolic_atMin_isValid() {
        assertValid(ClinicalRangeValidator.validateBloodPressure(VALID_SYSTOLIC, 30));
    }

    @Test
    public void diastolic_atMax_isValid() {
        assertValid(ClinicalRangeValidator.validateBloodPressure(200, 160));
    }

    @Test
    public void diastolic_aboveMax_isRejectedAsTooHigh() {
        assertRejected(ValidationError.DIASTOLIC_TOO_HIGH,
                ClinicalRangeValidator.validateBloodPressure(200, 161));
    }

    // ===== Coherencia entre sistólica y diastólica =====

    @Test
    public void systolicEqualToDiastolic_isRejected() {
        assertRejected(ValidationError.SYSTOLIC_NOT_ABOVE_DIASTOLIC,
                ClinicalRangeValidator.validateBloodPressure(90, 90));
    }

    @Test
    public void systolicBelowDiastolic_isRejected() {
        assertRejected(ValidationError.SYSTOLIC_NOT_ABOVE_DIASTOLIC,
                ClinicalRangeValidator.validateBloodPressure(80, 90));
    }

    // ===== Presión de pulso: 9 ✗ / 10 ✓ =====

    @Test
    public void pulsePressureOf9_isRejectedAsTooNarrow() {
        assertRejected(ValidationError.PULSE_PRESSURE_TOO_NARROW,
                ClinicalRangeValidator.validateBloodPressure(90, 81));
    }

    @Test
    public void pulsePressureOf10_isValid() {
        assertValid(ClinicalRangeValidator.validateBloodPressure(90, 80));
    }

    @Test
    public void manualVerificationCase_90over85_isRejected() {
        assertRejected(ValidationError.PULSE_PRESSURE_TOO_NARROW,
                ClinicalRangeValidator.validateBloodPressure(90, 85));
    }

    @Test
    public void rangeErrorsTakePriorityOverCoherence() {
        assertRejected(ValidationError.SYSTOLIC_TOO_LOW,
                ClinicalRangeValidator.validateBloodPressure(50, 100));
    }

    private static void assertValid(final ValidationResult result) {
        assertTrue("Debe ser válida, error = " + result.getError(), result.isValid());
        assertNull(result.getError());
    }

    private static void assertRejected(final ValidationError expected,
                                       final ValidationResult result) {
        assertTrue("Debe rechazarse", !result.isValid());
        assertSame(expected, result.getError());
    }
}
