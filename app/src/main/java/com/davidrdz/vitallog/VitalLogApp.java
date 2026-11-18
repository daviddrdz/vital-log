package com.davidrdz.vitallog;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.davidrdz.vitallog.alarms.AlarmScheduler;
import com.davidrdz.vitallog.alarms.ReminderNotifier;
import com.davidrdz.vitallog.data.db.AppDatabase;
import com.davidrdz.vitallog.data.repository.AppointmentRepository;
import com.davidrdz.vitallog.data.repository.MedicationRepository;
import com.davidrdz.vitallog.data.repository.RepositoryExecutors;
import com.davidrdz.vitallog.data.repository.VitalsRepository;
import com.davidrdz.vitallog.ui.common.SpokenTimeFormatter;

import java.time.Clock;
import java.time.ZoneId;

/**
 * Inyección manual de dependencias: crea la base de datos y los repositorios una sola vez y los
 * expone a los ViewModels a través de {@link com.davidrdz.vitallog.ui.common.ViewModelFactory}.
 */
public class VitalLogApp extends Application {

    private VitalsRepository vitalsRepository;
    private MedicationRepository medicationRepository;
    private AppointmentRepository appointmentRepository;
    private SpokenTimeFormatter.Labels timeLabels;
    private AlarmScheduler alarmScheduler;
    private ReminderNotifier reminderNotifier;

    @Override
    public void onCreate() {
        super.onCreate();
        final AppDatabase database = AppDatabase.getInstance(this);
        final RepositoryExecutors executors = new RepositoryExecutors(
                AppDatabase.getWriteExecutor(), ContextCompat.getMainExecutor(this));
        ReminderNotifier.createChannel(this);
        reminderNotifier = new ReminderNotifier(this);
        alarmScheduler = new AlarmScheduler(this);
        vitalsRepository = new VitalsRepository(database.glucoseReadingDao(),
                database.bloodPressureReadingDao(), executors);
        medicationRepository = new MedicationRepository(database.medicationDao(),
                database.doseLogDao(), executors, alarmScheduler);
        appointmentRepository = new AppointmentRepository(database.appointmentDao(), executors,
                alarmScheduler);
        timeLabels = new SpokenTimeFormatter.Labels(
                getString(R.string.time_pattern),
                getString(R.string.time_period_early_morning),
                getString(R.string.time_period_morning),
                getString(R.string.time_period_noon),
                getString(R.string.time_period_afternoon),
                getString(R.string.time_period_night),
                getString(R.string.day_today),
                getString(R.string.day_yesterday),
                getString(R.string.day_tomorrow),
                getString(R.string.date_pattern),
                getString(R.string.join_pattern));
    }

    @NonNull
    public static VitalLogApp from(@NonNull final Context context) {
        return (VitalLogApp) context.getApplicationContext();
    }

    @NonNull
    public VitalsRepository getVitalsRepository() {
        return vitalsRepository;
    }

    @NonNull
    public MedicationRepository getMedicationRepository() {
        return medicationRepository;
    }

    @NonNull
    public AppointmentRepository getAppointmentRepository() {
        return appointmentRepository;
    }

    @NonNull
    public AlarmScheduler getAlarmScheduler() {
        return alarmScheduler;
    }

    @NonNull
    public ReminderNotifier getReminderNotifier() {
        return reminderNotifier;
    }

    /** Reloj del sistema en la zona horaria actual (se lee en cada llamada). */
    @NonNull
    public Clock getClock() {
        return Clock.systemDefaultZone();
    }

    /** Formateador de horas y días en la zona horaria actual. */
    @NonNull
    public SpokenTimeFormatter getTimeFormatter() {
        return new SpokenTimeFormatter(ZoneId.systemDefault(), timeLabels);
    }
}
