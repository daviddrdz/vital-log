package com.davidrdz.vitallog.ui.common;

import androidx.activity.ComponentActivity;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.davidrdz.vitallog.VitalLogApp;
import com.davidrdz.vitallog.ui.agenda.AgendaViewModel;
import com.davidrdz.vitallog.ui.alarm.AlarmViewModel;
import com.davidrdz.vitallog.ui.glucose.GlucoseEntryViewModel;
import com.davidrdz.vitallog.ui.history.HistoryViewModel;
import com.davidrdz.vitallog.ui.pressure.PressureEntryViewModel;

/** Crea los ViewModels con los repositorios de {@link VitalLogApp}. */
public final class ViewModelFactory implements ViewModelProvider.Factory {

    @NonNull
    private final VitalLogApp app;

    public ViewModelFactory(@NonNull final VitalLogApp app) {
        this.app = app;
    }

    /** Atajo para obtener el ViewModel de una Activity. */
    @NonNull
    public static <T extends ViewModel> T get(@NonNull final ComponentActivity activity,
                                              @NonNull final Class<T> modelClass) {
        return new ViewModelProvider(activity, new ViewModelFactory(VitalLogApp.from(activity)))
                .get(modelClass);
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull final Class<T> modelClass) {
        if (modelClass == GlucoseEntryViewModel.class) {
            return modelClass.cast(
                    new GlucoseEntryViewModel(app.getVitalsRepository(), app.getClock()));
        }
        if (modelClass == PressureEntryViewModel.class) {
            return modelClass.cast(
                    new PressureEntryViewModel(app.getVitalsRepository(), app.getClock()));
        }
        if (modelClass == AgendaViewModel.class) {
            return modelClass.cast(new AgendaViewModel(app.getMedicationRepository(),
                    app.getAppointmentRepository(), app.getClock()));
        }
        if (modelClass == AlarmViewModel.class) {
            return modelClass.cast(new AlarmViewModel(app.getMedicationRepository(),
                    app.getAppointmentRepository(), app.getAlarmScheduler(),
                    app.getReminderNotifier(), app.getClock()));
        }
        if (modelClass == HistoryViewModel.class) {
            return modelClass.cast(new HistoryViewModel(app.getVitalsRepository(),
                    app.getMedicationRepository(), app.getClock()));
        }
        throw new IllegalArgumentException("Unknown ViewModel: " + modelClass.getName());
    }
}
