package com.davidrdz.vitallog.ui.agenda;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.davidrdz.vitallog.data.entity.Appointment;
import com.davidrdz.vitallog.data.entity.DoseUnit;
import com.davidrdz.vitallog.data.entity.Medication;
import com.davidrdz.vitallog.data.repository.AppointmentRepository;
import com.davidrdz.vitallog.data.repository.MedicationRepository;
import com.davidrdz.vitallog.data.repository.SaveResult;
import com.davidrdz.vitallog.ui.common.Event;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

/**
 * Estado de "Mis medicamentos y citas" y de sus formularios: listas, alta y borrado.
 * Los formularios usan su propia instancia para guardar.
 */
public final class AgendaViewModel extends ViewModel {

    /** Motivo por el que un formulario no se puede guardar todavía. */
    public enum FormProblem {
        NONE,
        MISSING_NAME,
        TIME_ALREADY_PASSED
    }

    @NonNull
    private final MedicationRepository medicationRepository;
    @NonNull
    private final AppointmentRepository appointmentRepository;
    @NonNull
    private final Clock clock;

    private final MutableLiveData<Event<SaveResult<Medication>>> medicationSaved =
            new MutableLiveData<>();
    private final MutableLiveData<Event<SaveResult<Medication>>> medicationDeleted =
            new MutableLiveData<>();
    private final MutableLiveData<Event<SaveResult<Appointment>>> appointmentSaved =
            new MutableLiveData<>();
    private final MutableLiveData<Event<SaveResult<Appointment>>> appointmentDeleted =
            new MutableLiveData<>();

    private LiveData<List<Medication>> medications;
    private LiveData<List<Appointment>> appointments;
    private boolean saving;

    public AgendaViewModel(@NonNull final MedicationRepository medicationRepository,
                           @NonNull final AppointmentRepository appointmentRepository,
                           @NonNull final Clock clock) {
        this.medicationRepository = medicationRepository;
        this.appointmentRepository = appointmentRepository;
        this.clock = clock;
    }

    /** Medicamentos activos, ordenados por hora. */
    @NonNull
    public LiveData<List<Medication>> getMedications() {
        if (medications == null) {
            medications = medicationRepository.observeActive();
        }
        return medications;
    }

    /** Citas activas desde ahora, de la más próxima a la más lejana. */
    @NonNull
    public LiveData<List<Appointment>> getAppointments() {
        if (appointments == null) {
            appointments = appointmentRepository.observeUpcoming(clock.millis());
        }
        return appointments;
    }

    @NonNull
    public LiveData<Event<SaveResult<Medication>>> getMedicationSaved() {
        return medicationSaved;
    }

    @NonNull
    public LiveData<Event<SaveResult<Medication>>> getMedicationDeleted() {
        return medicationDeleted;
    }

    @NonNull
    public LiveData<Event<SaveResult<Appointment>>> getAppointmentSaved() {
        return appointmentSaved;
    }

    @NonNull
    public LiveData<Event<SaveResult<Appointment>>> getAppointmentDeleted() {
        return appointmentDeleted;
    }

    /** Ahora, en epoch millis (para formatear días relativos: "hoy", "mañana"). */
    public long now() {
        return clock.millis();
    }

    /**
     * Guarda un medicamento nuevo. Devuelve {@link FormProblem#MISSING_NAME} si falta el nombre;
     * el resultado del guardado llega por {@link #getMedicationSaved()}.
     */
    @NonNull
    public FormProblem saveMedication(@NonNull final String name, final int doseAmount,
                                      @NonNull final DoseUnit doseUnit, final int hour,
                                      final int minute) {
        final String trimmed = name.trim();
        if (trimmed.isEmpty()) {
            return FormProblem.MISSING_NAME;
        }
        if (!saving) {
            saving = true;
            medicationRepository.save(
                    new Medication(0, trimmed, doseAmount, doseUnit, hour, minute, true),
                    result -> {
                        saving = false;
                        medicationSaved.setValue(new Event<>(result));
                    });
        }
        return FormProblem.NONE;
    }

    /**
     * Guarda una cita nueva dentro de {@code daysAhead} días a la hora indicada. Devuelve el
     * problema que impide guardarla, si lo hay; el resultado llega por
     * {@link #getAppointmentSaved()}.
     */
    @NonNull
    public FormProblem saveAppointment(@NonNull final String doctorOrPlace, final int daysAhead,
                                       final int hour, final int minute) {
        final String trimmed = doctorOrPlace.trim();
        if (trimmed.isEmpty()) {
            return FormProblem.MISSING_NAME;
        }
        final long scheduledAt = scheduledAtFor(daysAhead, hour, minute);
        if (scheduledAt <= clock.millis()) {
            return FormProblem.TIME_ALREADY_PASSED;
        }
        if (!saving) {
            saving = true;
            appointmentRepository.save(new Appointment(0, trimmed, scheduledAt, true),
                    result -> {
                        saving = false;
                        appointmentSaved.setValue(new Event<>(result));
                    });
        }
        return FormProblem.NONE;
    }

    /** Fecha y hora (epoch millis) de "dentro de {@code daysAhead} días a las hour:minute". */
    public long scheduledAtFor(final int daysAhead, final int hour, final int minute) {
        return LocalDate.now(clock).plusDays(daysAhead).atTime(hour, minute)
                .atZone(clock.getZone()).toInstant().toEpochMilli();
    }

    public void deleteMedication(@NonNull final Medication medication) {
        medicationRepository.delete(medication,
                result -> medicationDeleted.setValue(new Event<>(result)));
    }

    public void deleteAppointment(@NonNull final Appointment appointment) {
        appointmentRepository.delete(appointment,
                result -> appointmentDeleted.setValue(new Event<>(result)));
    }
}
