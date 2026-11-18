package com.davidrdz.vitallog.ui.history;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.davidrdz.vitallog.analytics.ReadingCategory;
import com.davidrdz.vitallog.data.entity.DoseUnit;

import org.junit.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class HistorySummaryCalculatorTest {

    private static final ZoneId ZONE = ZoneId.of("America/Mexico_City");
    /** Martes 17 de noviembre de 2026, 10:00. */
    private static final long NOW = millis(2026, 11, 17, 10, 0);

    private static long millis(final int year, final int month, final int day, final int hour,
                               final int minute) {
        return LocalDateTime.of(year, month, day, hour, minute)
                .atZone(ZONE).toInstant().toEpochMilli();
    }

    // ===== Resumen =====

    @Test
    public void emptyList_hasNoData() {
        final HistorySummary summary =
                HistorySummaryCalculator.summarize(Collections.emptyList());
        assertTrue(summary.isEmpty());
        assertNull(summary.getGlucose());
        assertNull(summary.getSystolic());
        assertNull(summary.getDiastolic());
        assertEquals(0, summary.getDoseCount());
    }

    @Test
    public void onlyDoses_hasNoReadingDataButIsNotEmpty() {
        final HistorySummary summary = HistorySummaryCalculator.summarize(Collections.singletonList(
                HistoryItem.dose(1, "Metformina", 1, DoseUnit.TABLET, NOW)));
        assertFalse(summary.isEmpty());
        assertNull(summary.getGlucose());
        assertNull(summary.getSystolic());
        assertEquals(1, summary.getDoseCount());
    }

    @Test
    public void glucose_averageIsRoundedToNearest() {
        // (100 + 101) / 2 = 100.5 → 101
        final HistorySummary up = HistorySummaryCalculator.summarize(Arrays.asList(
                HistoryItem.glucose(1, 100, NOW), HistoryItem.glucose(2, 101, NOW)));
        assertEquals(101, up.getGlucose().getAverage());
        // (100 + 100 + 101) / 3 = 100.33 → 100
        final HistorySummary down = HistorySummaryCalculator.summarize(Arrays.asList(
                HistoryItem.glucose(1, 100, NOW), HistoryItem.glucose(2, 100, NOW),
                HistoryItem.glucose(3, 101, NOW)));
        assertEquals(100, down.getGlucose().getAverage());
    }

    @Test
    public void glucose_minAndMax() {
        final HistorySummary.Stats stats = HistorySummaryCalculator.summarize(Arrays.asList(
                HistoryItem.glucose(1, 140, NOW), HistoryItem.glucose(2, 65, NOW),
                HistoryItem.glucose(3, 320, NOW))).getGlucose();
        assertNotNull(stats);
        assertEquals(65, stats.getMin());
        assertEquals(320, stats.getMax());
        assertEquals(175, stats.getAverage());
    }

    @Test
    public void pressure_eachNumberHasItsOwnStats() {
        final HistorySummary summary = HistorySummaryCalculator.summarize(Arrays.asList(
                HistoryItem.bloodPressure(1, 150, 70, NOW),
                HistoryItem.bloodPressure(2, 110, 95, NOW)));
        assertEquals(130, summary.getSystolic().getAverage());
        assertEquals(110, summary.getSystolic().getMin());
        assertEquals(150, summary.getSystolic().getMax());
        // (70 + 95) / 2 = 82.5 → 83
        assertEquals(83, summary.getDiastolic().getAverage());
        assertEquals(70, summary.getDiastolic().getMin());
        assertEquals(95, summary.getDiastolic().getMax());
        assertNull(summary.getGlucose());
    }

    @Test
    public void doses_areCounted_andHeadersIgnored() {
        final List<HistoryItem> rows = HistoryItem.groupByDay(Arrays.asList(
                HistoryItem.dose(1, "Losartán", 1, DoseUnit.TABLET, NOW),
                HistoryItem.dose(2, "Metformina", 2, DoseUnit.TABLET, millis(2026, 11, 16, 8, 0)),
                HistoryItem.glucose(3, 120, millis(2026, 11, 16, 7, 0))), ZONE);
        final HistorySummary summary = HistorySummaryCalculator.summarize(rows);
        assertEquals(2, summary.getDoseCount());
        assertEquals(120, summary.getGlucose().getAverage());
    }

    @Test
    public void readings_areClassifiedWithReadingClassifier() {
        assertEquals(ReadingCategory.CRITICAL, HistoryItem.glucose(1, 320, NOW).getCategory());
        assertEquals(ReadingCategory.HIGH,
                HistoryItem.bloodPressure(1, 150, 55, NOW).getCategory());
        assertNull(HistoryItem.dose(1, "Losartán", 1, DoseUnit.TABLET, NOW).getCategory());
    }

    // ===== Periodos =====

    @Test
    public void sevenDays_startsAtMidnightSixDaysAgo() {
        assertEquals(millis(2026, 11, 11, 0, 0),
                HistorySummaryCalculator.windowStart(HistoryPeriod.LAST_7_DAYS, NOW, ZONE));
    }

    @Test
    public void thirtyDays_startsAtMidnightTwentyNineDaysAgo() {
        assertEquals(millis(2026, 10, 19, 0, 0),
                HistorySummaryCalculator.windowStart(HistoryPeriod.LAST_30_DAYS, NOW, ZONE));
    }

    @Test
    public void sevenDays_boundaries() {
        final long start = millis(2026, 11, 11, 0, 0);
        final HistoryItem firstIncluded = HistoryItem.glucose(1, 100, start);
        final HistoryItem lastExcluded = HistoryItem.glucose(2, 100, start - 1);
        final HistoryItem today = HistoryItem.glucose(3, 100, NOW);
        assertEquals(Arrays.asList(today, firstIncluded), HistorySummaryCalculator.filter(
                Arrays.asList(today, firstIncluded, lastExcluded),
                HistoryPeriod.LAST_7_DAYS, NOW, ZONE));
    }

    @Test
    public void thirtyDays_boundaries() {
        final long start = millis(2026, 10, 19, 0, 0);
        final HistoryItem firstIncluded = HistoryItem.bloodPressure(1, 120, 80, start);
        final HistoryItem lastExcluded = HistoryItem.bloodPressure(2, 120, 80, start - 1);
        final HistoryItem eightDaysAgo =
                HistoryItem.bloodPressure(3, 120, 80, millis(2026, 11, 9, 9, 0));
        final List<HistoryItem> entries = Arrays.asList(eightDaysAgo, firstIncluded, lastExcluded);
        assertEquals(Arrays.asList(eightDaysAgo, firstIncluded), HistorySummaryCalculator.filter(
                entries, HistoryPeriod.LAST_30_DAYS, NOW, ZONE));
        assertTrue(HistorySummaryCalculator.filter(
                entries, HistoryPeriod.LAST_7_DAYS, NOW, ZONE).isEmpty());
    }

    // ===== Agrupado por día =====

    @Test
    public void groupByDay_insertsOneHeaderPerLocalDay() {
        final HistoryItem lateToday = HistoryItem.glucose(1, 100, millis(2026, 11, 17, 9, 0));
        final HistoryItem earlyToday = HistoryItem.glucose(2, 110, millis(2026, 11, 17, 0, 5));
        final HistoryItem lateYesterday =
                HistoryItem.glucose(3, 120, millis(2026, 11, 16, 23, 55));
        final List<HistoryItem> rows = HistoryItem.groupByDay(
                Arrays.asList(lateToday, earlyToday, lateYesterday), ZONE);
        assertEquals(5, rows.size());
        assertEquals(HistoryItem.Kind.DAY_HEADER, rows.get(0).getKind());
        assertEquals(lateToday, rows.get(1));
        assertEquals(earlyToday, rows.get(2));
        assertEquals(HistoryItem.Kind.DAY_HEADER, rows.get(3).getKind());
        assertEquals(lateYesterday, rows.get(4));
    }

    @Test
    public void groupByDay_emptyListHasNoHeaders() {
        assertTrue(HistoryItem.groupByDay(Collections.emptyList(), ZONE).isEmpty());
    }
}
