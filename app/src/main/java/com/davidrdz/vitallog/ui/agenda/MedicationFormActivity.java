package com.davidrdz.vitallog.ui.agenda;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.davidrdz.vitallog.R;
import com.davidrdz.vitallog.VitalLogApp;
import com.davidrdz.vitallog.data.entity.DoseUnit;
import com.davidrdz.vitallog.data.entity.Medication;
import com.davidrdz.vitallog.data.repository.SaveResult;
import com.davidrdz.vitallog.feedback.FeedbackTonePlayer;
import com.davidrdz.vitallog.ui.common.BaseScreenActivity;
import com.davidrdz.vitallog.ui.common.SpokenTimeFormatter;
import com.davidrdz.vitallog.ui.common.ViewModelFactory;
import com.davidrdz.vitallog.ui.widget.NumericStepperView;
import com.google.android.material.button.MaterialButton;

import java.util.EnumMap;
import java.util.Map;

/**
 * Alta de un medicamento: nombre (teclado; no es un signo vital), cantidad con +/−, unidad con
 * botones grandes y hora con dos controles +/−. Al guardar vuelve a la agenda con el mensaje.
 */
public class MedicationFormActivity extends BaseScreenActivity {

    private static final int DEFAULT_DOSE = 1;
    private static final int DOSE_STEP = 1;
    private static final int DEFAULT_HOUR = 8;
    private static final int DEFAULT_MINUTE = 0;
    private static final DoseUnit DEFAULT_UNIT = DoseUnit.TABLET;
    private static final String STATE_DOSE_UNIT = "dose_unit";

    private final Map<DoseUnit, MaterialButton> unitButtons = new EnumMap<>(DoseUnit.class);
    private DoseUnit selectedUnit = DEFAULT_UNIT;
    private EditText nameField;
    private View nameError;
    private NumericStepperView doseStepper;
    private TimeSteppers time;
    private FeedbackTonePlayer tones;
    private SpokenTimeFormatter formatter;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            selectedUnit = DoseUnit.valueOf(
                    savedInstanceState.getString(STATE_DOSE_UNIT, DEFAULT_UNIT.name()));
        }
        formatter = VitalLogApp.from(this).getTimeFormatter();
        tones = FeedbackTonePlayer.attach(this);
        nameField = findViewById(R.id.edit_medication_name);
        nameError = findViewById(R.id.txt_name_error);

        doseStepper = findViewById(R.id.stepper_dose);
        doseStepper.bind(Medication.MIN_DOSE, Medication.MAX_DOSE, DEFAULT_DOSE, DOSE_STEP,
                new DoseFormatter());
        wireUnitButton(DoseUnit.TABLET, R.id.btn_unit_tablet);
        wireUnitButton(DoseUnit.CAPSULE, R.id.btn_unit_capsule);
        wireUnitButton(DoseUnit.MILLILITER, R.id.btn_unit_milliliter);
        wireUnitButton(DoseUnit.UNIT, R.id.btn_unit_unit);
        renderUnitButtons();

        time = new TimeSteppers(findViewById(R.id.stepper_hour), findViewById(R.id.stepper_minute),
                findViewById(R.id.txt_time_summary), formatter, DEFAULT_HOUR, DEFAULT_MINUTE,
                () -> { });

        final AgendaViewModel viewModel = ViewModelFactory.get(this, AgendaViewModel.class);
        viewModel.getMedicationSaved().observe(this, event -> {
            final SaveResult<Medication> result = event.getContentIfNotHandled();
            if (result != null) {
                returnToAgenda(result.getSaved());
            }
        });
        findViewById(R.id.btn_save_medication).setOnClickListener(view -> {
            final AgendaViewModel.FormProblem problem = viewModel.saveMedication(
                    nameField.getText().toString(), doseStepper.getValue(), selectedUnit,
                    time.getHour(), time.getMinute());
            showNameProblem(problem == AgendaViewModel.FormProblem.MISSING_NAME);
        });
    }

    private void wireUnitButton(@NonNull final DoseUnit unit, final int buttonId) {
        final MaterialButton button = findViewById(buttonId);
        unitButtons.put(unit, button);
        button.setOnClickListener(view -> {
            selectedUnit = unit;
            renderUnitButtons();
            doseStepper.refresh();
        });
    }

    /** La opción elegida lleva check, borde grueso y fondo azul claro; las demás, fondo blanco. */
    private void renderUnitButtons() {
        final int normalStroke = getResources().getDimensionPixelSize(R.dimen.stroke_strong);
        final int selectedStroke = getResources().getDimensionPixelSize(R.dimen.stroke_selected);
        for (final Map.Entry<DoseUnit, MaterialButton> entry : unitButtons.entrySet()) {
            final boolean selected = entry.getKey() == selectedUnit;
            final MaterialButton button = entry.getValue();
            button.setChecked(selected);
            button.setIconResource(selected ? R.drawable.ic_check : 0);
            button.setStrokeWidth(selected ? selectedStroke : normalStroke);
            button.setStrokeColorResource(selected ? R.color.brand_primary : R.color.outline_strong);
            button.setBackgroundTintList(getColorStateList(
                    selected ? R.color.brand_primary_container : R.color.background));
        }
    }

    private void showNameProblem(final boolean missing) {
        nameError.setVisibility(missing ? View.VISIBLE : View.GONE);
        if (missing) {
            tones.playError();
            nameField.requestFocus();
        }
    }

    private void returnToAgenda(@NonNull final Medication saved) {
        final String message = getString(R.string.saved_medication, saved.getName(),
                saved.getDoseAmount(),
                DoseUnitLabels.forAmount(getResources(), saved.getDoseUnit(),
                        saved.getDoseAmount()),
                formatter.formatTime(saved.getHour(), saved.getMinute()));
        setResult(RESULT_OK, new Intent().putExtra(AgendaActivity.EXTRA_SAVED_MESSAGE, message));
        finish();
    }

    @Override
    protected void onSaveInstanceState(@NonNull final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(STATE_DOSE_UNIT, selectedUnit.name());
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_medication_form;
    }

    @Override
    protected int getScreenTitleRes() {
        return R.string.title_medication_form;
    }

    /** Cantidad con la unidad elegida debajo: "1" + "tableta", "2" + "tabletas". */
    private final class DoseFormatter implements NumericStepperView.ValueFormatter {

        @NonNull
        @Override
        public CharSequence formatValue(final int value) {
            return getString(R.string.stepper_value, value);
        }

        @NonNull
        @Override
        public CharSequence formatUnit(final int value) {
            return DoseUnitLabels.forAmount(getResources(), selectedUnit, value);
        }

        @NonNull
        @Override
        public CharSequence formatSpokenUnit(final int value) {
            return formatUnit(value);
        }
    }
}
