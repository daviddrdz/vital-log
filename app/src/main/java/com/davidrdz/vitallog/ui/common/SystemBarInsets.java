package com.davidrdz.vitallog.ui.common;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * Evita que el contenido quede debajo de las barras del sistema cuando la app dibuja
 * de borde a borde: añade como padding el espacio que ocupan las barras.
 */
public final class SystemBarInsets {

    private SystemBarInsets() {
    }

    /** Suma los insets de las barras del sistema al padding original de {@code view}. */
    public static void applyAsPadding(@NonNull final View view) {
        final int initialLeft = view.getPaddingLeft();
        final int initialTop = view.getPaddingTop();
        final int initialRight = view.getPaddingRight();
        final int initialBottom = view.getPaddingBottom();
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, windowInsets) -> {
            final Insets bars = windowInsets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
            v.setPadding(initialLeft + bars.left, initialTop + bars.top,
                    initialRight + bars.right, initialBottom + bars.bottom);
            return windowInsets;
        });
    }
}
