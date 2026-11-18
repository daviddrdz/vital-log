package com.davidrdz.vitallog.data.repository;

import com.davidrdz.vitallog.alarms.ReminderScheduler;
import com.davidrdz.vitallog.alarms.ReminderType;
import com.davidrdz.vitallog.data.entity.Appointment;
import com.davidrdz.vitallog.data.entity.Medication;

import java.util.ArrayList;
import java.util.List;

/** Registra las llamadas al programador de alarmas para comprobarlas en las pruebas. */
final class FakeReminderScheduler implements ReminderScheduler {

    final List<Long> scheduledMedicationIds = new ArrayList<>();
    final List<Long> scheduledAppointmentIds = new ArrayList<>();
    /** Entradas con el formato "TIPO:id". */
    final List<String> cancelled = new ArrayList<>();

    @Override
    public void scheduleMedication(final Medication medication) {
        scheduledMedicationIds.add(medication.getId());
    }

    @Override
    public void scheduleAppointment(final Appointment appointment) {
        scheduledAppointmentIds.add(appointment.getId());
    }

    @Override
    public void cancel(final ReminderType type, final long id) {
        cancelled.add(type.name() + ":" + id);
    }
}
