package com.davidrdz.vitallog.feedback;

/** Duraciones y escalas de la retroalimentación visual y auditiva (CLAUDE.md §3.4). */
public final class FeedbackDurations {

    /** Tiempo que el banner de confirmación queda a la vista (≥ 2–3 s). */
    public static final long BANNER_VISIBLE_MS = 3000;
    /** Entrada del banner: crece de {@link #BANNER_START_SCALE} a su tamaño normal. */
    public static final long BANNER_ENTER_MS = 250;
    /** Aparición del check/aviso: crece desde cero con un pequeño rebote, tras el banner. */
    public static final long ICON_POP_MS = 350;
    /** Retraso del check respecto al banner, para que se note como un segundo paso. */
    public static final long ICON_POP_DELAY_MS = 100;
    /** Escala inicial del banner (la escala final siempre es 1: el texto queda nítido). */
    public static final float BANNER_START_SCALE = 0.9f;

    /** Tono de respaldo (ToneGenerator) de éxito. */
    public static final int SUCCESS_TONE_MS = 200;
    /** Tono de respaldo de error, más largo para distinguirlo del de éxito. */
    public static final int ERROR_TONE_MS = 400;

    private FeedbackDurations() {
    }
}
