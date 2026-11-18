package com.davidrdz.vitallog.data.repository;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.davidrdz.vitallog.data.db.AppointmentDao;
import com.davidrdz.vitallog.data.entity.Appointment;

import java.util.List;

/** Único punto de acceso a las citas médicas desde la UI. */
public final class AppointmentRepository {

    @NonNull
    private final AppointmentDao appointmentDao;
    @NonNull
    private final RepositoryExecutors executors;

    public AppointmentRepository(@NonNull final AppointmentDao appointmentDao,
                                 @NonNull final RepositoryExecutors executors) {
        this.appointmentDao = appointmentDao;
        this.executors = executors;
    }

    /** Guarda la cita en segundo plano y entrega el resultado en el hilo principal. */
    public void save(@NonNull final Appointment appointment,
                     @NonNull final SaveResult.Callback<Appointment> callback) {
        executors.save(() -> appointment.withId(appointmentDao.insert(appointment)), callback);
    }

    /** Quita la cita de la lista (la marca inactiva). */
    public void delete(@NonNull final Appointment appointment,
                       @NonNull final SaveResult.Callback<Appointment> callback) {
        executors.save(() -> {
            appointmentDao.deactivate(appointment.getId());
            return appointment.deactivated();
        }, callback);
    }

    /** Citas activas desde {@code now}, de la más próxima a la más lejana. */
    @NonNull
    public LiveData<List<Appointment>> observeUpcoming(final long now) {
        return appointmentDao.observeUpcoming(now);
    }
}
