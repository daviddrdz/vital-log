package com.davidrdz.vitallog.ui.agenda;

import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.davidrdz.vitallog.R;
import com.davidrdz.vitallog.data.entity.Appointment;
import com.davidrdz.vitallog.ui.common.SpokenTimeFormatter;

import java.util.function.LongSupplier;

/** Filas de "Mis citas con el médico": médico o lugar, día, hora y botón visible "Borrar". */
final class AppointmentAdapter extends ListAdapter<Appointment, AppointmentAdapter.Holder> {

    /** Recibe el toque en "Borrar" (luego se pide confirmación). */
    interface OnDeleteListener {
        void onDelete(@NonNull Appointment appointment);
    }

    private final SpokenTimeFormatter formatter;
    private final LongSupplier now;
    private final OnDeleteListener onDelete;

    /** @param now momento actual en epoch millis, para escribir "hoy" o "mañana". */
    AppointmentAdapter(@NonNull final SpokenTimeFormatter formatter,
                       @NonNull final LongSupplier now,
                       @NonNull final OnDeleteListener onDelete) {
        super(DIFF);
        this.formatter = formatter;
        this.now = now;
        this.onDelete = onDelete;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_appointment, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final Holder holder, final int position) {
        final Appointment appointment = getItem(position);
        final Resources res = holder.itemView.getResources();
        holder.place.setText(appointment.getDoctorOrPlace());
        holder.day.setText(res.getString(R.string.appointment_day_line,
                formatter.formatDayWithDate(appointment.getScheduledAt(), now.getAsLong())));
        holder.time.setText(res.getString(R.string.appointment_time_line,
                formatter.formatTime(appointment.getScheduledAt())));
        holder.delete.setContentDescription(res.getString(
                R.string.delete_appointment_description, appointment.getDoctorOrPlace()));
        holder.delete.setOnClickListener(view -> onDelete.onDelete(appointment));
    }

    static final class Holder extends RecyclerView.ViewHolder {
        final TextView place;
        final TextView day;
        final TextView time;
        final View delete;

        Holder(@NonNull final View itemView) {
            super(itemView);
            place = itemView.findViewById(R.id.txt_appointment_name);
            day = itemView.findViewById(R.id.txt_appointment_day);
            time = itemView.findViewById(R.id.txt_appointment_time);
            delete = itemView.findViewById(R.id.btn_delete_appointment);
        }
    }

    private static final DiffUtil.ItemCallback<Appointment> DIFF =
            new DiffUtil.ItemCallback<Appointment>() {
                @Override
                public boolean areItemsTheSame(@NonNull final Appointment a,
                                               @NonNull final Appointment b) {
                    return a.getId() == b.getId();
                }

                @Override
                public boolean areContentsTheSame(@NonNull final Appointment a,
                                                  @NonNull final Appointment b) {
                    return a.getDoctorOrPlace().equals(b.getDoctorOrPlace())
                            && a.getScheduledAt() == b.getScheduledAt()
                            && a.isActive() == b.isActive();
                }
            };
}
