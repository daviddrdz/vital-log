package com.davidrdz.vitallog.ui.widget;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class AccelerationPolicyTest {

    @Test
    public void firstTenRepeats_useStepOne() {
        final AccelerationPolicy policy = new AccelerationPolicy();
        for (int i = 0; i < 10; i++) {
            assertEquals("repeat " + i, 1, policy.nextStep());
        }
    }

    @Test
    public void stepChangesAtRepeatTen_andRepeatTwenty() {
        assertEquals(1, AccelerationPolicy.stepForRepeat(9));
        assertEquals(5, AccelerationPolicy.stepForRepeat(10));
        assertEquals(5, AccelerationPolicy.stepForRepeat(19));
        assertEquals(10, AccelerationPolicy.stepForRepeat(20));
    }

    @Test
    public void nextStep_followsOneFiveTenSequence() {
        final AccelerationPolicy policy = new AccelerationPolicy();
        for (int i = 0; i < 10; i++) {
            assertEquals(1, policy.nextStep());
        }
        for (int i = 0; i < 10; i++) {
            assertEquals(5, policy.nextStep());
        }
        for (int i = 0; i < 100; i++) {
            assertEquals(10, policy.nextStep());
        }
    }

    @Test
    public void reset_returnsToStepOne() {
        final AccelerationPolicy policy = new AccelerationPolicy();
        for (int i = 0; i < 25; i++) {
            policy.nextStep();
        }
        assertEquals(10, policy.nextStep());
        policy.reset();
        assertEquals(1, policy.nextStep());
    }

    @Test(expected = IllegalArgumentException.class)
    public void negativeRepeatIndex_isRejected() {
        AccelerationPolicy.stepForRepeat(-1);
    }
}
