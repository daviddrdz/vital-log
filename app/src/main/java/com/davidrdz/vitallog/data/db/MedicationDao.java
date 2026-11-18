package com.davidrdz.vitallog.data.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.davidrdz.vitallog.data.entity.Medication;

import java.util.List;

@Dao
public interface MedicationDao {

    /** Inserta el medicamento y devuelve el id asignado. Llamar fuera del hilo principal. */
    @Insert
    long insert(Medication medication);

    /** Medicamentos activos, ordenados por la hora de la toma. */
    @Query("SELECT * FROM medications WHERE active = 1 ORDER BY hour, minute, name")
    LiveData<List<Medication>> observeActive();

    /** Marca el medicamento como inactivo (borrado de la lista) sin perder sus tomas. */
    @Query("UPDATE medications SET active = 0 WHERE id = :id")
    int deactivate(long id);
}
