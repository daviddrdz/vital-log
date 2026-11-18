package com.davidrdz.vitallog.data.repository;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.davidrdz.vitallog.alarms.ReminderScheduler;
import com.davidrdz.vitallog.alarms.ReminderType;
import com.davidrdz.vitallog.data.db.DoseLogDao;
import com.davidrdz.vitallog.data.db.MedicationDao;
import com.davidrdz.vitallog.data.entity.DoseLog;
import com.davidrdz.vitallog.data.entity.Medication;
import com.davidrdz.vitallog.data.entity.TakenDose;

import java.util.List;
import java.util.function.Consumer;

/**
 * Único punto de acceso a los medicamentos y sus tomas ({@link DoseLog}). Mantiene las alarmas
 * sincronizadas: guardar programa el recordatorio diario y borrar lo cancela.
 */
public final class MedicationRepository {

    @NonNull
    private final MedicationDao medicationDao;
    @NonNull
    private final DoseLogDao doseLogDao;
    @NonNull
    private final RepositoryExecutors executors;
    @NonNull
    private final ReminderScheduler scheduler;

    public MedicationRepository(@NonNull final MedicationDao medicationDao,
                                @NonNull final DoseLogDao doseLogDao,
                                @NonNull final RepositoryExecutors executors,
                                @NonNull final ReminderScheduler scheduler) {
        this.medicationDao = medicationDao;
        this.doseLogDao = doseLogDao;
        this.executors = executors;
        this.scheduler = scheduler;
    }

    /** Guarda el medicamento y programa su recordatorio; entrega el resultado en el hilo principal. */
    public void save(@NonNull final Medication medication,
                     @NonNull final SaveResult.Callback<Medication> callback) {
        executors.save(() -> {
            final Medication saved = medication.withId(medicationDao.insert(medication));
            scheduler.scheduleMedication(saved);
            return saved;
        }, callback);
    }

    /** Quita el medicamento de la lista (lo marca inactivo) y cancela su recordatorio. */
    public void delete(@NonNull final Medication medication,
                       @NonNull final SaveResult.Callback<Medication> callback) {
        executors.save(() -> {
            medicationDao.deactivate(medication.getId());
            scheduler.cancel(ReminderType.MEDICATION, medication.getId());
            return medication.deactivated();
        }, callback);
    }

    /** Registra una toma ("Ya lo tomé") en segundo plano. */
    public void saveDoseLog(@NonNull final DoseLog doseLog,
                            @NonNull final SaveResult.Callback<DoseLog> callback) {
        executors.save(() -> doseLog.withId(doseLogDao.insert(doseLog)), callback);
    }

    /** Entrega el medicamento si sigue activo, o null, en el hilo principal. */
    public void findActive(final long id, @NonNull final Consumer<Medication> callback) {
        executors.load(() -> medicationDao.findActiveById(id), callback);
    }

    /** Vuelve a programar el recordatorio de cada medicamento activo; luego ejecuta {@code done}. */
    public void rescheduleReminders(@NonNull final Runnable done) {
        executors.run(() -> {
            for (final Medication medication : medicationDao.getActive()) {
                scheduler.scheduleMedication(medication);
            }
        }, done);
    }

    /** Medicamentos activos, ordenados por hora de la toma. */
    @NonNull
    public LiveData<List<Medication>> observeActive() {
        return medicationDao.observeActive();
    }

    /** Tomas registradas desde {@code startInclusive}, con su medicamento, de la más reciente. */
    @NonNull
    public LiveData<List<TakenDose>> observeTakenDosesSince(final long startInclusive) {
        return doseLogDao.observeTakenSince(startInclusive);
    }
}
