package com.davidrdz.vitallog.ui.common;

import android.content.res.Resources;

import androidx.annotation.NonNull;

import com.davidrdz.vitallog.R;
import com.davidrdz.vitallog.analytics.ValidationError;
import com.davidrdz.vitallog.analytics.VitalRanges;

/**
 * Convierte cada {@link ValidationError} del Gestor Analítico en un mensaje en lenguaje llano
 * (qué pasó y qué hacer). Vive en la UI para que el validador no dependa de Android.
 */
public final class ValidationErrorMessages {

    private ValidationErrorMessages() {
    }

    @NonNull
    public static String message(@NonNull final Resources res,
                                 @NonNull final ValidationError error) {
        switch (error) {
            case GLUCOSE_TOO_LOW:
                return res.getString(R.string.error_glucose_too_low);
            case GLUCOSE_TOO_HIGH:
                return res.getString(R.string.error_glucose_too_high);
            case SYSTOLIC_TOO_LOW:
                return res.getString(R.string.error_systolic_too_low);
            case SYSTOLIC_TOO_HIGH:
                return res.getString(R.string.error_systolic_too_high);
            case DIASTOLIC_TOO_LOW:
                return res.getString(R.string.error_diastolic_too_low);
            case DIASTOLIC_TOO_HIGH:
                return res.getString(R.string.error_diastolic_too_high);
            case SYSTOLIC_NOT_ABOVE_DIASTOLIC:
                return res.getString(R.string.error_systolic_not_above_diastolic);
            case PULSE_PRESSURE_TOO_NARROW:
                return res.getString(R.string.error_pulse_pressure_too_narrow,
                        VitalRanges.MIN_PULSE_PRESSURE);
            default:
                throw new IllegalArgumentException("Unknown validation error: " + error);
        }
    }

    /** Mensaje del banner de error: "No se guardó. …". */
    @NonNull
    public static String notSavedMessage(@NonNull final Resources res,
                                         @NonNull final ValidationError error) {
        return res.getString(R.string.error_not_saved, message(res, error));
    }
}
