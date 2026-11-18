package com.davidrdz.vitallog.data.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/** Lectura de presión arterial en mmHg (número de arriba / número de abajo). Inmutable. */
@Entity(tableName = "blood_pressure_readings", indices = @Index("taken_at"))
public final class BloodPressureReading {

    @PrimaryKey(autoGenerate = true)
    private final long id;

    /** Presión sistólica ("número de arriba"). */
    @ColumnInfo(name = "systolic")
    private final int systolic;

    /** Presión diastólica ("número de abajo"). */
    @ColumnInfo(name = "diastolic")
    private final int diastolic;

    /** Momento de la medición, epoch millis. */
    @ColumnInfo(name = "taken_at")
    private final long takenAt;

    /** @param id 0 para una lectura nueva; Room asigna el id al insertarla. */
    public BloodPressureReading(final long id, final int systolic, final int diastolic,
                                final long takenAt) {
        this.id = id;
        this.systolic = systolic;
        this.diastolic = diastolic;
        this.takenAt = takenAt;
    }

    public long getId() {
        return id;
    }

    public int getSystolic() {
        return systolic;
    }

    public int getDiastolic() {
        return diastolic;
    }

    public long getTakenAt() {
        return takenAt;
    }

    /** Copia con el id asignado por la base de datos. */
    public BloodPressureReading withId(final long newId) {
        return new BloodPressureReading(newId, systolic, diastolic, takenAt);
    }
}
