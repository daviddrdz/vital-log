package com.davidrdz.vitallog.ui.history;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;

import com.davidrdz.vitallog.data.entity.BloodPressureReading;
import com.davidrdz.vitallog.data.entity.GlucoseReading;
import com.davidrdz.vitallog.data.entity.TakenDose;
import com.davidrdz.vitallog.data.repository.MedicationRepository;
import com.davidrdz.vitallog.data.repository.VitalsRepository;

import java.time.Clock;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Estado de "Mi historial" (solo lectura). Observa una sola vez los últimos 30 días de azúcar,
 * presión y tomas; cambiar de periodo solo vuelve a filtrar en memoria con
 * {@link HistorySummaryCalculator}.
 */
public final class HistoryViewModel extends ViewModel {

    /** Lo que muestra la pantalla para el periodo elegido. */
    public static final class Snapshot {
        @NonNull
        private final HistoryPeriod period;
        @NonNull
        private final HistorySummary summary;
        @NonNull
        private final List<HistoryItem> rows;

        Snapshot(@NonNull final HistoryPeriod period, @NonNull final HistorySummary summary,
                 @NonNull final List<HistoryItem> rows) {
            this.period = period;
            this.summary = summary;
            this.rows = rows;
        }

        @NonNull
        public HistoryPeriod getPeriod() {
            return period;
        }

        @NonNull
        public HistorySummary getSummary() {
            return summary;
        }

        /** Encabezados de día y registros, del más reciente al más antiguo. */
        @NonNull
        public List<HistoryItem> getRows() {
            return rows;
        }
    }

    private final long now;
    @NonNull
    private final ZoneId zone;
    private final MediatorLiveData<Snapshot> snapshot = new MediatorLiveData<>();

    @NonNull
    private HistoryPeriod period = HistoryPeriod.LAST_7_DAYS;
    @Nullable
    private List<GlucoseReading> glucose;
    @Nullable
    private List<BloodPressureReading> pressure;
    @Nullable
    private List<TakenDose> doses;

    public HistoryViewModel(@NonNull final VitalsRepository vitalsRepository,
                            @NonNull final MedicationRepository medicationRepository,
                            @NonNull final Clock clock) {
        now = clock.millis();
        zone = clock.getZone();
        final long since = HistorySummaryCalculator.windowStart(
                HistoryPeriod.LAST_30_DAYS, now, zone);
        snapshot.addSource(vitalsRepository.observeGlucoseSince(since), list -> {
            glucose = list;
            publish();
        });
        snapshot.addSource(vitalsRepository.observeBloodPressureSince(since), list -> {
            pressure = list;
            publish();
        });
        snapshot.addSource(medicationRepository.observeTakenDosesSince(since), list -> {
            doses = list;
            publish();
        });
    }

    /** Periodo, resumen y filas; llega cuando las tres consultas respondieron. */
    @NonNull
    public LiveData<Snapshot> getSnapshot() {
        return snapshot;
    }

    @NonNull
    public HistoryPeriod getPeriod() {
        return period;
    }

    public void selectPeriod(@NonNull final HistoryPeriod newPeriod) {
        if (newPeriod != period) {
            period = newPeriod;
            publish();
        }
    }

    /** Momento en que se abrió la pantalla, epoch millis (para "Hoy" y "Ayer"). */
    public long now() {
        return now;
    }

    private void publish() {
        if (glucose == null || pressure == null || doses == null) {
            return;
        }
        final List<HistoryItem> inPeriod = HistorySummaryCalculator.filter(
                allEntries(glucose, pressure, doses), period, now, zone);
        snapshot.setValue(new Snapshot(period, HistorySummaryCalculator.summarize(inPeriod),
                HistoryItem.groupByDay(inPeriod, zone)));
    }

    /** Une las tres listas en una sola, del registro más reciente al más antiguo. */
    @NonNull
    private static List<HistoryItem> allEntries(@NonNull final List<GlucoseReading> glucose,
                                                @NonNull final List<BloodPressureReading> pressure,
                                                @NonNull final List<TakenDose> doses) {
        final List<HistoryItem> entries =
                new ArrayList<>(glucose.size() + pressure.size() + doses.size());
        for (final GlucoseReading reading : glucose) {
            entries.add(HistoryItem.glucose(reading.getId(), reading.getValueMgDl(),
                    reading.getTakenAt()));
        }
        for (final BloodPressureReading reading : pressure) {
            entries.add(HistoryItem.bloodPressure(reading.getId(), reading.getSystolic(),
                    reading.getDiastolic(), reading.getTakenAt()));
        }
        for (final TakenDose dose : doses) {
            entries.add(HistoryItem.dose(dose.getId(), dose.getName(), dose.getDoseAmount(),
                    dose.getDoseUnit(), dose.getTakenAt()));
        }
        entries.sort(Comparator.comparingLong(HistoryItem::getTakenAt).reversed());
        return entries;
    }
}
