package com.davidrdz.vitallog.feedback;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.media.ToneGenerator;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RawRes;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

import com.davidrdz.vitallog.R;

import java.util.HashSet;
import java.util.Set;

/**
 * Sonidos cortos de confirmación (dos notas ascendentes) y de aviso (una nota grave) con
 * {@link SoundPool}. Los {@code .ogg} de {@code res/raw/} se sintetizaron para el proyecto.
 *
 * <p>Si un sonido aún no cargó o no se pudo cargar, suena el tono equivalente de
 * {@link ToneGenerator}, para que el guardado nunca quede sin aviso auditivo. Todo se libera
 * cuando se destruye la pantalla dueña.
 */
public final class FeedbackTonePlayer implements DefaultLifecycleObserver {

    private static final String TAG = "FeedbackTonePlayer";
    private static final int MAX_STREAMS = 2;
    private static final int LOAD_PRIORITY = 1;
    private static final int PLAY_PRIORITY = 1;
    private static final int NO_LOOP = 0;
    private static final float FULL_VOLUME = 1f;
    private static final float NORMAL_RATE = 1f;
    private static final int LOAD_OK = 0;

    @Nullable
    private SoundPool soundPool;
    private final int successSoundId;
    private final int errorSoundId;
    /** Ids de los sonidos que terminaron de cargar bien (solo se toca en el hilo principal). */
    private final Set<Integer> loadedSounds = new HashSet<>();
    @Nullable
    private ToneGenerator fallbackTones;

    private FeedbackTonePlayer(@NonNull final Context context) {
        final AudioAttributes attributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        soundPool = new SoundPool.Builder()
                .setMaxStreams(MAX_STREAMS)
                .setAudioAttributes(attributes)
                .build();
        soundPool.setOnLoadCompleteListener((pool, soundId, status) -> {
            if (status == LOAD_OK) {
                loadedSounds.add(soundId);
            } else {
                Log.w(TAG, "Sound " + soundId + " failed to load; using ToneGenerator");
            }
        });
        successSoundId = load(context, R.raw.feedback_success);
        errorSoundId = load(context, R.raw.feedback_error);
    }

    /** Crea el reproductor y lo libera cuando {@code owner} se destruye. */
    @NonNull
    public static FeedbackTonePlayer attach(@NonNull final Context context,
                                            @NonNull final LifecycleOwner owner) {
        final FeedbackTonePlayer player = new FeedbackTonePlayer(context);
        owner.getLifecycle().addObserver(player);
        return player;
    }

    public void playSuccess() {
        play(successSoundId, ToneGenerator.TONE_PROP_ACK, FeedbackDurations.SUCCESS_TONE_MS);
    }

    public void playError() {
        play(errorSoundId, ToneGenerator.TONE_PROP_NACK, FeedbackDurations.ERROR_TONE_MS);
    }

    private int load(@NonNull final Context context, @RawRes final int sound) {
        return soundPool == null ? 0 : soundPool.load(context, sound, LOAD_PRIORITY);
    }

    private void play(final int soundId, final int fallbackTone, final int fallbackMs) {
        if (soundPool != null && loadedSounds.contains(soundId)) {
            soundPool.play(soundId, FULL_VOLUME, FULL_VOLUME, PLAY_PRIORITY, NO_LOOP, NORMAL_RATE);
            return;
        }
        final ToneGenerator tones = fallbackTones();
        if (tones != null) {
            tones.startTone(fallbackTone, fallbackMs);
        }
    }

    /** Generador de tonos de respaldo; se crea la primera vez que hace falta. */
    @Nullable
    private ToneGenerator fallbackTones() {
        if (fallbackTones == null) {
            try {
                fallbackTones = new ToneGenerator(AudioManager.STREAM_NOTIFICATION,
                        ToneGenerator.MAX_VOLUME);
            } catch (RuntimeException e) {
                // Algunos equipos no tienen generador de tonos disponible: queda el aviso visual.
                Log.w(TAG, "ToneGenerator unavailable; visual feedback only", e);
            }
        }
        return fallbackTones;
    }

    @Override
    public void onDestroy(@NonNull final LifecycleOwner owner) {
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
        loadedSounds.clear();
        if (fallbackTones != null) {
            fallbackTones.release();
            fallbackTones = null;
        }
    }
}
