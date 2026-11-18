package com.davidrdz.vitallog.data.repository;

import androidx.annotation.NonNull;

import com.davidrdz.vitallog.analytics.ValidationError;

import java.util.concurrent.Executor;

/**
 * Par de executors de un repositorio: uno para el trabajo con la base de datos y otro para
 * entregar el resultado en el hilo principal. En las pruebas JVM ambos son síncronos.
 */
public final class RepositoryExecutors {

    @NonNull
    private final Executor io;
    @NonNull
    private final Executor main;

    public RepositoryExecutors(@NonNull final Executor io, @NonNull final Executor main) {
        this.io = io;
        this.main = main;
    }

    /** Ejecuta {@code work} en el executor de E/S y entrega su resultado en el principal. */
    <T> void save(@NonNull final Work<T> work, @NonNull final SaveResult.Callback<T> callback) {
        io.execute(() -> {
            final SaveResult<T> result = SaveResult.success(work.run());
            main.execute(() -> callback.onResult(result));
        });
    }

    /** Entrega el rechazo en el hilo principal sin tocar la base de datos. */
    <T> void reject(@NonNull final ValidationError error,
                    @NonNull final SaveResult.Callback<T> callback) {
        final SaveResult<T> result = SaveResult.rejected(error);
        main.execute(() -> callback.onResult(result));
    }

    /** Escritura en la base de datos que devuelve la entidad guardada. */
    interface Work<T> {
        @NonNull
        T run();
    }
}
