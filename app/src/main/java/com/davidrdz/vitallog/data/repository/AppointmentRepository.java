package com.davidrdz.vitallog.data.repository;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.davidrdz.vitallog.alarms.ReminderScheduler;
import com.davidrdz.vitallog.alarms.ReminderType;
import com.davidrdz.vitallog.data.db.AppointmentDao;
import com.davidrdz.vitallog.data.entity.Appointment;

import java.util.List;
import java.util.function.Consumer;

/**
 * Único punto de acceso a las citas médicas. Mantiene las alarmas sincronizadas: guardar programa
 * el aviso de la cita y borrar lo cancela.
 */
public final class AppointmentRepository {

    @NonNull
    private final AppointmentDao appointmentDao;
    @NonNull
    private final RepositoryExecutors executors;
    @NonNull
    private final ReminderScheduler scheduler;

    public AppointmentRepository(@NonNull final AppointmentDao appointmentDao,
                                 @NonNull final RepositoryExecutors executors,
                                 @NonNull final ReminderScheduler scheduler) {
        this.appointmentDao = appointmentDao;
        this.executors = executors;
        this.scheduler = scheduler;
    }

    /** Guarda la cita y programa su aviso; entrega el resultado en el hilo principal. */
    public void save(@NonNull final Appointment appointment,
                     @NonNull final SaveResult.Callback<Appointment> callback) {
        executors.save(() -> {
            final Appointment saved = appointment.withId(appointmentDao.insert(appointment));
            scheduler.scheduleAppointment(saved);
            return saved;
        }, callback);
    }

    /** Quita la cita de la lista (la marca inactiva) y cancela su aviso. */
    public void delete(@NonNull final Appointment appointment,
                       @NonNull final SaveResult.Callback<Appointment> callback) {
        executors.save(() -> {
            appointmentDao.deactivate(appointment.getId());
            scheduler.cancel(ReminderType.APPOINTMENT, appointment.getId());
            return appointment.deactivated();
        }, callback);
    }

    /** Entrega la cita si sigue activa, o null, en el hilo principal. */
    public void findActive(final long id, @NonNull final Consumer<Appointment> callback) {
        executors.load(() -> appointmentDao.findActiveById(id), callback);
    }

    /** Vuelve a programar el aviso de cada cita activa futura; luego ejecuta {@code done}. */
    public void rescheduleReminders(final long now, @NonNull final Runnable done) {
        executors.run(() -> {
            for (final Appointment appointment : appointmentDao.getUpcoming(now)) {
                scheduler.scheduleAppointment(appointment);
            }
        }, done);
    }

    /** Citas activas desde {@code now}, de la más próxima a la más lejana. */
    @NonNull
    public LiveData<List<Appointment>> observeUpcoming(final long now) {
        return appointmentDao.observeUpcoming(now);
    }
}
