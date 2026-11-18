package com.davidrdz.vitallog.feedback;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class FeedbackDurationsTest {

    /** CLAUDE.md §3.4: el banner queda visible al menos 2–3 s. */
    private static final long MIN_BANNER_VISIBLE_MS = 2500;

    @Test
    public void banner_staysVisibleAtLeastTwoAndAHalfSeconds() {
        assertTrue(FeedbackDurations.BANNER_VISIBLE_MS >= MIN_BANNER_VISIBLE_MS);
    }

    @Test
    public void entranceAnimation_endsWellBeforeBannerHides() {
        final long iconEnds = FeedbackDurations.ICON_POP_DELAY_MS + FeedbackDurations.ICON_POP_MS;
        assertTrue(FeedbackDurations.BANNER_ENTER_MS < FeedbackDurations.BANNER_VISIBLE_MS);
        assertTrue(iconEnds * 2 < FeedbackDurations.BANNER_VISIBLE_MS);
    }

    @Test
    public void entranceAnimation_onlyScalesUpToFullSize() {
        assertTrue(FeedbackDurations.BANNER_START_SCALE > 0f);
        assertTrue(FeedbackDurations.BANNER_START_SCALE < 1f);
    }

    @Test
    public void errorFallbackTone_isLongerThanSuccess() {
        assertTrue(FeedbackDurations.ERROR_TONE_MS > FeedbackDurations.SUCCESS_TONE_MS);
    }
}
