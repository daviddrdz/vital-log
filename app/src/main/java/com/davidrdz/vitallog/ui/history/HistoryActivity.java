package com.davidrdz.vitallog.ui.history;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.davidrdz.vitallog.R;
import com.davidrdz.vitallog.VitalLogApp;
import com.davidrdz.vitallog.analytics.VitalUnit;
import com.davidrdz.vitallog.ui.common.BaseScreenActivity;
import com.davidrdz.vitallog.ui.common.ViewModelFactory;
import com.davidrdz.vitallog.ui.widget.ChoiceButtonStyle;
import com.davidrdz.vitallog.ui.widget.VitalUnitLabels;
import com.google.android.material.button.MaterialButton;

/**
 * "Mi historial", pensado para enseñarlo al médico: dos botones de periodo (filtros, no pestañas),
 * resumen arriba y lista día por día. Solo lectura: no hay toques en las filas ni gestos.
 */
public class HistoryActivity extends BaseScreenActivity {

    private HistoryViewModel viewModel;
    private MaterialButton period7Button;
    private MaterialButton period30Button;
    private HistoryAdapter adapter;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = ViewModelFactory.get(this, HistoryViewModel.class);

        adapter = new HistoryAdapter(VitalLogApp.from(this).getTimeFormatter(), viewModel.now());
        final RecyclerView list = findViewById(R.id.list_history);
        list.setLayoutManager(new LinearLayoutManager(this));
        list.setAdapter(adapter);

        period7Button = findViewById(R.id.btn_period_7);
        period30Button = findViewById(R.id.btn_period_30);
        period7Button.setOnClickListener(view -> selectPeriod(HistoryPeriod.LAST_7_DAYS));
        period30Button.setOnClickListener(view -> selectPeriod(HistoryPeriod.LAST_30_DAYS));
        renderPeriod(viewModel.getPeriod());

        viewModel.getSnapshot().observe(this, this::render);
    }

    /**
     * Un botón "checkable" se desmarca solo al tocarlo de nuevo; se repinta siempre para que el
     * periodo elegido nunca quede sin su check.
     */
    private void selectPeriod(@NonNull final HistoryPeriod period) {
        viewModel.selectPeriod(period);
        renderPeriod(viewModel.getPeriod());
    }

    private void render(@NonNull final HistoryViewModel.Snapshot snapshot) {
        renderPeriod(snapshot.getPeriod());
        renderSummary(snapshot.getSummary());
        adapter.submitList(snapshot.getRows());
        final boolean empty = snapshot.getRows().isEmpty();
        findViewById(R.id.txt_history_empty).setVisibility(empty ? View.VISIBLE : View.GONE);
        findViewById(R.id.list_history).setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    private void renderPeriod(@NonNull final HistoryPeriod period) {
        ChoiceButtonStyle.render(period7Button, period == HistoryPeriod.LAST_7_DAYS);
        ChoiceButtonStyle.render(period30Button, period == HistoryPeriod.LAST_30_DAYS);
        final TextView title = findViewById(R.id.txt_summary_title);
        title.setText(summaryTitle(period));
    }

    private void renderSummary(@NonNull final HistorySummary summary) {
        final TextView glucoseText = findViewById(R.id.txt_summary_glucose);
        final HistorySummary.Stats glucose = summary.getGlucose();
        if (glucose == null) {
            glucoseText.setText(R.string.history_no_readings);
        } else {
            glucoseText.setText(getString(R.string.history_summary_glucose, glucose.getAverage(),
                    glucose.getMin(), glucose.getMax(), unit(VitalUnit.MG_PER_DL)));
        }

        final TextView pressureText = findViewById(R.id.txt_summary_pressure);
        final HistorySummary.Stats systolic = summary.getSystolic();
        final HistorySummary.Stats diastolic = summary.getDiastolic();
        if (systolic == null || diastolic == null) {
            pressureText.setText(R.string.history_no_readings);
        } else {
            pressureText.setText(getString(R.string.history_summary_pressure,
                    systolic.getAverage(), diastolic.getAverage(),
                    systolic.getMin(), systolic.getMax(),
                    diastolic.getMin(), diastolic.getMax(), unit(VitalUnit.MMHG)));
        }

        final TextView dosesText = findViewById(R.id.txt_summary_doses);
        if (summary.getDoseCount() == 0) {
            dosesText.setText(R.string.history_no_doses);
        } else {
            dosesText.setText(getString(R.string.history_summary_doses, summary.getDoseCount()));
        }
    }

    @NonNull
    private String unit(@NonNull final VitalUnit unit) {
        return getString(VitalUnitLabels.shortLabel(unit));
    }

    @StringRes
    private static int summaryTitle(@NonNull final HistoryPeriod period) {
        switch (period) {
            case LAST_7_DAYS:
                return R.string.history_summary_title_7;
            case LAST_30_DAYS:
                return R.string.history_summary_title_30;
            default:
                throw new IllegalArgumentException("Unknown period: " + period);
        }
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_history;
    }

    @Override
    protected int getScreenTitleRes() {
        return R.string.title_history;
    }
}
