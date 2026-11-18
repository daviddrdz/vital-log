package com.davidrdz.vitallog.feedback;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
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
 *
 * <p>Entrada animada solo con escala (el banner crece un poco y el icono aparece con un rebote);
 * nunca con transparencia, así el texto siempre se ve con su contraste final. Si el sistema tiene
 * las animaciones desactivadas, aparece directamente. TalkBack lee el mismo mensaje al mostrarse.
 */
public class FeedbackBannerView extends LinearLayout {

    private static final float FULL_SCALE = 1f;
    private static final float HIDDEN_SCALE = 0f;

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
        messageText.setText(message);
        setVisibility(VISIBLE);
        if (ValueAnimator.areAnimatorsEnabled()) {
            animateEntrance();
        } else {
            resetScale();
        }
        announce(message);
        postDelayed(hide, FeedbackDurations.BANNER_VISIBLE_MS);
    }

    /** El banner crece hasta su tamaño normal y después el icono aparece con un rebote. */
    private void animateEntrance() {
        animate().cancel();
        iconView.animate().cancel();
        setScaleX(FeedbackDurations.BANNER_START_SCALE);
        setScaleY(FeedbackDurations.BANNER_START_SCALE);
        iconView.setScaleX(HIDDEN_SCALE);
        iconView.setScaleY(HIDDEN_SCALE);
        animate().scaleX(FULL_SCALE).scaleY(FULL_SCALE)
                .setDuration(FeedbackDurations.BANNER_ENTER_MS)
                .setInterpolator(new DecelerateInterpolator())
                .start();
        iconView.animate().scaleX(FULL_SCALE).scaleY(FULL_SCALE)
                .setStartDelay(FeedbackDurations.ICON_POP_DELAY_MS)
                .setDuration(FeedbackDurations.ICON_POP_MS)
                .setInterpolator(new OvershootInterpolator())
                .start();
    }

    /** Sin animaciones del sistema: banner e icono aparecen ya en su tamaño final. */
    private void resetScale() {
        animate().cancel();
        iconView.animate().cancel();
        setScaleX(FULL_SCALE);
        setScaleY(FULL_SCALE);
        iconView.setScaleX(FULL_SCALE);
        iconView.setScaleY(FULL_SCALE);
    }

    /**
     * TalkBack lee el mensaje aunque sea idéntico al anterior (una región "live" no lo repetiría).
     * {@code announceForAccessibility} está en desuso desde API 36, pero sigue funcionando y es la
     * única forma de repetir el mismo aviso; el plan del Hito 6 la pide expresamente.
     */
    @SuppressWarnings("deprecation")
    private void announce(@NonNull final CharSequence message) {
        announceForAccessibility(message);
    }

    @Override
    protected void onDetachedFromWindow() {
        removeCallbacks(hide);
        animate().cancel();
        iconView.animate().cancel();
        super.onDetachedFromWindow();
    }
}
