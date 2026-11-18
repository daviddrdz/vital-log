package com.davidrdz.vitallog.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.IdRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;

import com.davidrdz.vitallog.R;
import com.davidrdz.vitallog.VitalLogApp;
import com.davidrdz.vitallog.ui.agenda.AgendaActivity;
import com.davidrdz.vitallog.ui.common.SpokenTimeFormatter;
import com.davidrdz.vitallog.ui.common.SystemBarInsets;
import com.davidrdz.vitallog.ui.glucose.GlucoseEntryActivity;
import com.davidrdz.vitallog.ui.history.HistoryActivity;
import com.davidrdz.vitallog.ui.pressure.PressureEntryActivity;

import java.time.ZonedDateTime;

/**
 * Dashboard principal: saludo según la hora y cuatro acciones que llevan directo a cada pantalla
 * (un solo nivel de profundidad, CLAUDE.md §3.2).
 */
public class MainActivity extends AppCompatActivity {

    private ImageView greetingIcon;
    private TextView greetingText;
    private TextView todayDateText;
    private SpokenTimeFormatter timeFormatter;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.enableEdgeToEdge(getWindow());
        setContentView(R.layout.activity_main);
        SystemBarInsets.applyAsPadding(findViewById(R.id.screen_root));

        greetingIcon = findViewById(R.id.img_greeting);
        greetingText = findViewById(R.id.txt_greeting);
        todayDateText = findViewById(R.id.txt_today_date);
        timeFormatter = ((VitalLogApp) getApplication()).getTimeFormatter();

        openOnClick(R.id.btn_register_glucose, GlucoseEntryActivity.class);
        openOnClick(R.id.btn_register_pressure, PressureEntryActivity.class);
        openOnClick(R.id.btn_agenda, AgendaActivity.class);
        openOnClick(R.id.btn_history, HistoryActivity.class);
    }

    /** Se recalcula al volver a la pantalla, por si la app quedó abierta de un día para otro. */
    @Override
    protected void onResume() {
        super.onResume();
        final ZonedDateTime now = ZonedDateTime.now();
        final DayPeriod period = DayPeriod.from(now.toLocalTime());
        greetingIcon.setImageResource(DayPeriodStyle.icon(period));
        greetingText.setText(DayPeriodStyle.greeting(period));
        todayDateText.setText(timeFormatter.formatFullDate(now.toInstant().toEpochMilli()));
    }

    private void openOnClick(@IdRes final int buttonId, final Class<?> destination) {
        findViewById(buttonId).setOnClickListener(
                view -> startActivity(new Intent(this, destination)));
    }
}
