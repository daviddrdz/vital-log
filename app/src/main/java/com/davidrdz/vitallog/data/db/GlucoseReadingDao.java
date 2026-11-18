package com.davidrdz.vitallog.data.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.davidrdz.vitallog.data.entity.GlucoseReading;

import java.util.List;

@Dao
public interface GlucoseReadingDao {

    /** Inserta la lectura y devuelve el id asignado. Llamar fuera del hilo principal. */
    @Insert
    long insert(GlucoseReading reading);

    /** Todas las lecturas, de la más reciente a la más antigua. */
    @Query("SELECT * FROM glucose_readings ORDER BY taken_at DESC, id DESC")
    LiveData<List<GlucoseReading>> observeAll();

    /** La lectura más reciente, o {@code null} si no hay ninguna. */
    @Query("SELECT * FROM glucose_readings ORDER BY taken_at DESC, id DESC LIMIT 1")
    LiveData<GlucoseReading> observeLatest();
}
