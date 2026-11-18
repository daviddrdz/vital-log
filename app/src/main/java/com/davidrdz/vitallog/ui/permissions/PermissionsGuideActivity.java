package com.davidrdz.vitallog.ui.permissions;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.davidrdz.vitallog.R;
import com.davidrdz.vitallog.alarms.AlarmPermissionChecker;
import com.davidrdz.vitallog.alarms.AlarmPermissionChecker.Permission;
import com.davidrdz.vitallog.ui.common.BaseScreenActivity;

import java.util.List;

/**
 * Guía para activar los permisos de los recordatorios: un permiso por pantalla, explicado en
 * lenguaje llano, con un solo botón "Abrir ajustes". Al volver de los ajustes muestra el siguiente
 * permiso que falte, o se cierra si ya están todos.
 */
public final class PermissionsGuideActivity extends BaseScreenActivity {

    private ImageView pictogram;
    private TextView permissionTitle;
    private TextView permissionBody;
    @Nullable
    private Permission current;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pictogram = findViewById(R.id.img_permission_pictogram);
        permissionTitle = findViewById(R.id.txt_permission_title);
        permissionBody = findViewById(R.id.txt_permission_body);
        findViewById(R.id.btn_open_settings).setOnClickListener(view -> openSettings());
    }

    @Override
    protected void onResume() {
        super.onResume();
        final List<Permission> missing = AlarmPermissionChecker.missing(this);
        if (missing.isEmpty()) {
            finish();
            return;
        }
        current = missing.get(0);
        render(current);
    }

    private void render(@NonNull final Permission permission) {
        switch (permission) {
            case NOTIFICATIONS:
                show(R.drawable.ic_notifications, R.string.permission_notifications_title,
                        R.string.permission_notifications_body);
                break;
            case EXACT_ALARMS:
                show(R.drawable.ic_snooze, R.string.permission_exact_alarms_title,
                        R.string.permission_exact_alarms_body);
                break;
            case FULL_SCREEN_INTENT:
                show(R.drawable.ic_fullscreen, R.string.permission_full_screen_title,
                        R.string.permission_full_screen_body);
                break;
            default:
                throw new IllegalArgumentException("Unknown permission: " + permission);
        }
    }

    private void show(@DrawableRes final int icon, @StringRes final int title,
                      @StringRes final int body) {
        pictogram.setImageResource(icon);
        pictogram.setContentDescription(getString(title));
        permissionTitle.setText(title);
        permissionBody.setText(body);
    }

    private void openSettings() {
        if (current == null) {
            return;
        }
        final Intent specific = AlarmPermissionChecker.settingsIntent(this, current);
        try {
            startActivity(specific);
        } catch (final ActivityNotFoundException noSpecificScreen) {
            startActivity(AlarmPermissionChecker.appDetailsIntent(this));
        }
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_permissions_guide;
    }

    @Override
    protected int getScreenTitleRes() {
        return R.string.title_permissions_guide;
    }
}
