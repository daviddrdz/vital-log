package com.davidrdz.vitallog.ui.glucose;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.davidrdz.vitallog.analytics.ReadingClassifier;
import com.davidrdz.vitallog.data.entity.GlucoseReading;
import com.davidrdz.vitallog.data.repository.SaveResult;
import com.davidrdz.vitallog.data.repository.VitalsRepository;
import com.davidrdz.vitallog.ui.common.Event;
import com.davidrdz.vitallog.ui.common.ReadingStatus;

import java.time.Clock;

/**
 * Estado de la pantalla de glucosa: valor inicial (última lectura), categoría informativa del
 * valor que se está ajustando y guardado (validado por el repositorio).
 */
public final class GlucoseEntryViewModel extends ViewModel {

    @NonNull
    private final VitalsRepository repository;
    @NonNull
    private final Clock clock;
    @NonNull
    private final LiveData<Event<Integer>> initialValue;
    private final MutableLiveData<Event<SaveResult<GlucoseReading>>> saveEvents =
            new MutableLiveData<>();
    private final MutableLiveData<ReadingStatus> status = new MutableLiveData<>();
    private boolean saving;

    public GlucoseEntryViewModel(@NonNull final VitalsRepository repository,
                                 @NonNull final Clock clock) {
        this.repository = repository;
        this.clock = clock;
        initialValue = Transformations.map(repository.observeLatestGlucose(),
                latest -> latest == null ? null : new Event<>(latest.getValueMgDl()));
    }

    /**
     * Última glucosa guardada, para arrancar el control en ella. Es un evento: tras rotar la
     * pantalla no pisa el valor que el usuario ya ajustó.
     */
    @NonNull
    public LiveData<Event<Integer>> getInitialValue() {
        return initialValue;
    }

    @NonNull
    public LiveData<Event<SaveResult<GlucoseReading>>> getSaveEvents() {
        return saveEvents;
    }

    /** Categoría del valor que muestra el control (texto + icono + color en la UI). */
    @NonNull
    public LiveData<ReadingStatus> getStatus() {
        return status;
    }

    /** Recalcula la categoría cada vez que cambia el control. */
    public void onValueChanged(final int valueMgDl) {
        status.setValue(ReadingStatus.of(ReadingStatus.Measure.GLUCOSE,
                ReadingClassifier.classifyGlucose(valueMgDl)));
    }

    /**
     * Guarda la lectura con la hora actual; ignora toques repetidos mientras guarda. El
     * repositorio la rechaza si es imposible; un valor alarmante pero posible sí se guarda.
     */
    public void save(final int valueMgDl) {
        if (saving) {
            return;
        }
        saving = true;
        repository.saveGlucose(new GlucoseReading(0, valueMgDl, clock.millis()), result -> {
            saving = false;
            saveEvents.setValue(new Event<>(result));
        });
    }
}
