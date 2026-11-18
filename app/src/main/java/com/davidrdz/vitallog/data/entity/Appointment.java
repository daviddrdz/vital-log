package com.davidrdz.vitallog.data.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Cita médica: con quién o dónde y cuándo. Inmutable.
 *
 * <p>Borrar una cita la marca como inactiva ({@link #isActive()} = false) en lugar de eliminar la
 * fila.
 */
@Entity(tableName = "appointments", indices = @Index("scheduled_at"))
public final class Appointment {

    /** Hasta cuántos días en el futuro se puede agendar una cita con el control +/−. */
    public static final int MAX_DAYS_AHEAD = 365;

    @PrimaryKey(autoGenerate = true)
    private final long id;

    @NonNull
    @ColumnInfo(name = "doctor_or_place")
    private final String doctorOrPlace;

    /** Fecha y hora de la cita, epoch millis. */
    @ColumnInfo(name = "scheduled_at")
    private final long scheduledAt;

    @ColumnInfo(name = "active")
    private final boolean active;

    /** @param id 0 para una cita nueva; Room asigna el id al insertarla. */
    public Appointment(final long id, @NonNull final String doctorOrPlace, final long scheduledAt,
                       final boolean active) {
        this.id = id;
        this.doctorOrPlace = doctorOrPlace;
        this.scheduledAt = scheduledAt;
        this.active = active;
    }

    public long getId() {
        return id;
    }

    @NonNull
    public String getDoctorOrPlace() {
        return doctorOrPlace;
    }

    public long getScheduledAt() {
        return scheduledAt;
    }

    public boolean isActive() {
        return active;
    }

    /** Copia con el id asignado por la base de datos. */
    public Appointment withId(final long newId) {
        return new Appointment(newId, doctorOrPlace, scheduledAt, active);
    }

    /** Copia marcada como inactiva (borrada de la lista del usuario). */
    public Appointment deactivated() {
        return new Appointment(id, doctorOrPlace, scheduledAt, false);
    }
}
