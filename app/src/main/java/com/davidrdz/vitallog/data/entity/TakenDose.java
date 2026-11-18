package com.davidrdz.vitallog.data.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;

/**
 * Resultado de consulta (no es una tabla): una toma registrada junto con el nombre y la dosis de
 * su medicamento, para el historial. Inmutable.
 */
public final class TakenDose {

    @ColumnInfo(name = "id")
    private final long id;

    /** Momento en que el usuario confirmó la toma, epoch millis. */
    @ColumnInfo(name = "taken_at")
    private final long takenAt;

    @NonNull
    @ColumnInfo(name = "name")
    private final String name;

    @ColumnInfo(name = "dose_amount")
    private final int doseAmount;

    @NonNull
    @ColumnInfo(name = "dose_unit")
    private final DoseUnit doseUnit;

    public TakenDose(final long id, final long takenAt, @NonNull final String name,
                     final int doseAmount, @NonNull final DoseUnit doseUnit) {
        this.id = id;
        this.takenAt = takenAt;
        this.name = name;
        this.doseAmount = doseAmount;
        this.doseUnit = doseUnit;
    }

    public long getId() {
        return id;
    }

    public long getTakenAt() {
        return takenAt;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public int getDoseAmount() {
        return doseAmount;
    }

    @NonNull
    public DoseUnit getDoseUnit() {
        return doseUnit;
    }
}
