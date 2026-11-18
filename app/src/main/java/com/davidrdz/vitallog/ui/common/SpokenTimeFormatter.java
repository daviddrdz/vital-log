package com.davidrdz.vitallog.ui.common;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

/**
 * Formatea horas y días como se dicen en voz alta (Java puro, Locale es-MX):
 * "8:30 de la mañana", "12:00 del mediodía", "hoy", "lunes 16 de noviembre".
 *
 * <p>Las palabras y patrones llegan en {@link Labels} desde strings.xml (los arma
 * {@code VitalLogApp}), así no hay textos de UI en el código. Los nombres de días y meses los da
 * {@code java.time} con {@link #LOCALE}.
 */
public final class SpokenTimeFormatter {

    public static final Locale LOCALE = Locale.forLanguageTag("es-MX");

    private static final int HOURS_PER_HALF_DAY = 12;
    private static final int NOON = 12;
    /** Primera hora de cada periodo del día. */
    private static final int EARLY_MORNING_FROM = 1;
    private static final int MORNING_FROM = 6;
    private static final int NIGHT_FROM = 19;
    private static final int MAX_HOUR = 23;
    private static final int MAX_MINUTE = 59;

    /** Palabras y patrones del formateador, tomados de strings.xml. */
    public static final class Labels {
        final String timePattern;
        final String earlyMorning;
        final String morning;
        final String noon;
        final String afternoon;
        final String night;
        final String today;
        final String yesterday;
        final String tomorrow;
        final String datePattern;
        final String joinPattern;

        /**
         * @param timePattern  {@code String.format} con hora (%1$d), minutos (%2$d) y periodo (%3$s).
         * @param datePattern  patrón de {@link DateTimeFormatter}, p. ej. "EEEE d 'de' MMMM".
         * @param joinPattern  une dos partes, p. ej. "%1$s, %2$s" → "hoy, 8:30 de la mañana".
         */
        public Labels(@NonNull final String timePattern, @NonNull final String earlyMorning,
                      @NonNull final String morning, @NonNull final String noon,
                      @NonNull final String afternoon, @NonNull final String night,
                      @NonNull final String today, @NonNull final String yesterday,
                      @NonNull final String tomorrow, @NonNull final String datePattern,
                      @NonNull final String joinPattern) {
            this.timePattern = timePattern;
            this.earlyMorning = earlyMorning;
            this.morning = morning;
            this.noon = noon;
            this.afternoon = afternoon;
            this.night = night;
            this.today = today;
            this.yesterday = yesterday;
            this.tomorrow = tomorrow;
            this.datePattern = datePattern;
            this.joinPattern = joinPattern;
        }
    }

    @NonNull
    private final ZoneId zone;
    @NonNull
    private final Labels labels;
    @NonNull
    private final DateTimeFormatter dateFormatter;

    public SpokenTimeFormatter(@NonNull final ZoneId zone, @NonNull final Labels labels) {
        this.zone = zone;
        this.labels = labels;
        this.dateFormatter = DateTimeFormatter.ofPattern(labels.datePattern, LOCALE);
    }

    /** "8:30 de la mañana", "12:00 del mediodía", "12:00 de la noche" (medianoche). */
    @NonNull
    public String formatTime(final int hour, final int minute) {
        requireTime(hour, minute);
        return String.format(LOCALE, labels.timePattern,
                toTwelveHour(hour), minute, dayPeriod(hour, minute));
    }

    /** Hora del instante dado en la zona del formateador. */
    @NonNull
    public String formatTime(final long epochMillis) {
        final ZonedDateTime time = toZoned(epochMillis);
        return formatTime(time.getHour(), time.getMinute());
    }

    /** Hora en formato de 12 horas: 0 → 12, 13 → 1. */
    public static int toTwelveHour(final int hour) {
        final int twelve = hour % HOURS_PER_HALF_DAY;
        return twelve == 0 ? HOURS_PER_HALF_DAY : twelve;
    }

    /** "de la madrugada", "de la mañana", "del mediodía", "de la tarde" o "de la noche". */
    @NonNull
    public String dayPeriod(final int hour, final int minute) {
        requireTime(hour, minute);
        if (hour == NOON && minute == 0) {
            return labels.noon;
        }
        if (hour >= NIGHT_FROM || hour < EARLY_MORNING_FROM) {
            return labels.night;
        }
        if (hour < MORNING_FROM) {
            return labels.earlyMorning;
        }
        if (hour < NOON) {
            return labels.morning;
        }
        return labels.afternoon;
    }

    /** "hoy", "ayer", "mañana" o la fecha completa ("lunes 16 de noviembre"). */
    @NonNull
    public String formatDay(final long epochMillis, final long nowMillis) {
        final String relative = relativeDay(epochMillis, nowMillis);
        return relative != null ? relative : formatDate(epochMillis);
    }

    /** Fecha completa: "lunes 16 de noviembre". */
    @NonNull
    public String formatDate(final long epochMillis) {
        return dateFormatter.format(toZoned(epochMillis));
    }

    /** "mañana, miércoles 18 de noviembre"; si no es hoy, ayer ni mañana, solo la fecha. */
    @NonNull
    public String formatDayWithDate(final long epochMillis, final long nowMillis) {
        final String relative = relativeDay(epochMillis, nowMillis);
        final String date = formatDate(epochMillis);
        return relative == null ? date : String.format(LOCALE, labels.joinPattern, relative, date);
    }

    /** "hoy, 8:30 de la mañana" o "lunes 16 de noviembre, 8:30 de la mañana". */
    @NonNull
    public String formatDateTime(final long epochMillis, final long nowMillis) {
        return String.format(LOCALE, labels.joinPattern,
                formatDay(epochMillis, nowMillis), formatTime(epochMillis));
    }

    @Nullable
    private String relativeDay(final long epochMillis, final long nowMillis) {
        final LocalDate day = toZoned(epochMillis).toLocalDate();
        final LocalDate today = toZoned(nowMillis).toLocalDate();
        final long difference = ChronoUnit.DAYS.between(today, day);
        if (difference == 0) {
            return labels.today;
        }
        if (difference == -1) {
            return labels.yesterday;
        }
        if (difference == 1) {
            return labels.tomorrow;
        }
        return null;
    }

    private ZonedDateTime toZoned(final long epochMillis) {
        return Instant.ofEpochMilli(epochMillis).atZone(zone);
    }

    private static void requireTime(final int hour, final int minute) {
        if (hour < 0 || hour > MAX_HOUR || minute < 0 || minute > MAX_MINUTE) {
            throw new IllegalArgumentException("Invalid time " + hour + ":" + minute);
        }
    }
}
