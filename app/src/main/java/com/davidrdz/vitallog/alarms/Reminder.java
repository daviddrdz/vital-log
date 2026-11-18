package com.davidrdz.vitallog.alarms;

/**
 * Un recordatorio concreto: qué se recuerda ({@link ReminderType} + id) y para qué momento estaba
 * programado. Al posponerlo se conserva {@code scheduledAt}, para que la toma quede registrada con
 * su hora original. Java puro, inmutable.
 */
public final class Reminder {

    private final ReminderType type;
    private final long id;
    private final long scheduledAt;

    public Reminder(final ReminderType type, final long id, final long scheduledAt) {
        this.type = type;
        this.id = id;
        this.scheduledAt = scheduledAt;
    }

    public ReminderType getType() {
        return type;
    }

    public long getId() {
        return id;
    }

    /** Momento programado (epoch millis): la toma diaria o el aviso de la cita. */
    public long getScheduledAt() {
        return scheduledAt;
    }

    /** Id de la notificación y código de su alarma principal. */
    public int notificationId() {
        return ReminderRequestCodes.reminder(type, id);
    }
}
