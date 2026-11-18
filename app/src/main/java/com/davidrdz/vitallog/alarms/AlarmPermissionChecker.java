package com.davidrdz.vitallog.alarms;

import android.Manifest;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

/**
 * Comprueba los permisos que necesitan los recordatorios y da el intent de ajustes de cada uno.
 * Solo pide lo que existe en la versión de Android del teléfono.
 */
public final class AlarmPermissionChecker {

    /** Permisos de los recordatorios, en el orden en que se piden. */
    public enum Permission {
        /** Mostrar notificaciones (API 33+). */
        NOTIFICATIONS,
        /** Alarmas a la hora exacta (API 31+). */
        EXACT_ALARMS,
        /** Mostrar el modal a pantalla completa con el teléfono bloqueado (API 34+). */
        FULL_SCREEN_INTENT
    }

    private static final String PACKAGE_SCHEME = "package";

    private AlarmPermissionChecker() {
    }

    /** Permisos que faltan, en orden; vacía si los recordatorios pueden funcionar completos. */
    @NonNull
    public static List<Permission> missing(@NonNull final Context context) {
        final List<Permission> missing = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(context,
                Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            missing.add(Permission.NOTIFICATIONS);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                && !context.getSystemService(AlarmManager.class).canScheduleExactAlarms()) {
            missing.add(Permission.EXACT_ALARMS);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE
                && !context.getSystemService(NotificationManager.class).canUseFullScreenIntent()) {
            missing.add(Permission.FULL_SCREEN_INTENT);
        }
        return missing;
    }

    /** Pantalla de ajustes del sistema donde el usuario concede {@code permission} a la app. */
    @NonNull
    public static Intent settingsIntent(@NonNull final Context context,
                                        @NonNull final Permission permission) {
        final Uri packageUri = Uri.fromParts(PACKAGE_SCHEME, context.getPackageName(), null);
        if (permission == Permission.NOTIFICATIONS) {
            return new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                    .putExtra(Settings.EXTRA_APP_PACKAGE, context.getPackageName());
        }
        if (permission == Permission.EXACT_ALARMS
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, packageUri);
        }
        if (permission == Permission.FULL_SCREEN_INTENT
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            return new Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT, packageUri);
        }
        return appDetailsIntent(context);
    }

    /** Ajustes generales de la app: respaldo si el teléfono no tiene la pantalla específica. */
    @NonNull
    public static Intent appDetailsIntent(@NonNull final Context context) {
        return new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts(PACKAGE_SCHEME, context.getPackageName(), null));
    }
}
