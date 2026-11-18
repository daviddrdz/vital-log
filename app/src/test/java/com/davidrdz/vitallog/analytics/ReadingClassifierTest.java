package com.davidrdz.vitallog.analytics;

import static com.davidrdz.vitallog.analytics.ReadingClassifier.classifyBloodPressure;
import static com.davidrdz.vitallog.analytics.ReadingClassifier.classifyGlucose;
import static org.junit.Assert.assertSame;

import org.junit.Test;

/** Cada frontera de categoría, probada por ambos lados. */
public class ReadingClassifierTest {

    // ===== Glucosa: < 70 baja; 70–180 en rango; 181–249 alta; ≥ 250 muy alta =====

    @Test
    public void glucose_lowNormalBoundary() {
        assertSame(ReadingCategory.LOW, classifyGlucose(69));
        assertSame(ReadingCategory.NORMAL, classifyGlucose(70));
    }

    @Test
    public void glucose_normalHighBoundary() {
        assertSame(ReadingCategory.NORMAL, classifyGlucose(180));
        assertSame(ReadingCategory.HIGH, classifyGlucose(181));
    }

    @Test
    public void glucose_highCriticalBoundary() {
        assertSame(ReadingCategory.HIGH, classifyGlucose(249));
        assertSame(ReadingCategory.CRITICAL, classifyGlucose(250));
    }

    @Test
    public void glucose_captureLimits() {
        assertSame(ReadingCategory.LOW, classifyGlucose(VitalRanges.GLUCOSE.getMin()));
        assertSame(ReadingCategory.CRITICAL, classifyGlucose(VitalRanges.GLUCOSE.getMax()));
        assertSame(ReadingCategory.CRITICAL, classifyGlucose(320));
    }

    // ===== Presión: baja / normal / elevada / alta / muy alta =====

    @Test
    public void pressure_systolicLowBoundary() {
        assertSame(ReadingCategory.LOW, classifyBloodPressure(89, 70));
        assertSame(ReadingCategory.NORMAL, classifyBloodPressure(90, 70));
    }

    @Test
    public void pressure_diastolicLowBoundary() {
        assertSame(ReadingCategory.LOW, classifyBloodPressure(110, 59));
        assertSame(ReadingCategory.NORMAL, classifyBloodPressure(110, 60));
    }

    @Test
    public void pressure_normalElevatedBoundary() {
        assertSame(ReadingCategory.NORMAL, classifyBloodPressure(119, 79));
        assertSame(ReadingCategory.ELEVATED, classifyBloodPressure(120, 79));
    }

    @Test
    public void pressure_elevatedHighBoundaryOnSystolic() {
        assertSame(ReadingCategory.ELEVATED, classifyBloodPressure(129, 79));
        assertSame(ReadingCategory.HIGH, classifyBloodPressure(130, 79));
    }

    @Test
    public void pressure_highBoundaryOnDiastolic() {
        assertSame(ReadingCategory.NORMAL, classifyBloodPressure(110, 79));
        assertSame(ReadingCategory.HIGH, classifyBloodPressure(110, 80));
        assertSame(ReadingCategory.HIGH, classifyBloodPressure(125, 80));
    }

    @Test
    public void pressure_criticalBoundaryOnSystolic() {
        assertSame(ReadingCategory.HIGH, classifyBloodPressure(179, 90));
        assertSame(ReadingCategory.CRITICAL, classifyBloodPressure(180, 90));
    }

    @Test
    public void pressure_criticalBoundaryOnDiastolic() {
        assertSame(ReadingCategory.HIGH, classifyBloodPressure(150, 119));
        assertSame(ReadingCategory.CRITICAL, classifyBloodPressure(150, 120));
    }

    @Test
    public void pressure_mostWorryingNumberWins() {
        assertSame(ReadingCategory.HIGH, classifyBloodPressure(150, 55));
        assertSame(ReadingCategory.CRITICAL, classifyBloodPressure(200, 50));
    }
}
