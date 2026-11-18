package com.davidrdz.vitallog.data.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Medicamento con su dosis y la hora diaria de la toma. Inmutable.
 *
 * <p>Borrar un medicamento lo marca como inactivo ({@link #isActive()} = false) en lugar de
 * eliminar la fila, para conservar el historial de tomas ({@link DoseLog}).
 */
@Entity(tableName = "medications")
public final class Medication {

    /** Cantidad mínima por toma que permite el control +/−. */
    public static final int MIN_DOSE = 1;
    /** Cantidad máxima por toma que permite el control +/−. */
    public static final int MAX_DOSE = 100;

    @PrimaryKey(autoGenerate = true)
    private final long id;

    @NonNull
    @ColumnInfo(name = "name")
    private final String name;

    @ColumnInfo(name = "dose_amount")
    private final int doseAmount;

    @NonNull
    @ColumnInfo(name = "dose_unit")
    private final DoseUnit doseUnit;

    /** Hora de la toma diaria, 0–23. */
    @ColumnInfo(name = "hour")
    private final int hour;

    /** Minuto de la toma diaria, 0–59. */
    @ColumnInfo(name = "minute")
    private final int minute;

    @ColumnInfo(name = "active")
    private final boolean active;

    /** @param id 0 para un medicamento nuevo; Room asigna el id al insertarlo. */
    public Medication(final long id, @NonNull final String name, final int doseAmount,
                      @NonNull final DoseUnit doseUnit, final int hour, final int minute,
                      final boolean active) {
        this.id = id;
        this.name = name;
        this.doseAmount = doseAmount;
        this.doseUnit = doseUnit;
        this.hour = hour;
        this.minute = minute;
        this.active = active;
    }

    public long getId() {
        return id;
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

    public int getHour() {
        return hour;
    }

    public int getMinute() {
        return minute;
    }

    public boolean isActive() {
        return active;
    }

    /** Copia con el id asignado por la base de datos. */
    public Medication withId(final long newId) {
        return new Medication(newId, name, doseAmount, doseUnit, hour, minute, active);
    }

    /** Copia marcada como inactiva (borrada de la lista del usuario). */
    public Medication deactivated() {
        return new Medication(id, name, doseAmount, doseUnit, hour, minute, false);
    }
}
