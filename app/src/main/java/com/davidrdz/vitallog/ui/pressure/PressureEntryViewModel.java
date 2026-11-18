package com.davidrdz.vitallog.ui.pressure;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.davidrdz.vitallog.data.entity.BloodPressureReading;
import com.davidrdz.vitallog.data.repository.SaveResult;
import com.davidrdz.vitallog.data.repository.VitalsRepository;
import com.davidrdz.vitallog.ui.common.Event;

import java.time.Clock;

/** Estado de la pantalla de presión arterial: valores iniciales (última lectura) y guardado. */
public final class PressureEntryViewModel extends ViewModel {

    @NonNull
    private final VitalsRepository repository;
    @NonNull
    private final Clock clock;
    @NonNull
    private final LiveData<Event<BloodPressureReading>> initialReading;
    private final MutableLiveData<Event<SaveResult<BloodPressureReading>>> saveEvents =
            new MutableLiveData<>();
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

    /**
     * Guarda la lectura con la hora actual; ignora toques repetidos mientras guarda.
     * La regla "número de arriba mayor que el de abajo" llega en el Hito 4.
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
