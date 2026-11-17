package com.davidrdz.vitallog.analytics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class VitalRangesTest {

    @Test
    public void glucose_matchesClaudeMdLimits() {
        assertEquals(20, VitalRanges.GLUCOSE.getMin());
        assertEquals(600, VitalRanges.GLUCOSE.getMax());
        assertEquals(100, VitalRanges.GLUCOSE.getTypical());
        assertSame(VitalUnit.MG_PER_DL, VitalRanges.GLUCOSE.getUnit());
    }

    @Test
    public void systolic_matchesClaudeMdLimits() {
        assertEquals(60, VitalRanges.SYSTOLIC.getMin());
        assertEquals(260, VitalRanges.SYSTOLIC.getMax());
        assertEquals(120, VitalRanges.SYSTOLIC.getTypical());
        assertSame(VitalUnit.MMHG, VitalRanges.SYSTOLIC.getUnit());
    }

    @Test
    public void diastolic_matchesClaudeMdLimits() {
        assertEquals(30, VitalRanges.DIASTOLIC.getMin());
        assertEquals(160, VitalRanges.DIASTOLIC.getMax());
        assertEquals(80, VitalRanges.DIASTOLIC.getTypical());
        assertSame(VitalUnit.MMHG, VitalRanges.DIASTOLIC.getUnit());
    }

    @Test
    public void everyRange_hasMinBelowTypicalBelowMax() {
        for (final VitalRange range : new VitalRange[]{
                VitalRanges.GLUCOSE, VitalRanges.SYSTOLIC, VitalRanges.DIASTOLIC}) {
            assertTrue(range.getMin() < range.getTypical());
            assertTrue(range.getTypical() < range.getMax());
        }
    }

    @Test
    public void minPulsePressure_isTen() {
        assertEquals(10, VitalRanges.MIN_PULSE_PRESSURE);
    }

    @Test
    public void systolicRange_isCompatibleWithDiastolicPlusMinPulsePressure() {
        final VitalRange sys = VitalRanges.SYSTOLIC;
        final VitalRange dia = VitalRanges.DIASTOLIC;
        final int gap = VitalRanges.MIN_PULSE_PRESSURE;
        // La sistólica más baja posible deja sitio a una diastólica válida debajo.
        assertTrue(sys.getMin() >= dia.getMin() + gap);
        // La diastólica más alta posible deja sitio a una sistólica válida encima.
        assertTrue(dia.getMax() + gap <= sys.getMax());
        // Los valores típicos forman una lectura coherente.
        assertTrue(sys.getTypical() - dia.getTypical() >= gap);
    }

    @Test(expected = IllegalArgumentException.class)
    public void vitalRange_rejectsMinNotBelowMax() {
        new VitalRange(10, 10, 10, VitalUnit.MMHG);
    }

    @Test(expected = IllegalArgumentException.class)
    public void vitalRange_rejectsTypicalOnLimit() {
        new VitalRange(10, 20, 10, VitalUnit.MMHG);
    }

    @Test(expected = IllegalArgumentException.class)
    public void vitalRange_rejectsTypicalOutsideRange() {
        new VitalRange(10, 20, 21, VitalUnit.MMHG);
    }

    @Test(expected = IllegalArgumentException.class)
    public void vitalRange_rejectsMissingUnit() {
        new VitalRange(10, 20, 15, null);
    }
}
