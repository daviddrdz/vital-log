package com.davidrdz.vitallog.ui.home;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.IdRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;

import com.davidrdz.vitallog.R;
import com.davidrdz.vitallog.ui.agenda.AgendaActivity;
import com.davidrdz.vitallog.ui.common.SystemBarInsets;
import com.davidrdz.vitallog.ui.glucose.GlucoseEntryActivity;
import com.davidrdz.vitallog.ui.pressure.PressureEntryActivity;

/**
 * Dashboard principal: tres acciones masivas que llevan directo a cada pantalla
 * (un solo nivel de profundidad, CLAUDE.md §3.2).
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.enableEdgeToEdge(getWindow());
        setContentView(R.layout.activity_main);
        SystemBarInsets.applyAsPadding(findViewById(R.id.screen_root));

        openOnClick(R.id.btn_register_glucose, GlucoseEntryActivity.class);
        openOnClick(R.id.btn_register_pressure, PressureEntryActivity.class);
        openOnClick(R.id.btn_agenda, AgendaActivity.class);
    }

    private void openOnClick(@IdRes final int buttonId, final Class<?> destination) {
        findViewById(buttonId).setOnClickListener(
                view -> startActivity(new Intent(this, destination)));
    }
}
