package com.davidrdz.vitallog.data.db;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.davidrdz.vitallog.data.entity.Appointment;

import java.util.List;

@Dao
public interface AppointmentDao {

    /** Inserta la cita y devuelve el id asignado. Llamar fuera del hilo principal. */
    @Insert
    long insert(Appointment appointment);

    /** Citas activas desde {@code now} (inclusive), de la más próxima a la más lejana. */
    @Query("SELECT * FROM appointments WHERE active = 1 AND scheduled_at >= :now"
            + " ORDER BY scheduled_at, id")
    LiveData<List<Appointment>> observeUpcoming(long now);

    /** Citas activas desde {@code now} (consulta síncrona para reprogramar alarmas). */
    @Query("SELECT * FROM appointments WHERE active = 1 AND scheduled_at >= :now")
    List<Appointment> getUpcoming(long now);

    /** La cita si existe y está activa; null si no. Llamar fuera del hilo principal. */
    @Nullable
    @Query("SELECT * FROM appointments WHERE id = :id AND active = 1")
    Appointment findActiveById(long id);

    /** Marca la cita como inactiva (borrada de la lista). */
    @Query("UPDATE appointments SET active = 0 WHERE id = :id")
    int deactivate(long id);
}
