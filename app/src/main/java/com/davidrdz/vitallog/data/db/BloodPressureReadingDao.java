package com.davidrdz.vitallog.data.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.davidrdz.vitallog.data.entity.BloodPressureReading;

import java.util.List;

@Dao
public interface BloodPressureReadingDao {

    /** Inserta la lectura y devuelve el id asignado. Llamar fuera del hilo principal. */
    @Insert
    long insert(BloodPressureReading reading);

    /** Todas las lecturas, de la más reciente a la más antigua. */
    @Query("SELECT * FROM blood_pressure_readings ORDER BY taken_at DESC, id DESC")
    LiveData<List<BloodPressureReading>> observeAll();

    /** La lectura más reciente, o {@code null} si no hay ninguna. */
    @Query("SELECT * FROM blood_pressure_readings ORDER BY taken_at DESC, id DESC LIMIT 1")
    LiveData<BloodPressureReading> observeLatest();
}
