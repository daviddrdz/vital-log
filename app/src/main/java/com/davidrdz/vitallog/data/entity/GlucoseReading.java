package com.davidrdz.vitallog.data.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/** Lectura de azúcar en la sangre (glucosa) en mg/dL. Inmutable. */
@Entity(tableName = "glucose_readings", indices = @Index("taken_at"))
public final class GlucoseReading {

    @PrimaryKey(autoGenerate = true)
    private final long id;

    @ColumnInfo(name = "value_mg_dl")
    private final int valueMgDl;

    /** Momento de la medición, epoch millis. */
    @ColumnInfo(name = "taken_at")
    private final long takenAt;

    /** @param id 0 para una lectura nueva; Room asigna el id al insertarla. */
    public GlucoseReading(final long id, final int valueMgDl, final long takenAt) {
        this.id = id;
        this.valueMgDl = valueMgDl;
        this.takenAt = takenAt;
    }

    public long getId() {
        return id;
    }

    public int getValueMgDl() {
        return valueMgDl;
    }

    public long getTakenAt() {
        return takenAt;
    }

    /** Copia con el id asignado por la base de datos. */
    public GlucoseReading withId(final long newId) {
        return new GlucoseReading(newId, valueMgDl, takenAt);
    }
}
