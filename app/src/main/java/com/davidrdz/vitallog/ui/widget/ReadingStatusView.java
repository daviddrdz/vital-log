package com.davidrdz.vitallog.ui.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;

import com.davidrdz.vitallog.R;
import com.davidrdz.vitallog.analytics.ReadingCategory;
import com.davidrdz.vitallog.ui.common.ReadingStatus;
import com.davidrdz.vitallog.ui.common.ValidationErrorMessages;

/**
 * Recuadro bajo los controles con la categoría informativa de la lectura (CLAUDE.md §4.2):
 * icono + texto + color, nunca solo color. En "muy alta" agrega un aviso en lenguaje llano sin
 * bloquear el guardado; si los números no cuadran, dice qué revisar. TalkBack lee los cambios.
 */
public class ReadingStatusView extends LinearLayout {

    private final ImageView iconView;
    private final TextView labelText;
    private final TextView detailText;

    public ReadingStatusView(@NonNull final Context context) {
        this(context, null);
    }

    public ReadingStatusView(@NonNull final Context context, @Nullable final AttributeSet attrs) {
        super(context, attrs);
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);
        setBackgroundResource(R.drawable.bg_outlined);
        final int padding = getResources().getDimensionPixelSize(R.dimen.spacing_medium);
        setPadding(padding, padding, padding, padding);
        setAccessibilityLiveRegion(ACCESSIBILITY_LIVE_REGION_POLITE);
        inflate(context, R.layout.view_reading_status, this);
        iconView = findViewById(R.id.img_status_icon);
        labelText = findViewById(R.id.txt_status_label);
        detailText = findViewById(R.id.txt_status_detail);
    }

    public void render(@NonNull final ReadingStatus status) {
        final ReadingCategory category = status.getCategory();
        if (status.getProblem() != null) {
            show(R.drawable.ic_warning, R.color.status_danger, R.string.reading_problem_title,
                    ValidationErrorMessages.message(getResources(), status.getProblem()));
        } else if (category != null) {
            show(ReadingCategoryStyle.icon(category), ReadingCategoryStyle.color(category),
                    ReadingCategoryStyle.label(status.getMeasure(), category),
                    category == ReadingCategory.CRITICAL
                            ? getResources().getString(R.string.reading_critical_warning)
                            : null);
        }
    }

    private void show(@DrawableRes final int icon, @ColorRes final int color,
                      @StringRes final int label, @Nullable final String detail) {
        final ColorStateList tint =
                ColorStateList.valueOf(ContextCompat.getColor(getContext(), color));
        iconView.setImageResource(icon);
        ImageViewCompat.setImageTintList(iconView, tint);
        labelText.setText(label);
        labelText.setTextColor(tint);
        detailText.setText(detail);
        detailText.setVisibility(detail == null ? GONE : VISIBLE);
    }
}
