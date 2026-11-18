package com.davidrdz.vitallog.alarms;

/**
 * Códigos únicos para los {@code PendingIntent} y las notificaciones de los recordatorios. Java
 * puro.
 *
 * <p>Cada combinación (tipo, id, pospuesto o no) recibe un código distinto, para que posponer un
 * recordatorio no reemplace ni cancele su alarma diaria.
 */
public final class ReminderRequestCodes {

    /** Ranuras por id: medicamento, cita, medicamento pospuesto, cita pospuesta. */
    private static final int SLOTS_PER_ID = 4;
    private static final int MEDICATION_SLOT = 0;
    private static final int APPOINTMENT_SLOT = 1;
    private static final int SNOOZE_OFFSET = 2;

    private ReminderRequestCodes() {
    }

    /** Código de la alarma principal (diaria o de cita). También es el id de su notificación. */
    public static int reminder(final ReminderType type, final long id) {
        return code(id, baseSlot(type));
    }

    /** Código de la alarma única de "Recordar más tarde". */
    public static int snooze(final ReminderType type, final long id) {
        return code(id, baseSlot(type) + SNOOZE_OFFSET);
    }

    private static int baseSlot(final ReminderType type) {
        return type == ReminderType.MEDICATION ? MEDICATION_SLOT : APPOINTMENT_SLOT;
    }

    private static int code(final long id, final int slot) {
        if (id <= 0) {
            throw new IllegalArgumentException("El id debe estar guardado (> 0): " + id);
        }
        return Math.toIntExact(Math.addExact(Math.multiplyExact(id, SLOTS_PER_ID), slot));
    }
}
