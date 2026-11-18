package com.davidrdz.vitallog.alarms;

import java.util.concurrent.TimeUnit;

/** Reglas de tiempo de los recordatorios. Java puro. */
public final class ReminderPolicy {

    /** "Recordar más tarde" vuelve a avisar tras estos minutos. */
    public static final int SNOOZE_MINUTES = 10;
    /** El aviso de una cita llega estos minutos antes de la hora de la cita. */
    public static final int APPOINTMENT_LEAD_MINUTES = 120;

    private ReminderPolicy() {
    }

    /** Momento (epoch millis) en que vuelve a sonar un recordatorio pospuesto ahora. */
    public static long snoozeTriggerAt(final long nowMillis) {
        return nowMillis + TimeUnit.MINUTES.toMillis(SNOOZE_MINUTES);
    }
}
