package com.davidrdz.vitallog.data.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.davidrdz.vitallog.data.entity.DoseLog;

import java.util.List;

@Dao
public interface DoseLogDao {

    /** Inserta la toma y devuelve el id asignado. Llamar fuera del hilo principal. */
    @Insert
    long insert(DoseLog doseLog);

    /**
     * Tomas confirmadas en [{@code startInclusive}, {@code endExclusive}) (p. ej. un día),
     * de la más antigua a la más reciente.
     */
    @Query("SELECT * FROM dose_logs WHERE taken_at >= :startInclusive AND taken_at < :endExclusive"
            + " ORDER BY taken_at, id")
    LiveData<List<DoseLog>> observeTakenBetween(long startInclusive, long endExclusive);
}
