package com.davidrdz.vitallog.data.repository;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.davidrdz.vitallog.analytics.ValidationError;

/**
 * Resultado de una escritura en un repositorio: la entidad tal como quedó guardada (con su id) o
 * el rechazo del Gestor Analítico con su {@link ValidationError} (en ese caso no se guardó nada).
 *
 * @param <T> tipo de la entidad guardada.
 */
public final class SaveResult<T> {

    /** Recibe el resultado en el hilo principal. */
    public interface Callback<T> {
        void onResult(@NonNull SaveResult<T> result);
    }

    @Nullable
    private final T saved;
    @Nullable
    private final ValidationError error;

    private SaveResult(@Nullable final T saved, @Nullable final ValidationError error) {
        this.saved = saved;
        this.error = error;
    }

    @NonNull
    public static <T> SaveResult<T> success(@NonNull final T saved) {
        return new SaveResult<>(saved, null);
    }

    @NonNull
    public static <T> SaveResult<T> rejected(@NonNull final ValidationError error) {
        return new SaveResult<>(null, error);
    }

    public boolean isSuccess() {
        return error == null;
    }

    /**
     * La entidad guardada, con el id asignado por la base de datos.
     *
     * @throws IllegalStateException si la lectura fue rechazada.
     */
    @NonNull
    public T getSaved() {
        if (saved == null) {
            throw new IllegalStateException("Rejected result has no saved entity: " + error);
        }
        return saved;
    }

    /** Motivo del rechazo, o {@code null} si se guardó. */
    @Nullable
    public ValidationError getError() {
        return error;
    }
}
