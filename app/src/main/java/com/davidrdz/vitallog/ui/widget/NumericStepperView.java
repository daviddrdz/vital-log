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
 * {@link com.davidrdz.vitallog.analytics.VitalRanges}. Para datos que no son signos vitales
 * (dosis, hora, días) se usa {@link #bind(int, int, int, int, ValueFormatter)}. El valor se
 * conserva al rotar la pantalla.
 */
public class NumericStepperView extends LinearLayout {

    /** Texto visible y hablado del número y su unidad. */
    public interface ValueFormatter {
        /** Número grande, p. ej. "120" o "8". */
        @NonNull
        CharSequence formatValue(int value);

        /** Unidad bajo el número, p. ej. "mg/dL" o "de la mañana". */
        @NonNull
        CharSequence formatUnit(int value);

        /** Unidad como la lee TalkBack, p. ej. "miligramos por decilitro". */
        @NonNull
        CharSequence formatSpokenUnit(int value);
    }

    /** Avisa cada vez que cambia el número (toque, pulsación continua o restauración). */
    public interface OnValueChangeListener {
        void onValueChanged(int value);
    }

    private static final int DIRECTION_UP = 1;
    private static final int DIRECTION_DOWN = -1;
    private static final int DEFAULT_STEP_SIZE = 1;

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
    private ValueFormatter formatter;
    @Nullable
    private OnValueChangeListener valueChangeListener;
    /** Cuánto cambia el número en cada paso de {@link AccelerationPolicy} (p. ej. 5 minutos). */
    private int stepSize = DEFAULT_STEP_SIZE;
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

    /** Fija límites, unidad y valor inicial (el típico del rango) de un signo vital. */
    public void bind(@NonNull final VitalRange range) {
        bind(range.getMin(), range.getMax(), range.getTypical(), DEFAULT_STEP_SIZE,
                new VitalUnitFormatter(range.getUnit()));
    }

    /**
     * Configura el control para un dato que no es un signo vital.
     *
     * @param stepSize cuánto cambia el número por paso (p. ej. 5 para minutos de 5 en 5).
     */
    public void bind(final int min, final int max, final int initial, final int stepSize,
                     @NonNull final ValueFormatter valueFormatter) {
        if (stepSize <= 0) {
            throw new IllegalArgumentException("stepSize must be positive");
        }
        final int start = pendingRestoredValue != null ? pendingRestoredValue : initial;
        pendingRestoredValue = null;
        model = new StepperModel(min, max, start);
        this.stepSize = stepSize;
        formatter = valueFormatter;
        render();
    }

    /** Valor actual; llamar después de {@code bind}. */
    public int getValue() {
        return requireModel().getValue();
    }

    /** Fija el valor (ajustado al rango), p. ej. la última lectura guardada. */
    public void setValue(final int value) {
        if (requireModel().setValue(value)) {
            onValueChanged();
        }
    }

    public void setOnValueChangeListener(@Nullable final OnValueChangeListener listener) {
        valueChangeListener = listener;
    }

    /** Vuelve a pintar número y unidad, p. ej. si el formateador depende de otro control. */
    public void refresh() {
        render();
    }

    @NonNull
    private StepperModel requireModel() {
        if (model == null) {
            throw new IllegalStateException("bind() must be called first");
        }
        return model;
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
        final int delta = amount * stepSize;
        final boolean changed = direction == DIRECTION_UP
                ? model.increment(delta) : model.decrement(delta);
        if (changed) {
            onValueChanged();
        }
    }

    private void onValueChanged() {
        render();
        if (valueChangeListener != null && model != null) {
            valueChangeListener.onValueChanged(model.getValue());
        }
    }

    private void render() {
        if (model == null || formatter == null) {
            return;
        }
        final int value = model.getValue();
        valueText.setText(formatter.formatValue(value));
        unitText.setText(formatter.formatUnit(value));
        valueText.setContentDescription(getResources().getString(
                R.string.stepper_value_description, label, formatter.formatValue(value),
                formatter.formatSpokenUnit(value)));

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

    /** Formateador de los signos vitales: número entero y unidad fija (mg/dL, mmHg). */
    private final class VitalUnitFormatter implements ValueFormatter {

        @NonNull
        private final VitalUnit unit;

        VitalUnitFormatter(@NonNull final VitalUnit unit) {
            this.unit = unit;
        }

        @NonNull
        @Override
        public CharSequence formatValue(final int value) {
            return getResources().getString(R.string.stepper_value, value);
        }

        @NonNull
        @Override
        public CharSequence formatUnit(final int value) {
            return getResources().getString(VitalUnitLabels.shortLabel(unit));
        }

        @NonNull
        @Override
        public CharSequence formatSpokenUnit(final int value) {
            return getResources().getString(VitalUnitLabels.spokenLabel(unit));
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
            onValueChanged();
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
