package com.davidrdz.vitallog.ui.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;

import com.davidrdz.vitallog.R;
import com.davidrdz.vitallog.analytics.VitalRange;
import com.davidrdz.vitallog.analytics.VitalUnit;
import com.google.android.material.button.MaterialButton;

/**
 * Control numérico visual reutilizable (CLAUDE.md §3.1): botón "Subir", número grande, unidad y
 * botón "Bajar". Sin teclado. Mantener presionado acelera el paso (1 → 5 → 10).
 *
 * <p>El rótulo se declara en XML con {@code app:stepperLabel}; límites, valor inicial y unidad se
 * configuran desde Java con {@link #bind(VitalRange)}, de modo que coinciden con
 * {@link com.davidrdz.vitallog.analytics.VitalRanges}. El valor se conserva al rotar la pantalla.
 */
public class NumericStepperView extends LinearLayout {

    private static final int DIRECTION_UP = 1;
    private static final int DIRECTION_DOWN = -1;

    private final TextView valueText;
    private final TextView unitText;
    private final MaterialButton increaseButton;
    private final MaterialButton decreaseButton;
    private final CharSequence label;
    private final AccelerationPolicy acceleration = new AccelerationPolicy();
    private final RepeatTouchListener increaseRepeater;
    private final RepeatTouchListener decreaseRepeater;

    @Nullable
    private StepperModel model;
    @Nullable
    private VitalUnit unit;
    /** Valor restaurado antes de {@link #bind(VitalRange)}; se aplica al enlazar. */
    @Nullable
    private Integer pendingRestoredValue;

    public NumericStepperView(@NonNull final Context context) {
        this(context, null);
    }

    public NumericStepperView(@NonNull final Context context, @Nullable final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NumericStepperView(@NonNull final Context context, @Nullable final AttributeSet attrs,
                              final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOrientation(VERTICAL);
        inflate(context, R.layout.view_numeric_stepper, this);

        final TypedArray a = context.obtainStyledAttributes(
                attrs, R.styleable.NumericStepperView, defStyleAttr, 0);
        final CharSequence declaredLabel = a.getText(R.styleable.NumericStepperView_stepperLabel);
        a.recycle();
        label = declaredLabel == null ? "" : declaredLabel;

        final TextView labelText = findViewById(R.id.txt_stepper_label);
        labelText.setText(label);
        valueText = findViewById(R.id.txt_stepper_value);
        unitText = findViewById(R.id.txt_stepper_unit);
        increaseButton = findViewById(R.id.btn_stepper_increase);
        decreaseButton = findViewById(R.id.btn_stepper_decrease);

        increaseRepeater = wireButton(increaseButton, DIRECTION_UP);
        decreaseRepeater = wireButton(decreaseButton, DIRECTION_DOWN);
        addValueAccessibilityActions();
    }

    /** Fija límites, unidad y valor inicial (el típico del rango) del control. */
    public void bind(@NonNull final VitalRange range) {
        final int initial = pendingRestoredValue != null
                ? pendingRestoredValue : range.getTypical();
        pendingRestoredValue = null;
        model = new StepperModel(range.getMin(), range.getMax(), initial);
        unit = range.getUnit();
        unitText.setText(unitShortLabel(unit));
        render();
    }

    private RepeatTouchListener wireButton(final MaterialButton button, final int direction) {
        // Toque corto y clic de accesibilidad: un paso de 1.
        button.setOnClickListener(v -> step(direction, AccelerationPolicy.SLOW_STEP));
        final RepeatTouchListener repeater = new RepeatTouchListener(
                new RepeatTouchListener.Listener() {
                    @Override
                    public void onRepeat() {
                        step(direction, acceleration.nextStep());
                    }

                    @Override
                    public void onRepeatEnd() {
                        acceleration.reset();
                    }
                });
        button.setOnTouchListener(repeater);
        return repeater;
    }

    private void addValueAccessibilityActions() {
        ViewCompat.addAccessibilityAction(valueText,
                getResources().getString(R.string.stepper_increase), (view, args) -> {
                    step(DIRECTION_UP, AccelerationPolicy.SLOW_STEP);
                    return true;
                });
        ViewCompat.addAccessibilityAction(valueText,
                getResources().getString(R.string.stepper_decrease), (view, args) -> {
                    step(DIRECTION_DOWN, AccelerationPolicy.SLOW_STEP);
                    return true;
                });
    }

    private void step(final int direction, final int amount) {
        if (model == null) {
            return;
        }
        final boolean changed = direction == DIRECTION_UP
                ? model.increment(amount) : model.decrement(amount);
        if (changed) {
            render();
        }
    }

    private void render() {
        if (model == null || unit == null) {
            return;
        }
        final int value = model.getValue();
        valueText.setText(getResources().getString(R.string.stepper_value, value));
        valueText.setContentDescription(getResources().getString(
                R.string.stepper_value_description, label, value,
                getResources().getString(unitSpokenLabel(unit))));

        renderButton(increaseButton, model.isAtMax(), R.string.stepper_increase,
                R.string.stepper_at_max, R.string.stepper_increase_description,
                R.string.stepper_at_max_description, R.drawable.ic_add);
        renderButton(decreaseButton, model.isAtMin(), R.string.stepper_decrease,
                R.string.stepper_at_min, R.string.stepper_decrease_description,
                R.string.stepper_at_min_description, R.drawable.ic_remove);
    }

    /**
     * En el límite el botón dice "Máximo"/"Mínimo" con fondo neutro sólido. Sigue habilitado a
     * propósito: el estado deshabilitado de Material reduce la opacidad (prohibido por §3.3).
     */
    private void renderButton(final MaterialButton button, final boolean atLimit,
                              @StringRes final int text, @StringRes final int limitText,
                              @StringRes final int description,
                              @StringRes final int limitDescription,
                              @DrawableRes final int icon) {
        button.setText(atLimit ? limitText : text);
        button.setContentDescription(
                getResources().getString(atLimit ? limitDescription : description, label));
        button.setIconResource(atLimit ? 0 : icon);
        button.setBackgroundTintList(colors(atLimit
                ? R.color.surface_neutral : R.color.brand_primary_container));
        button.setStrokeColor(colors(atLimit ? R.color.outline_strong : R.color.brand_primary));
    }

    private ColorStateList colors(@ColorRes final int color) {
        return ColorStateList.valueOf(ContextCompat.getColor(getContext(), color));
    }

    @StringRes
    private static int unitShortLabel(@NonNull final VitalUnit unit) {
        switch (unit) {
            case MG_PER_DL:
                return R.string.unit_mg_dl;
            case MMHG:
                return R.string.unit_mmhg;
            default:
                throw new IllegalArgumentException("Unknown unit: " + unit);
        }
    }

    @StringRes
    private static int unitSpokenLabel(@NonNull final VitalUnit unit) {
        switch (unit) {
            case MG_PER_DL:
                return R.string.unit_mg_dl_spoken;
            case MMHG:
                return R.string.unit_mmhg_spoken;
            default:
                throw new IllegalArgumentException("Unknown unit: " + unit);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        increaseRepeater.stop();
        decreaseRepeater.stop();
        super.onDetachedFromWindow();
    }

    // ===== Conservar el valor al rotar =====
    // Los hijos comparten IDs entre instancias (pantalla de presión con dos controles), así que el
    // control guarda solo su propio estado y no el de sus hijos.

    @Override
    protected void dispatchSaveInstanceState(final SparseArray<Parcelable> container) {
        dispatchFreezeSelfOnly(container);
    }

    @Override
    protected void dispatchRestoreInstanceState(final SparseArray<Parcelable> container) {
        dispatchThawSelfOnly(container);
    }

    @Nullable
    @Override
    protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();
        if (model == null) {
            return superState;
        }
        return new SavedState(superState, model.getValue());
    }

    @Override
    protected void onRestoreInstanceState(final Parcelable state) {
        if (!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }
        final SavedState saved = (SavedState) state;
        super.onRestoreInstanceState(saved.getSuperState());
        if (model == null) {
            pendingRestoredValue = saved.value;
        } else if (model.setValue(saved.value)) {
            render();
        }
    }

    static final class SavedState extends BaseSavedState {

        final int value;

        SavedState(final Parcelable superState, final int value) {
            super(superState);
            this.value = value;
        }

        private SavedState(final Parcel in) {
            super(in);
            value = in.readInt();
        }

        @Override
        public void writeToParcel(final Parcel out, final int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(value);
        }

        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            @Override
            public SavedState createFromParcel(final Parcel in) {
                return new SavedState(in);
            }

            @Override
            public SavedState[] newArray(final int size) {
                return new SavedState[size];
            }
        };
    }
}
