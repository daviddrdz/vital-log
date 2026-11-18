package com.davidrdz.vitallog.ui.agenda;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.davidrdz.vitallog.R;
import com.davidrdz.vitallog.VitalLogApp;
import com.davidrdz.vitallog.alarms.AlarmPermissionChecker;
import com.davidrdz.vitallog.data.entity.Appointment;
import com.davidrdz.vitallog.data.entity.Medication;
import com.davidrdz.vitallog.data.repository.SaveResult;
import com.davidrdz.vitallog.feedback.SaveFeedback;
import com.davidrdz.vitallog.ui.common.BaseScreenActivity;
import com.davidrdz.vitallog.ui.common.SpokenTimeFormatter;
import com.davidrdz.vitallog.ui.common.ViewModelFactory;
import com.davidrdz.vitallog.ui.permissions.PermissionsGuideActivity;

import java.util.List;

/**
 * "Mis medicamentos y citas": listas con botón visible "Borrar" (con confirmación) y botones para
 * agregar. Los formularios devuelven el mensaje de lo guardado y aquí se muestra banner + tono.
 */
public class AgendaActivity extends BaseScreenActivity {

    /** Mensaje de confirmación que devuelve un formulario al guardar. */
    static final String EXTRA_SAVED_MESSAGE = "com.davidrdz.vitallog.extra.SAVED_MESSAGE";

    private final ActivityResultLauncher<Intent> formLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), this::onFormResult);

    private AgendaViewModel viewModel;
    private SaveFeedback feedback;
    private SpokenTimeFormatter formatter;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null && !AlarmPermissionChecker.missing(this).isEmpty()) {
            startActivity(new Intent(this, PermissionsGuideActivity.class));
        }
        feedback = SaveFeedback.attach(this, findViewById(R.id.feedback_banner));
        formatter = VitalLogApp.from(this).getTimeFormatter();
        viewModel = ViewModelFactory.get(this, AgendaViewModel.class);

        final MedicationAdapter medicationAdapter =
                new MedicationAdapter(formatter, this::confirmDelete);
        final AppointmentAdapter appointmentAdapter =
                new AppointmentAdapter(formatter, viewModel::now, this::confirmDelete);
        setUpList(R.id.list_medications, medicationAdapter);
        setUpList(R.id.list_appointments, appointmentAdapter);

        viewModel.getMedications().observe(this, medications -> {
            medicationAdapter.submitList(medications);
            showEmptyState(R.id.txt_medications_empty, R.id.list_medications, medications);
        });
        viewModel.getAppointments().observe(this, appointments -> {
            appointmentAdapter.submitList(appointments);
            showEmptyState(R.id.txt_appointments_empty, R.id.list_appointments, appointments);
        });
        viewModel.getMedicationDeleted().observe(this, event -> {
            final SaveResult<Medication> result = event.getContentIfNotHandled();
            if (result != null) {
                feedback.showSuccess(getString(R.string.deleted_medication,
                        result.getSaved().getName()));
            }
        });
        viewModel.getAppointmentDeleted().observe(this, event -> {
            final SaveResult<Appointment> result = event.getContentIfNotHandled();
            if (result != null) {
                feedback.showSuccess(getString(R.string.deleted_appointment,
                        result.getSaved().getDoctorOrPlace()));
            }
        });

        findViewById(R.id.btn_add_medication).setOnClickListener(
                view -> formLauncher.launch(new Intent(this, MedicationFormActivity.class)));
        findViewById(R.id.btn_add_appointment).setOnClickListener(
                view -> formLauncher.launch(new Intent(this, AppointmentFormActivity.class)));
    }

    private void setUpList(final int listId, @NonNull final RecyclerView.Adapter<?> adapter) {
        final RecyclerView list = findViewById(listId);
        list.setLayoutManager(new LinearLayoutManager(this));
        list.setAdapter(adapter);
    }

    private void showEmptyState(final int emptyTextId, final int listId,
                                @Nullable final List<?> items) {
        final boolean empty = items == null || items.isEmpty();
        findViewById(emptyTextId).setVisibility(empty ? View.VISIBLE : View.GONE);
        findViewById(listId).setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    private void confirmDelete(@NonNull final Medication medication) {
        DeleteConfirmationDialog.show(this,
                getString(R.string.confirm_delete_medication_question, medication.getName()),
                getString(R.string.confirm_delete_medication_detail),
                () -> viewModel.deleteMedication(medication));
    }

    private void confirmDelete(@NonNull final Appointment appointment) {
        DeleteConfirmationDialog.show(this,
                getString(R.string.confirm_delete_appointment_question,
                        appointment.getDoctorOrPlace()),
                getString(R.string.confirm_delete_appointment_detail, formatter.formatDateTime(
                        appointment.getScheduledAt(), viewModel.now())),
                () -> viewModel.deleteAppointment(appointment));
    }

    private void onFormResult(@NonNull final ActivityResult result) {
        final Intent data = result.getData();
        if (result.getResultCode() != RESULT_OK || data == null) {
            return;
        }
        final String message = data.getStringExtra(EXTRA_SAVED_MESSAGE);
        if (message != null) {
            feedback.showSuccess(message);
        }
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_agenda;
    }

    @Override
    protected int getScreenTitleRes() {
        return R.string.title_agenda;
    }
}
