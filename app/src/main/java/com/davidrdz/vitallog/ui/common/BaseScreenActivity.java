package com.davidrdz.vitallog.ui.common;

import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;

import com.davidrdz.vitallog.R;

/**
 * Base de toda pantalla secundaria (CLAUDE.md §3.2).
 *
 * <p>El layout de la subclase debe tener una vista raíz con id {@code screen_root} e incluir
 * {@code @layout/view_screen_header}, que aporta el botón visible "Volver" y el título.
 */
public abstract class BaseScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.enableEdgeToEdge(getWindow());
        setContentView(getLayoutRes());
        SystemBarInsets.applyAsPadding(findViewById(R.id.screen_root));

        setTitle(getScreenTitleRes());
        final TextView title = findViewById(R.id.txt_screen_title);
        title.setText(getScreenTitleRes());
        findViewById(R.id.btn_back).setOnClickListener(view -> finish());
    }

    /** Layout de la pantalla; debe contener {@code screen_root} y el encabezado común. */
    @LayoutRes
    protected abstract int getLayoutRes();

    /** Título visible de la pantalla, también anunciado por TalkBack. */
    @StringRes
    protected abstract int getScreenTitleRes();
}
