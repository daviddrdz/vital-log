package com.davidrdz.vitallog.feedback;

/** Duraciones de la retroalimentación visual y auditiva (CLAUDE.md §3.4). */
public final class FeedbackDurations {

    /** Tiempo que el banner de confirmación queda a la vista (≥ 2–3 s). */
    public static final long BANNER_VISIBLE_MS = 3000;
    /** Tono corto de éxito. */
    public static final int SUCCESS_TONE_MS = 200;
    /** Tono de error, más largo para distinguirlo del de éxito. */
    public static final int ERROR_TONE_MS = 400;

    private FeedbackDurations() {
    }
}
