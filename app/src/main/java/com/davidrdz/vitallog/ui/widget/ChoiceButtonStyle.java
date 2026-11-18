package com.davidrdz.vitallog.ui.widget;

import androidx.annotation.NonNull;

import com.davidrdz.vitallog.R;
import com.google.android.material.button.MaterialButton;

/**
 * Pinta un botón de opción ({@code Widget.VitalLog.Button.Choice}): la opción elegida lleva check,
 * borde grueso azul y fondo azul claro; las demás, fondo blanco y borde normal. Colores sólidos,
 * nunca opacidad; TalkBack la anuncia como marcada.
 */
public final class ChoiceButtonStyle {

    private ChoiceButtonStyle() {
    }

    public static void render(@NonNull final MaterialButton button, final boolean selected) {
        final int stroke = button.getResources().getDimensionPixelSize(
                selected ? R.dimen.stroke_selected : R.dimen.stroke_strong);
        button.setChecked(selected);
        button.setIconResource(selected ? R.drawable.ic_check : 0);
        button.setStrokeWidth(stroke);
        button.setStrokeColorResource(selected ? R.color.brand_primary : R.color.outline_strong);
        button.setBackgroundTintList(button.getContext().getColorStateList(
                selected ? R.color.brand_primary_container : R.color.background));
    }
}
