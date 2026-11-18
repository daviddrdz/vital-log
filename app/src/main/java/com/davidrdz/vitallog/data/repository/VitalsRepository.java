package com.davidrdz.vitallog.data.repository;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.davidrdz.vitallog.data.db.BloodPressureReadingDao;
import com.davidrdz.vitallog.data.db.GlucoseReadingDao;
import com.davidrdz.vitallog.data.entity.BloodPressureReading;
import com.davidrdz.vitallog.data.entity.GlucoseReading;

/**
 * Único punto de acceso a las lecturas de glucosa y presión arterial desde la UI.
 * En el Hito 4 valida cada lectura con el Gestor Analítico antes de guardarla.
 */
public final class VitalsRepository {

    @NonNull
    private final GlucoseReadingDao glucoseDao;
    @NonNull
    private final BloodPressureReadingDao pressureDao;
    @NonNull
    private final RepositoryExecutors executors;

    public VitalsRepository(@NonNull final GlucoseReadingDao glucoseDao,
                            @NonNull final BloodPressureReadingDao pressureDao,
                            @NonNull final RepositoryExecutors executors) {
        this.glucoseDao = glucoseDao;
        this.pressureDao = pressureDao;
        this.executors = executors;
    }

    /** Guarda la lectura en segundo plano y entrega el resultado en el hilo principal. */
    public void saveGlucose(@NonNull final GlucoseReading reading,
                            @NonNull final SaveResult.Callback<GlucoseReading> callback) {
        executors.save(() -> reading.withId(glucoseDao.insert(reading)), callback);
    }

    /** Guarda la lectura en segundo plano y entrega el resultado en el hilo principal. */
    public void saveBloodPressure(@NonNull final BloodPressureReading reading,
                                  @NonNull final SaveResult.Callback<BloodPressureReading> callback) {
        executors.save(() -> reading.withId(pressureDao.insert(reading)), callback);
    }

    /** Última lectura de glucosa ({@code null} si no hay); valor inicial del control. */
    @NonNull
    public LiveData<GlucoseReading> observeLatestGlucose() {
        return glucoseDao.observeLatest();
    }

    /** Última lectura de presión ({@code null} si no hay); valor inicial de los controles. */
    @NonNull
    public LiveData<BloodPressureReading> observeLatestBloodPressure() {
        return pressureDao.observeLatest();
    }
}
