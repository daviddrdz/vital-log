package com.davidrdz.vitallog.alarms;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.OptionalLong;
import java.util.concurrent.TimeUnit;

/** Calcula cuándo debe sonar el próximo recordatorio. Java puro (java.time). */
public final class NextTriggerCalculator {

    private final ZoneId zone;

    public NextTriggerCalculator(final ZoneId zone) {
        this.zone = zone;
    }

    /**
     * Próxima toma diaria estrictamente posterior a {@code nowMillis}. Si la hora no existe ese
     * día por el cambio de horario de verano, se usa la hora válida siguiente.
     */
    public long nextDailyTrigger(final int hour, final int minute, final long nowMillis) {
        final ZonedDateTime now = Instant.ofEpochMilli(nowMillis).atZone(zone);
        final LocalDate today = now.toLocalDate();
        ZonedDateTime candidate = today.atTime(hour, minute).atZone(zone);
        if (!candidate.isAfter(now)) {
            candidate = today.plusDays(1).atTime(hour, minute).atZone(zone);
        }
        return candidate.toInstant().toEpochMilli();
    }

    /**
     * Momento del aviso de una cita ({@link ReminderPolicy#APPOINTMENT_LEAD_MINUTES} antes), o
     * vacío si ese momento ya pasó.
     */
    public OptionalLong appointmentTrigger(final long scheduledAtMillis, final long nowMillis) {
        final long trigger = scheduledAtMillis
                - TimeUnit.MINUTES.toMillis(ReminderPolicy.APPOINTMENT_LEAD_MINUTES);
        return trigger > nowMillis ? OptionalLong.of(trigger) : OptionalLong.empty();
    }
}
