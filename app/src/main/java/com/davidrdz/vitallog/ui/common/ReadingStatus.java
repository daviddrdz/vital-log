package com.davidrdz.vitallog.ui.common;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.davidrdz.vitallog.analytics.ReadingCategory;
import com.davidrdz.vitallog.analytics.ValidationError;

/**
 * Lo que se muestra debajo de los controles mientras el usuario ajusta el valor: la categoría
 * informativa de una lectura válida, o el motivo por el que la lectura no se podría guardar.
 */
public final class ReadingStatus {

    /** Qué se está midiendo; decide los textos de la categoría. */
    public enum Measure {
        GLUCOSE,
        BLOOD_PRESSURE
    }

    @NonNull
    private final Measure measure;
    @Nullable
    private final ReadingCategory category;
    @Nullable
    private final ValidationError problem;

    private ReadingStatus(@NonNull final Measure measure, @Nullable final ReadingCategory category,
                          @Nullable final ValidationError problem) {
        this.measure = measure;
        this.category = category;
        this.problem = problem;
    }

    @NonNull
    public static ReadingStatus of(@NonNull final Measure measure,
                                   @NonNull final ReadingCategory category) {
        return new ReadingStatus(measure, category, null);
    }

    @NonNull
    public static ReadingStatus problem(@NonNull final Measure measure,
                                        @NonNull final ValidationError problem) {
        return new ReadingStatus(measure, null, problem);
    }

    @NonNull
    public Measure getMeasure() {
        return measure;
    }

    /** Categoría de la lectura, o {@code null} si hay un {@link #getProblem() problema}. */
    @Nullable
    public ReadingCategory getCategory() {
        return category;
    }

    /** Motivo por el que no se podría guardar, o {@code null} si la lectura es válida. */
    @Nullable
    public ValidationError getProblem() {
        return problem;
    }
}
