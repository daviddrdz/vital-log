package com.davidrdz.vitallog.ui.history;

/** Periodos que se pueden ver en el historial: hoy y los días anteriores, en días de calendario. */
public enum HistoryPeriod {
    LAST_7_DAYS(7),
    LAST_30_DAYS(30);

    private final int days;

    HistoryPeriod(final int days) {
        this.days = days;
    }

    /** Días de calendario que abarca, contando hoy. */
    public int getDays() {
        return days;
    }
}
