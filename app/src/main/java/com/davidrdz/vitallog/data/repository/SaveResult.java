package com.davidrdz.vitallog.data.repository;

import androidx.annotation.NonNull;

/**
 * Resultado de una escritura en un repositorio: la entidad tal como quedó guardada (con su id).
 * En el Hito 4 se agrega el rechazo por validación del Gestor Analítico.
 *
 * @param <T> tipo de la entidad guardada.
 */
public final class SaveResult<T> {

    /** Recibe el resultado en el hilo principal. */
    public interface Callback<T> {
        void onResult(@NonNull SaveResult<T> result);
    }

    @NonNull
    private final T saved;

    private SaveResult(@NonNull final T saved) {
        this.saved = saved;
    }

    @NonNull
    public static <T> SaveResult<T> success(@NonNull final T saved) {
        return new SaveResult<>(saved);
    }

    /** La entidad guardada, con el id asignado por la base de datos. */
    @NonNull
    public T getSaved() {
        return saved;
    }
}
