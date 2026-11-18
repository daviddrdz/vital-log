package com.davidrdz.vitallog.data.repository;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.davidrdz.vitallog.data.db.DoseLogDao;
import com.davidrdz.vitallog.data.db.MedicationDao;
import com.davidrdz.vitallog.data.entity.DoseLog;
import com.davidrdz.vitallog.data.entity.Medication;

import java.util.List;

/** Único punto de acceso a los medicamentos y sus tomas ({@link DoseLog}) desde la UI. */
public final class MedicationRepository {

    @NonNull
    private final MedicationDao medicationDao;
    @NonNull
    private final DoseLogDao doseLogDao;
    @NonNull
    private final RepositoryExecutors executors;

    public MedicationRepository(@NonNull final MedicationDao medicationDao,
                                @NonNull final DoseLogDao doseLogDao,
                                @NonNull final RepositoryExecutors executors) {
        this.medicationDao = medicationDao;
        this.doseLogDao = doseLogDao;
        this.executors = executors;
    }

    /** Guarda el medicamento en segundo plano y entrega el resultado en el hilo principal. */
    public void save(@NonNull final Medication medication,
                     @NonNull final SaveResult.Callback<Medication> callback) {
        executors.save(() -> medication.withId(medicationDao.insert(medication)), callback);
    }

    /** Quita el medicamento de la lista (lo marca inactivo) conservando su historial de tomas. */
    public void delete(@NonNull final Medication medication,
                       @NonNull final SaveResult.Callback<Medication> callback) {
        executors.save(() -> {
            medicationDao.deactivate(medication.getId());
            return medication.deactivated();
        }, callback);
    }

    /** Registra una toma ("Ya lo tomé") en segundo plano. */
    public void saveDoseLog(@NonNull final DoseLog doseLog,
                            @NonNull final SaveResult.Callback<DoseLog> callback) {
        executors.save(() -> doseLog.withId(doseLogDao.insert(doseLog)), callback);
    }

    /** Medicamentos activos, ordenados por hora de la toma. */
    @NonNull
    public LiveData<List<Medication>> observeActive() {
        return medicationDao.observeActive();
    }
}
