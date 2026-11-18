package com.davidrdz.vitallog.ui.common;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.view.WindowCompat;

import com.davidrdz.vitallog.R;

/**
 * Base de toda pantalla secundaria (CLAUDE.md §3.2).
 *
 * <p>El layout de la subclase debe tener una vista raíz con id {@code screen_root} e incluir
 * {@code @layout/view_screen_header}, que aporta el botón visible "Volver" y el título.
 */
public abstract class BaseScreenActivity extends AppCompatActivity {

    /** {@link #getScreenIconRes()} sin icono. */
    protected static final int NO_ICON = 0;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.enableEdgeToEdge(getWindow());
        setContentView(getLayoutRes());
        SystemBarInsets.applyAsPadding(findViewById(R.id.screen_root));

        setTitle(getScreenTitleRes());
        final TextView title = findViewById(R.id.txt_screen_title);
        title.setText(getScreenTitleRes());
        showTitleIcon(title, getScreenIconRes());
        findViewById(R.id.btn_back).setOnClickListener(view -> finish());
    }

    /**
     * Icono decorativo junto al título (p. ej. el plato del signo vital, el mismo del inicio).
     * Por defecto no hay icono. TalkBack no lo lee: el título ya dice dónde estás.
     */
    @DrawableRes
    protected int getScreenIconRes() {
        return NO_ICON;
    }

    private void showTitleIcon(@NonNull final TextView title, @DrawableRes final int iconRes) {
        if (iconRes == NO_ICON) {
            return;
        }
        final Drawable icon = AppCompatResources.getDrawable(this, iconRes);
        if (icon == null) {
            throw new IllegalStateException("Title icon not found: " + iconRes);
        }
        final int size = getResources().getDimensionPixelSize(R.dimen.title_icon_size);
        icon.setBounds(0, 0, size, size);
        title.setCompoundDrawablesRelative(icon, null, null, null);
        title.setCompoundDrawablePadding(
                getResources().getDimensionPixelSize(R.dimen.spacing_medium));
    }

    /** Layout de la pantalla; debe contener {@code screen_root} y el encabezado común. */
    @LayoutRes
    protected abstract int getLayoutRes();

    /** Título visible de la pantalla, también anunciado por TalkBack. */
    @StringRes
    protected abstract int getScreenTitleRes();
}
