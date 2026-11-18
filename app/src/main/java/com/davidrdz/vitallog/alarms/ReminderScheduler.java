package com.davidrdz.vitallog.alarms;

import com.davidrdz.vitallog.data.entity.Appointment;
import com.davidrdz.vitallog.data.entity.Medication;

/**
 * Programa y cancela recordatorios. Los repositorios lo llaman al guardar o borrar, así las
 * alarmas siempre coinciden con lo que hay en la base de datos. Las pruebas JVM usan un falso.
 */
public interface ReminderScheduler {

    /** Programa la próxima toma diaria (o la cancela si el medicamento está inactivo). */
    void scheduleMedication(Medication medication);

    /** Programa el aviso de la cita (o lo cancela si ya pasó o la cita está inactiva). */
    void scheduleAppointment(Appointment appointment);

    /** Cancela la alarma principal y la pospuesta del recordatorio. */
    void cancel(ReminderType type, long id);
}
