package com.davidrdz.vitallog.ui.pressure;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.davidrdz.vitallog.analytics.ClinicalRangeValidator;
import com.davidrdz.vitallog.analytics.ReadingClassifier;
import com.davidrdz.vitallog.analytics.ValidationResult;
import com.davidrdz.vitallog.data.entity.BloodPressureReading;
import com.davidrdz.vitallog.data.repository.SaveResult;
import com.davidrdz.vitallog.data.repository.VitalsRepository;
import com.davidrdz.vitallog.ui.common.Event;
import com.davidrdz.vitallog.ui.common.ReadingStatus;

import java.time.Clock;

/**
 * Estado de la pantalla de presión arterial: valores iniciales (última lectura), categoría
 * informativa (o el motivo por el que los números no cuadran) y guardado validado.
 */
public final class PressureEntryViewModel extends ViewModel {

    @NonNull
    private final VitalsRepository repository;
    @NonNull
    private final Clock clock;
    @NonNull
    private final LiveData<Event<BloodPressureReading>> initialReading;
    private final MutableLiveData<Event<SaveResult<BloodPressureReading>>> saveEvents =
            new MutableLiveData<>();
    private final MutableLiveData<ReadingStatus> status = new MutableLiveData<>();
    private boolean saving;

    public PressureEntryViewModel(@NonNull final VitalsRepository repository,
                                  @NonNull final Clock clock) {
        this.repository = repository;
        this.clock = clock;
        initialReading = Transformations.map(repository.observeLatestBloodPressure(),
                latest -> latest == null ? null : new Event<>(latest));
    }

    /**
     * Última presión guardada, para arrancar los controles en ella. Es un evento: tras rotar la
     * pantalla no pisa los valores que el usuario ya ajustó.
     */
    @NonNull
    public LiveData<Event<BloodPressureReading>> getInitialReading() {
        return initialReading;
    }

    @NonNull
    public LiveData<Event<SaveResult<BloodPressureReading>>> getSaveEvents() {
        return saveEvents;
    }

    /** Categoría de los números que muestran los controles, o qué revisar si no cuadran. */
    @NonNull
    public LiveData<ReadingStatus> getStatus() {
        return status;
    }

    /** Recalcula la categoría cada vez que cambia cualquiera de los dos controles. */
    public void onValuesChanged(final int systolic, final int diastolic) {
        final ValidationResult validation =
                ClinicalRangeValidator.validateBloodPressure(systolic, diastolic);
        status.setValue(validation.isValid()
                ? ReadingStatus.of(ReadingStatus.Measure.BLOOD_PRESSURE,
                        ReadingClassifier.classifyBloodPressure(systolic, diastolic))
                : ReadingStatus.problem(ReadingStatus.Measure.BLOOD_PRESSURE,
                        validation.getError()));
    }

    /**
     * Guarda la lectura con la hora actual; ignora toques repetidos mientras guarda. El
     * repositorio la rechaza si es imposible (p. ej. el número de arriba no es mayor que el de
     * abajo); un valor alarmante pero posible sí se guarda.
     */
    public void save(final int systolic, final int diastolic) {
        if (saving) {
            return;
        }
        saving = true;
        repository.saveBloodPressure(
                new BloodPressureReading(0, systolic, diastolic, clock.millis()), result -> {
                    saving = false;
                    saveEvents.setValue(new Event<>(result));
                });
    }
}
