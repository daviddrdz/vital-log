package com.davidrdz.vitallog.ui.widget;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.davidrdz.vitallog.analytics.VitalRange;
import com.davidrdz.vitallog.analytics.VitalRanges;

import org.junit.Test;

public class StepperModelTest {

    private static StepperModel glucoseAt(final int value) {
        final VitalRange r = VitalRanges.GLUCOSE;
        return new StepperModel(r.getMin(), r.getMax(), value);
    }

    @Test
    public void initialValue_insideRange_isKept() {
        assertEquals(100, glucoseAt(100).getValue());
    }

    @Test
    public void initialValue_belowMin_isClampedToMin() {
        final StepperModel model = glucoseAt(0);
        assertEquals(20, model.getValue());
        assertTrue(model.isAtMin());
    }

    @Test
    public void initialValue_aboveMax_isClampedToMax() {
        final StepperModel model = glucoseAt(9_999);
        assertEquals(600, model.getValue());
        assertTrue(model.isAtMax());
    }

    @Test
    public void increment_addsStep() {
        final StepperModel model = glucoseAt(100);
        assertTrue(model.increment(5));
        assertEquals(105, model.getValue());
    }

    @Test
    public void decrement_subtractsStep() {
        final StepperModel model = glucoseAt(100);
        assertTrue(model.decrement(10));
        assertEquals(90, model.getValue());
    }

    @Test
    public void increment_neverPassesMax() {
        final StepperModel model = glucoseAt(598);
        assertTrue(model.increment(10));
        assertEquals(600, model.getValue());
        assertTrue(model.isAtMax());
        assertFalse(model.increment(1));
        assertEquals(600, model.getValue());
    }

    @Test
    public void decrement_neverPassesMin() {
        final StepperModel model = glucoseAt(23);
        assertTrue(model.decrement(10));
        assertEquals(20, model.getValue());
        assertTrue(model.isAtMin());
        assertFalse(model.decrement(1));
        assertEquals(20, model.getValue());
    }

    @Test
    public void hugeStep_doesNotOverflow() {
        final StepperModel model = glucoseAt(100);
        model.increment(Integer.MAX_VALUE);
        assertEquals(600, model.getValue());
        model.decrement(Integer.MAX_VALUE);
        assertEquals(20, model.getValue());
    }

    @Test
    public void manySteps_stayInsideRange() {
        final StepperModel model = glucoseAt(100);
        for (int i = 0; i < 1_000; i++) {
            model.increment(AccelerationPolicy.FAST_STEP);
            assertTrue(model.getValue() <= 600);
        }
        for (int i = 0; i < 1_000; i++) {
            model.decrement(AccelerationPolicy.FAST_STEP);
            assertTrue(model.getValue() >= 20);
        }
    }

    @Test
    public void setValue_outsideRange_isClamped() {
        final StepperModel model = glucoseAt(100);
        assertTrue(model.setValue(700));
        assertEquals(600, model.getValue());
        assertTrue(model.setValue(-5));
        assertEquals(20, model.getValue());
        assertFalse(model.setValue(20));
    }

    @Test
    public void limitFlags_areFalseInsideRange() {
        final StepperModel model = glucoseAt(300);
        assertFalse(model.isAtMin());
        assertFalse(model.isAtMax());
    }

    @Test(expected = IllegalArgumentException.class)
    public void zeroStep_isRejected() {
        glucoseAt(100).increment(0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void negativeStep_isRejected() {
        glucoseAt(100).decrement(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidBounds_areRejected() {
        new StepperModel(10, 10, 10);
    }
}
