package com.davidrdz.vitallog.ui.common;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Valor de un solo consumo para {@code LiveData} (p. ej. "se guardó"): tras rotar la pantalla el
 * evento ya consumido no se vuelve a procesar.
 */
public final class Event<T> {

    @NonNull
    private final T content;
    private boolean handled;

    public Event(@NonNull final T content) {
        this.content = content;
    }

    /** Devuelve el contenido la primera vez; después, {@code null}. */
    @Nullable
    public T getContentIfNotHandled() {
        if (handled) {
            return null;
        }
        handled = true;
        return content;
    }
}
