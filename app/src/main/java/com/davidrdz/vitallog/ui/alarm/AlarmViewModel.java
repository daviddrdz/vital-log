package com.davidrdz.vitallog.ui.alarm;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.davidrdz.vitallog.alarms.AlarmScheduler;
import com.davidrdz.vitallog.alarms.Reminder;
import com.davidrdz.vitallog.alarms.ReminderNotifier;
import com.davidrdz.vitallog.alarms.ReminderType;
import com.davidrdz.vitallog.data.entity.Appointment;
import com.davidrdz.vitallog.data.entity.DoseLog;
import com.davidrdz.vitallog.data.entity.Medication;
import com.davidrdz.vitallog.data.repository.AppointmentRepository;
import com.davidrdz.vitallog.data.repository.MedicationRepository;
import com.davidrdz.vitallog.data.repository.SaveResult;
import com.davidrdz.vitallog.ui.common.Event;

import java.time.Clock;

/**
 * Estado del modal de alarma: qué se recuerda y las 2 respuestas posibles. Tras la primera
 * respuesta ignora cualquier otro toque, para que nunca se registre una toma doble.
 */
public final class AlarmViewModel extends ViewModel {

    /** Lo que muestra el modal: un medicamento o una cita (exactamente uno no es null). */
    public static final class AlarmContent {
        @Nullable
        private final Medication medication;
        @Nullable
        private final Appointment appointment;

        private AlarmContent(@Nullable final Medication medication,
                             @Nullable final Appointment appointment) {
            this.medication = medication;
            this.appointment = appointment;
        }

        @Nullable
        public Medication getMedication() {
            return medication;
        }

        @Nullable
        public Appointment getAppointment() {
            return appointment;
        }
    }

    @NonNull
    private final MedicationRepository medicationRepository;
    @NonNull
    private final AppointmentRepository appointmentRepository;
    @NonNull
    private final AlarmScheduler scheduler;
    @NonNull
    private final ReminderNotifier notifier;
    @NonNull
    private final Clock clock;

    private final MutableLiveData<AlarmContent> content = new MutableLiveData<>();
    private final MutableLiveData<Event<SaveResult<DoseLog>>> takenEvents =
            new MutableLiveData<>();
    private final MutableLiveData<Event<Boolean>> closeEvents = new MutableLiveData<>();

    @Nullable
    private Reminder reminder;
    private boolean answered;

    public AlarmViewModel(@NonNull final MedicationRepository medicationRepository,
                          @NonNull final AppointmentRepository appointmentRepository,
                          @NonNull final AlarmScheduler scheduler,
                          @NonNull final ReminderNotifier notifier,
                          @NonNull final Clock clock) {
        this.medicationRepository = medicationRepository;
        this.appointmentRepository = appointmentRepository;
        this.scheduler = scheduler;
        this.notifier = notifier;
        this.clock = clock;
    }

    @NonNull
    public LiveData<AlarmContent> getContent() {
        return content;
    }

    /** Resultado de "Ya lo tomé": la Activity muestra banner + tono y luego cierra. */
    @NonNull
    public LiveData<Event<SaveResult<DoseLog>>> getTakenEvents() {
        return takenEvents;
    }

    /** El modal debe cerrarse sin retroalimentación de guardado. */
    @NonNull
    public LiveData<Event<Boolean>> getCloseEvents() {
        return closeEvents;
    }

    /**
     * Carga el recordatorio. Si el medicamento o la cita ya se borró, quita la notificación y
     * cierra. Con {@code takeNow} (acción "Ya lo tomé" de la notificación) registra la toma en
     * cuanto carga.
     */
    public void load(@NonNull final Reminder newReminder, final boolean takeNow) {
        reminder = newReminder;
        answered = false;
        if (newReminder.getType() == ReminderType.MEDICATION) {
            medicationRepository.findActive(newReminder.getId(), medication -> {
                if (medication == null) {
                    closeMissing(newReminder);
                    return;
                }
                content.setValue(new AlarmContent(medication, null));
                if (takeNow) {
                    onConfirm();
                }
            });
        } else {
            appointmentRepository.findActive(newReminder.getId(), appointment -> {
                if (appointment == null) {
                    closeMissing(newReminder);
                    return;
                }
                content.setValue(new AlarmContent(null, appointment));
            });
        }
    }

    /** "Ya lo tomé" (medicamento: registra la toma) o "Entendido" (cita: solo cierra). */
    public void onConfirm() {
        final Reminder current = reminder;
        if (current == null || answered) {
            return;
        }
        answered = true;
        notifier.cancel(current);
        if (current.getType() == ReminderType.APPOINTMENT) {
            closeEvents.setValue(new Event<>(true));
            return;
        }
        medicationRepository.saveDoseLog(
                new DoseLog(0, current.getId(), current.getScheduledAt(), clock.millis()),
                result -> takenEvents.setValue(new Event<>(result)));
    }

    /** "Recordar más tarde": pospone 10 min sin tocar la alarma diaria y cierra. */
    public void onSnooze() {
        final Reminder current = reminder;
        if (current == null || answered) {
            return;
        }
        answered = true;
        scheduler.snooze(current);
        notifier.cancel(current);
        closeEvents.setValue(new Event<>(true));
    }

    private void closeMissing(@NonNull final Reminder missing) {
        notifier.cancel(missing);
        closeEvents.setValue(new Event<>(true));
    }
}
