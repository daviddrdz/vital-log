package com.davidrdz.vitallog.ui.history;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.davidrdz.vitallog.analytics.ReadingCategory;
import com.davidrdz.vitallog.analytics.ReadingClassifier;
import com.davidrdz.vitallog.data.entity.DoseUnit;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Fila del historial (Java puro): el encabezado de un día o un registro unificado de azúcar,
 * presión arterial o toma de medicamento. Las lecturas llevan su categoría informativa de
 * {@link ReadingClassifier}. Inmutable.
 */
public final class HistoryItem {

    /** Tipo de fila. */
    public enum Kind {
        DAY_HEADER,
        GLUCOSE,
        BLOOD_PRESSURE,
        DOSE
    }

    @NonNull
    private final Kind kind;
    /** Id de la fila en su tabla, o el día (epoch day) en un encabezado. */
    private final long id;
    /** Momento del registro, epoch millis; en un encabezado, el de su primer registro. */
    private final long takenAt;
    /** Glucosa, presión de arriba o cantidad de la dosis. */
    private final int value;
    /** Presión de abajo; 0 en los demás tipos. */
    private final int secondValue;
    @Nullable
    private final ReadingCategory category;
    @Nullable
    private final String medicationName;
    @Nullable
    private final DoseUnit doseUnit;

    private HistoryItem(@NonNull final Kind kind, final long id, final long takenAt,
                        final int value, final int secondValue,
                        @Nullable final ReadingCategory category,
                        @Nullable final String medicationName,
                        @Nullable final DoseUnit doseUnit) {
        this.kind = kind;
        this.id = id;
        this.takenAt = takenAt;
        this.value = value;
        this.secondValue = secondValue;
        this.category = category;
        this.medicationName = medicationName;
        this.doseUnit = doseUnit;
    }

    @NonNull
    public static HistoryItem glucose(final long id, final int valueMgDl, final long takenAt) {
        return new HistoryItem(Kind.GLUCOSE, id, takenAt, valueMgDl, 0,
                ReadingClassifier.classifyGlucose(valueMgDl), null, null);
    }

    @NonNull
    public static HistoryItem bloodPressure(final long id, final int systolic,
                                            final int diastolic, final long takenAt) {
        return new HistoryItem(Kind.BLOOD_PRESSURE, id, takenAt, systolic, diastolic,
                ReadingClassifier.classifyBloodPressure(systolic, diastolic), null, null);
    }

    @NonNull
    public static HistoryItem dose(final long id, @NonNull final String medicationName,
                                   final int amount, @NonNull final DoseUnit unit,
                                   final long takenAt) {
        return new HistoryItem(Kind.DOSE, id, takenAt, amount, 0, null, medicationName, unit);
    }

    @NonNull
    private static HistoryItem dayHeader(@NonNull final LocalDate day, final long takenAt) {
        return new HistoryItem(Kind.DAY_HEADER, day.toEpochDay(), takenAt, 0, 0, null, null,
                null);
    }

    /**
     * Inserta un encabezado antes del primer registro de cada día (en {@code zone}).
     *
     * @param entries registros ya ordenados, del más reciente al más antiguo.
     */
    @NonNull
    public static List<HistoryItem> groupByDay(@NonNull final List<HistoryItem> entries,
                                               @NonNull final ZoneId zone) {
        final List<HistoryItem> rows = new ArrayList<>(entries.size() * 2);
        LocalDate currentDay = null;
        for (final HistoryItem entry : entries) {
            final LocalDate day = Instant.ofEpochMilli(entry.takenAt).atZone(zone).toLocalDate();
            if (!day.equals(currentDay)) {
                rows.add(dayHeader(day, entry.takenAt));
                currentDay = day;
            }
            rows.add(entry);
        }
        return rows;
    }

    @NonNull
    public Kind getKind() {
        return kind;
    }

    public long getId() {
        return id;
    }

    public long getTakenAt() {
        return takenAt;
    }

    /** Glucosa (mg/dL), presión de arriba (mmHg) o cantidad de la dosis. */
    public int getValue() {
        return value;
    }

    /** Presión de abajo (mmHg); solo en {@link Kind#BLOOD_PRESSURE}. */
    public int getSecondValue() {
        return secondValue;
    }

    /** Categoría informativa; solo en lecturas de azúcar o presión. */
    @Nullable
    public ReadingCategory getCategory() {
        return category;
    }

    /** Nombre del medicamento; solo en {@link Kind#DOSE}. */
    @Nullable
    public String getMedicationName() {
        return medicationName;
    }

    /** Unidad de la dosis; solo en {@link Kind#DOSE}. */
    @Nullable
    public DoseUnit getDoseUnit() {
        return doseUnit;
    }

    @Override
    public boolean equals(@Nullable final Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof HistoryItem)) {
            return false;
        }
        final HistoryItem that = (HistoryItem) other;
        return kind == that.kind && id == that.id && takenAt == that.takenAt
                && value == that.value && secondValue == that.secondValue
                && category == that.category && doseUnit == that.doseUnit
                && Objects.equals(medicationName, that.medicationName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(kind, id, takenAt, value, secondValue, category, medicationName,
                doseUnit);
    }
}
