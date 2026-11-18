package com.davidrdz.vitallog.data.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Registro de una toma de medicamento ("Ya lo tomé"). Inmutable.
 *
 * <p>La clave foránea usa {@code RESTRICT}: no se puede eliminar físicamente un medicamento que
 * tenga tomas registradas, para no perder historial clínico.
 */
@Entity(tableName = "dose_logs",
        foreignKeys = @ForeignKey(entity = Medication.class,
                parentColumns = "id",
                childColumns = "medication_id",
                onDelete = ForeignKey.RESTRICT),
        indices = {@Index("medication_id"), @Index("taken_at")})
public final class DoseLog {

    @PrimaryKey(autoGenerate = true)
    private final long id;

    @ColumnInfo(name = "medication_id")
    private final long medicationId;

    /** Momento en que tocaba la toma, epoch millis. */
    @ColumnInfo(name = "scheduled_at")
    private final long scheduledAt;

    /** Momento en que el usuario confirmó la toma, epoch millis. */
    @ColumnInfo(name = "taken_at")
    private final long takenAt;

    /** @param id 0 para una toma nueva; Room asigna el id al insertarla. */
    public DoseLog(final long id, final long medicationId, final long scheduledAt,
                   final long takenAt) {
        this.id = id;
        this.medicationId = medicationId;
        this.scheduledAt = scheduledAt;
        this.takenAt = takenAt;
    }

    public long getId() {
        return id;
    }

    public long getMedicationId() {
        return medicationId;
    }

    public long getScheduledAt() {
        return scheduledAt;
    }

    public long getTakenAt() {
        return takenAt;
    }

    /** Copia con el id asignado por la base de datos. */
    public DoseLog withId(final long newId) {
        return new DoseLog(newId, medicationId, scheduledAt, takenAt);
    }
}
