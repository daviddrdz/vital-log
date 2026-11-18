package com.davidrdz.vitallog.data.repository;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.davidrdz.vitallog.analytics.ClinicalRangeValidator;
import com.davidrdz.vitallog.analytics.ValidationResult;
import com.davidrdz.vitallog.data.db.BloodPressureReadingDao;
import com.davidrdz.vitallog.data.db.GlucoseReadingDao;
import com.davidrdz.vitallog.data.entity.BloodPressureReading;
import com.davidrdz.vitallog.data.entity.GlucoseReading;

/**
 * Único punto de acceso a las lecturas de glucosa y presión arterial desde la UI.
 * Valida cada lectura con {@link ClinicalRangeValidator} antes de guardarla: una lectura imposible
 * nunca llega al DAO y se devuelve como {@link SaveResult#rejected}.
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

    /** Valida y guarda la lectura en segundo plano; entrega el resultado en el hilo principal. */
    public void saveGlucose(@NonNull final GlucoseReading reading,
                            @NonNull final SaveResult.Callback<GlucoseReading> callback) {
        final ValidationResult validation =
                ClinicalRangeValidator.validateGlucose(reading.getValueMgDl());
        if (!validation.isValid()) {
            executors.reject(validation.getError(), callback);
            return;
        }
        executors.save(() -> reading.withId(glucoseDao.insert(reading)), callback);
    }

    /** Valida y guarda la lectura en segundo plano; entrega el resultado en el hilo principal. */
    public void saveBloodPressure(@NonNull final BloodPressureReading reading,
                                  @NonNull final SaveResult.Callback<BloodPressureReading> callback) {
        final ValidationResult validation = ClinicalRangeValidator.validateBloodPressure(
                reading.getSystolic(), reading.getDiastolic());
        if (!validation.isValid()) {
            executors.reject(validation.getError(), callback);
            return;
        }
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
