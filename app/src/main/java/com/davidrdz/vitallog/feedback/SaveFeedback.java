package com.davidrdz.vitallog.feedback;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;

/**
 * Fachada de retroalimentación al guardar (CLAUDE.md §3.4): siempre banner visible + tono, con un
 * banner y un tono distintos para el éxito y para el error.
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

    /** Banner de aviso con el motivo en lenguaje llano + tono de error (más grave y largo). */
    public void showError(@NonNull final CharSequence message) {
        banner.showError(message);
        tonePlayer.playError();
    }
}
