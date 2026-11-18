package com.davidrdz.vitallog.analytics;

/** Resultado de validar una lectura: válida, o rechazada con un {@link ValidationError}. */
public final class ValidationResult {

    private static final ValidationResult VALID = new ValidationResult(null);

    private final ValidationError error;

    private ValidationResult(final ValidationError error) {
        this.error = error;
    }

    public static ValidationResult valid() {
        return VALID;
    }

    /** @throws IllegalArgumentException si {@code error} es {@code null}. */
    public static ValidationResult rejected(final ValidationError error) {
        if (error == null) {
            throw new IllegalArgumentException("error is required");
        }
        return new ValidationResult(error);
    }

    public boolean isValid() {
        return error == null;
    }

    /** Motivo del rechazo, o {@code null} si la lectura es válida. */
    public ValidationError getError() {
        return error;
    }
}
