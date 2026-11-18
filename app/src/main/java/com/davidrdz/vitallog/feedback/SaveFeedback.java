package com.davidrdz.vitallog.feedback;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;

/**
 * Fachada de retroalimentación al guardar (CLAUDE.md §3.4): siempre banner visible + tono.
 * En el Hito 4 se agrega {@code showError}.
 */
public final class SaveFeedback {

    @NonNull
    private final FeedbackBannerView banner;
    @NonNull
    private final FeedbackTonePlayer tonePlayer;

    private SaveFeedback(@NonNull final FeedbackBannerView banner,
                         @NonNull final FeedbackTonePlayer tonePlayer) {
        this.banner = banner;
        this.tonePlayer = tonePlayer;
    }

    /** Une la retroalimentación al banner de la pantalla; el tono se libera con {@code owner}. */
    @NonNull
    public static SaveFeedback attach(@NonNull final LifecycleOwner owner,
                                      @NonNull final FeedbackBannerView banner) {
        return new SaveFeedback(banner, FeedbackTonePlayer.attach(owner));
    }

    /** Banner con el mensaje de lo guardado + tono de éxito. */
    public void showSuccess(@NonNull final CharSequence message) {
        banner.showSuccess(message);
        tonePlayer.playSuccess();
    }
}
