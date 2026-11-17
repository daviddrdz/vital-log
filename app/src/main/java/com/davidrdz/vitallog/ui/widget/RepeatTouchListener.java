package com.davidrdz.vitallog.ui.widget;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;

import androidx.annotation.NonNull;

/**
 * Pulsación continua de un botón: si el dedo sigue apoyado tras {@link #INITIAL_DELAY_MS}, llama a
 * {@link Listener#onRepeat()} cada {@link #REPEAT_INTERVAL_MS} hasta soltar.
 *
 * <p>Un toque corto no lo gestiona este listener: llega al botón como clic normal (así también
 * funciona con TalkBack y con teclado). Si hubo repeticiones, el "soltar" se convierte en
 * cancelación para no sumar un paso extra. Mientras repite, el {@code ScrollView} padre no puede
 * robar el gesto aunque el dedo tiemble; antes de eso, arrastrar desplaza la pantalla con normalidad.
 */
public final class RepeatTouchListener implements View.OnTouchListener {

    static final long INITIAL_DELAY_MS = 400;
    static final long REPEAT_INTERVAL_MS = 100;

    /** Recibe cada repetición y el final de la pulsación. */
    public interface Listener {
        void onRepeat();

        /** El dedo se levantó o el gesto se canceló; se reinicia la aceleración. */
        void onRepeatEnd();
    }

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Listener listener;
    private View activeView;
    private boolean repeating;
    /** Hubo al menos una repetición en el gesto actual (se limpia con cada ACTION_DOWN). */
    private boolean repeatedInGesture;

    private final Runnable repeatTick = new Runnable() {
        @Override
        public void run() {
            if (!repeating) {
                repeating = true;
                repeatedInGesture = true;
                disallowParentIntercept(activeView, true);
            }
            listener.onRepeat();
            handler.postDelayed(this, REPEAT_INTERVAL_MS);
        }
    };

    public RepeatTouchListener(@NonNull final Listener listener) {
        this.listener = listener;
    }

    // El clic lo resuelve el propio botón (performClick en su onTouchEvent): devolvemos false salvo
    // al soltar tras repetir, donde se reenvía una cancelación al botón.
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(final View view, final MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                stop();
                repeatedInGesture = false;
                activeView = view;
                handler.postDelayed(repeatTick, INITIAL_DELAY_MS);
                return false;
            case MotionEvent.ACTION_MOVE:
                if (!isInside(view, event)) {
                    stop();
                }
                return false;
            case MotionEvent.ACTION_UP:
                stop();
                if (repeatedInGesture) {
                    cancelPendingClick(view, event);
                    return true;
                }
                return false;
            case MotionEvent.ACTION_CANCEL:
                stop();
                return false;
            default:
                return false;
        }
    }

    /** Detiene la repetición; se llama también cuando la vista se separa de la ventana. */
    public void stop() {
        handler.removeCallbacks(repeatTick);
        if (activeView != null) {
            disallowParentIntercept(activeView, false);
            activeView = null;
            listener.onRepeatEnd();
        }
        repeating = false;
    }

    private static boolean isInside(final View view, final MotionEvent event) {
        final float x = event.getX();
        final float y = event.getY();
        return x >= 0 && y >= 0 && x <= view.getWidth() && y <= view.getHeight();
    }

    private static void cancelPendingClick(final View view, final MotionEvent upEvent) {
        final MotionEvent cancel = MotionEvent.obtain(upEvent);
        cancel.setAction(MotionEvent.ACTION_CANCEL);
        view.onTouchEvent(cancel);
        cancel.recycle();
    }

    private static void disallowParentIntercept(final View view, final boolean disallow) {
        final ViewParent parent = view == null ? null : view.getParent();
        if (parent != null) {
            parent.requestDisallowInterceptTouchEvent(disallow);
        }
    }
}
