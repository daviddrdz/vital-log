package com.davidrdz.vitallog.feedback;

import android.media.AudioManager;
import android.media.ToneGenerator;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

/**
 * Tonos cortos de confirmación (éxito) y de aviso (error) con {@link ToneGenerator}.
 * Se libera solo cuando se destruye la pantalla dueña.
 */
public final class FeedbackTonePlayer implements DefaultLifecycleObserver {

    private static final String TAG = "FeedbackTonePlayer";

    @Nullable
    private ToneGenerator toneGenerator;

    private FeedbackTonePlayer() {
        try {
            toneGenerator = new ToneGenerator(AudioManager.STREAM_NOTIFICATION,
                    ToneGenerator.MAX_VOLUME);
        } catch (RuntimeException e) {
            // Algunos equipos no tienen generador de tonos disponible: queda solo el aviso visual.
            Log.w(TAG, "ToneGenerator unavailable; visual feedback only", e);
            toneGenerator = null;
        }
    }

    /** Crea el reproductor y lo libera cuando {@code owner} se destruye. */
    @NonNull
    public static FeedbackTonePlayer attach(@NonNull final LifecycleOwner owner) {
        final FeedbackTonePlayer player = new FeedbackTonePlayer();
        owner.getLifecycle().addObserver(player);
        return player;
    }

    public void playSuccess() {
        play(ToneGenerator.TONE_PROP_ACK, FeedbackDurations.SUCCESS_TONE_MS);
    }

    public void playError() {
        play(ToneGenerator.TONE_PROP_NACK, FeedbackDurations.ERROR_TONE_MS);
    }

    private void play(final int tone, final int durationMs) {
        if (toneGenerator != null) {
            toneGenerator.startTone(tone, durationMs);
        }
    }

    @Override
    public void onDestroy(@NonNull final LifecycleOwner owner) {
        if (toneGenerator != null) {
            toneGenerator.release();
            toneGenerator = null;
        }
    }
}
