package com.davidrdz.vitallog.feedback;

import android.content.Context;
import android.util.AttributeSet;
import android.content.res.ColorStateList;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;

import com.davidrdz.vitallog.R;

/**
 * Banner grande de retroalimentación (CLAUDE.md §3.4): check + mensaje de lo que se guardó, o
 * triángulo de aviso + motivo por el que no se guardó. Fondo claro sólido (verde o rojo), borde e
 * icono oscuros; visible {@link FeedbackDurations#BANNER_VISIBLE_MS}.
 * El mensaje es una región "live" para que TalkBack lo lea al aparecer.
 */
public class FeedbackBannerView extends LinearLayout {

    private final ImageView iconView;
    private final TextView messageText;
    private final Runnable hide = () -> setVisibility(GONE);

    public FeedbackBannerView(@NonNull final Context context) {
        this(context, null);
    }

    public FeedbackBannerView(@NonNull final Context context, @Nullable final AttributeSet attrs) {
        super(context, attrs);
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);
        final int padding = getResources().getDimensionPixelSize(R.dimen.spacing_medium);
        setPadding(padding, padding, padding, padding);
        inflate(context, R.layout.view_feedback_banner, this);
        iconView = findViewById(R.id.img_feedback_icon);
        messageText = findViewById(R.id.txt_feedback_message);
        applyStyle(R.drawable.bg_feedback_success, R.drawable.ic_check_circle, R.color.status_ok);
        setVisibility(GONE);
    }

    /** Muestra {@code message} con check verde y lo oculta tras el tiempo de visibilidad. */
    public void showSuccess(@NonNull final CharSequence message) {
        applyStyle(R.drawable.bg_feedback_success, R.drawable.ic_check_circle, R.color.status_ok);
        show(message);
    }

    /** Muestra {@code message} con aviso rojo y lo oculta tras el tiempo de visibilidad. */
    public void showError(@NonNull final CharSequence message) {
        applyStyle(R.drawable.bg_feedback_error, R.drawable.ic_warning, R.color.status_danger);
        show(message);
    }

    private void applyStyle(@DrawableRes final int background, @DrawableRes final int icon,
                            @ColorRes final int iconColor) {
        setBackgroundResource(background);
        iconView.setImageResource(icon);
        ImageViewCompat.setImageTintList(iconView,
                ColorStateList.valueOf(ContextCompat.getColor(getContext(), iconColor)));
    }

    private void show(@NonNull final CharSequence message) {
        removeCallbacks(hide);
        setVisibility(VISIBLE);
        messageText.setText(message);
        postDelayed(hide, FeedbackDurations.BANNER_VISIBLE_MS);
    }

    @Override
    protected void onDetachedFromWindow() {
        removeCallbacks(hide);
        super.onDetachedFromWindow();
    }
}
