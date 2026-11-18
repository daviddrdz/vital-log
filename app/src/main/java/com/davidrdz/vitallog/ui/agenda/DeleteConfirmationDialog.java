package com.davidrdz.vitallog.ui.agenda;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

import com.davidrdz.vitallog.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/**
 * Confirmación obligatoria antes de borrar: pregunta concreta y dos botones grandes,
 * "No, conservar" (primero, la opción segura) y "Sí, borrar".
 */
final class DeleteConfirmationDialog {

    private DeleteConfirmationDialog() {
    }

    static void show(@NonNull final AppCompatActivity activity, @NonNull final String question,
                     @NonNull final String detail, @NonNull final Runnable onConfirmed) {
        final View content = activity.getLayoutInflater()
                .inflate(R.layout.dialog_confirm_delete, null, false);
        ((TextView) content.findViewById(R.id.txt_confirm_question)).setText(question);
        ((TextView) content.findViewById(R.id.txt_confirm_detail)).setText(detail);

        final AlertDialog dialog = new MaterialAlertDialogBuilder(activity)
                .setView(content)
                .create();
        content.findViewById(R.id.btn_confirm_keep).setOnClickListener(view -> dialog.dismiss());
        content.findViewById(R.id.btn_confirm_delete).setOnClickListener(view -> {
            dialog.dismiss();
            onConfirmed.run();
        });
        // Al rotar, la pantalla se recrea: se cierra el diálogo para no filtrar la ventana.
        final DefaultLifecycleObserver dismissOnDestroy = new DefaultLifecycleObserver() {
            @Override
            public void onDestroy(@NonNull final LifecycleOwner owner) {
                dialog.dismiss();
            }
        };
        activity.getLifecycle().addObserver(dismissOnDestroy);
        dialog.setOnDismissListener(
                closed -> activity.getLifecycle().removeObserver(dismissOnDestroy));
        dialog.show();
    }
}
