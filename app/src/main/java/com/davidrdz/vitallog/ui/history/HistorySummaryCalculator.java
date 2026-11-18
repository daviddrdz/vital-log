package com.davidrdz.vitallog.ui.history;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.function.ToIntFunction;

/**
 * Cálculos del historial en Java puro: qué registros entran en "Últimos 7/30 días" y el resumen
 * (promedio redondeado, mínimo y máximo) de lo que queda.
 *
 * <p>"Últimos N días" son N días de calendario contando hoy: desde las 00:00 de hace N − 1 días.
 */
public final class HistorySummaryCalculator {

    private HistorySummaryCalculator() {
    }

    /** Primer instante (epoch millis, inclusive) del periodo, en la zona {@code zone}. */
    public static long windowStart(@NonNull final HistoryPeriod period, final long nowMillis,
                                   @NonNull final ZoneId zone) {
        return Instant.ofEpochMilli(nowMillis).atZone(zone).toLocalDate()
                .minusDays(period.getDays() - 1L)
                .atStartOfDay(zone).toInstant().toEpochMilli();
    }

    /** Registros de {@code entries} que caen dentro del periodo; conserva el orden. */
    @NonNull
    public static List<HistoryItem> filter(@NonNull final List<HistoryItem> entries,
                                           @NonNull final HistoryPeriod period,
                                           final long nowMillis, @NonNull final ZoneId zone) {
        final long start = windowStart(period, nowMillis, zone);
        final List<HistoryItem> inPeriod = new ArrayList<>();
        for (final HistoryItem entry : entries) {
            if (entry.getTakenAt() >= start) {
                inPeriod.add(entry);
            }
        }
        return inPeriod;
    }

    /** Resumen de {@code entries} (registros, sin encabezados de día). */
    @NonNull
    public static HistorySummary summarize(@NonNull final List<HistoryItem> entries) {
        final List<HistoryItem> glucose = new ArrayList<>();
        final List<HistoryItem> pressure = new ArrayList<>();
        int doses = 0;
        for (final HistoryItem entry : entries) {
            switch (entry.getKind()) {
                case GLUCOSE:
                    glucose.add(entry);
                    break;
                case BLOOD_PRESSURE:
                    pressure.add(entry);
                    break;
                case DOSE:
                    doses++;
                    break;
                case DAY_HEADER:
                    break;
                default:
                    throw new IllegalArgumentException("Unknown kind: " + entry.getKind());
            }
        }
        return new HistorySummary(stats(glucose, HistoryItem::getValue),
                stats(pressure, HistoryItem::getValue),
                stats(pressure, HistoryItem::getSecondValue),
                doses);
    }

    /** Promedio redondeado al entero más cercano (x.5 sube), mínimo y máximo; null si no hay. */
    @Nullable
    private static HistorySummary.Stats stats(@NonNull final List<HistoryItem> entries,
                                              @NonNull final ToIntFunction<HistoryItem> value) {
        if (entries.isEmpty()) {
            return null;
        }
        long sum = 0;
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        for (final HistoryItem entry : entries) {
            final int v = value.applyAsInt(entry);
            sum += v;
            min = Math.min(min, v);
            max = Math.max(max, v);
        }
        final int average = (int) Math.round((double) sum / entries.size());
        return new HistorySummary.Stats(average, min, max);
    }
}
