package com.davidrdz.vitallog.data.db;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.davidrdz.vitallog.data.entity.Appointment;
import com.davidrdz.vitallog.data.entity.BloodPressureReading;
import com.davidrdz.vitallog.data.entity.DoseLog;
import com.davidrdz.vitallog.data.entity.GlucoseReading;
import com.davidrdz.vitallog.data.entity.Medication;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Base de datos local única de la app (CLAUDE.md §4.1). Offline: nada sale del dispositivo.
 *
 * <p>Sin {@code allowMainThreadQueries()} ni {@code fallbackToDestructiveMigration()}: las
 * escrituras van en {@link #getWriteExecutor()} y los cambios de esquema necesitan una migración
 * explícita (el esquema se exporta a {@code app/schemas/}).
 */
@Database(
        entities = {
                GlucoseReading.class,
                BloodPressureReading.class,
                Medication.class,
                DoseLog.class,
                Appointment.class
        },
        version = 1,
        exportSchema = true)
public abstract class AppDatabase extends RoomDatabase {

    public static final String DATABASE_NAME = "vitallog.db";

    /** Un solo hilo: las escrituras se aplican en el orden en que se piden. */
    private static final ExecutorService WRITE_EXECUTOR = Executors.newSingleThreadExecutor();

    private static volatile AppDatabase instance;

    public abstract GlucoseReadingDao glucoseReadingDao();

    public abstract BloodPressureReadingDao bloodPressureReadingDao();

    public abstract MedicationDao medicationDao();

    public abstract DoseLogDao doseLogDao();

    public abstract AppointmentDao appointmentDao();

    @NonNull
    public static AppDatabase getInstance(@NonNull final Context context) {
        AppDatabase result = instance;
        if (result == null) {
            synchronized (AppDatabase.class) {
                result = instance;
                if (result == null) {
                    result = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, DATABASE_NAME).build();
                    instance = result;
                }
            }
        }
        return result;
    }

    /** Executor compartido para escrituras; nunca escribir en el hilo principal. */
    @NonNull
    public static ExecutorService getWriteExecutor() {
        return WRITE_EXECUTOR;
    }
}
