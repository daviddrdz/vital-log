package com.davidrdz.vitallog.feedback;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.davidrdz.vitallog.R;

/**
 * Banner grande de confirmación: check + mensaje que dice qué se guardó (CLAUDE.md §3.4).
 * Fondo claro sólido, borde y check oscuros; visible {@link FeedbackDurations#BANNER_VISIBLE_MS}.
 * El mensaje es una región "live" para que TalkBack lo lea al aparecer.
 */
public class FeedbackBannerView extends LinearLayout {

    private final TextView messageText;
    private final Runnable hide = () -> setVisibility(GONE);

    public FeedbackBannerView(@NonNull final Context context) {
        this(context, null);
    }

    public FeedbackBannerView(@NonNull final Context context, @Nullable final AttributeSet attrs) {
        super(context, attrs);
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);
        setBackgroundResource(R.drawable.bg_feedback_success);
        final int padding = getResources().getDimensionPixelSize(R.dimen.spacing_medium);
        setPadding(padding, padding, padding, padding);
        inflate(context, R.layout.view_feedback_banner, this);
        messageText = findViewById(R.id.txt_feedback_message);
        setVisibility(GONE);
    }

    /** Muestra {@code message} y lo oculta tras {@link FeedbackDurations#BANNER_VISIBLE_MS}. */
    public void showSuccess(@NonNull final CharSequence message) {
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
