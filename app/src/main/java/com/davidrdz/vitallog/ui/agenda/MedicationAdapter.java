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
import com.davidrdz.vitallog.data.entity.Medication;
import com.davidrdz.vitallog.ui.common.SpokenTimeFormatter;

/** Filas de "Mis medicamentos": nombre, dosis, hora y botón visible "Borrar". */
final class MedicationAdapter extends ListAdapter<Medication, MedicationAdapter.Holder> {

    /** Recibe el toque en "Borrar" (luego se pide confirmación). */
    interface OnDeleteListener {
        void onDelete(@NonNull Medication medication);
    }

    private final SpokenTimeFormatter formatter;
    private final OnDeleteListener onDelete;

    MedicationAdapter(@NonNull final SpokenTimeFormatter formatter,
                      @NonNull final OnDeleteListener onDelete) {
        super(DIFF);
        this.formatter = formatter;
        this.onDelete = onDelete;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_medication, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final Holder holder, final int position) {
        final Medication medication = getItem(position);
        final Resources res = holder.itemView.getResources();
        holder.name.setText(medication.getName());
        holder.dose.setText(res.getString(R.string.medication_dose_line,
                medication.getDoseAmount(), DoseUnitLabels.forAmount(res,
                        medication.getDoseUnit(), medication.getDoseAmount())));
        holder.time.setText(res.getString(R.string.medication_time_line,
                formatter.formatTime(medication.getHour(), medication.getMinute())));
        holder.delete.setContentDescription(
                res.getString(R.string.delete_medication_description, medication.getName()));
        holder.delete.setOnClickListener(view -> onDelete.onDelete(medication));
    }

    static final class Holder extends RecyclerView.ViewHolder {
        final TextView name;
        final TextView dose;
        final TextView time;
        final View delete;

        Holder(@NonNull final View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.txt_medication_name);
            dose = itemView.findViewById(R.id.txt_medication_dose);
            time = itemView.findViewById(R.id.txt_medication_time);
            delete = itemView.findViewById(R.id.btn_delete_medication);
        }
    }

    private static final DiffUtil.ItemCallback<Medication> DIFF =
            new DiffUtil.ItemCallback<Medication>() {
                @Override
                public boolean areItemsTheSame(@NonNull final Medication a,
                                               @NonNull final Medication b) {
                    return a.getId() == b.getId();
                }

                @Override
                public boolean areContentsTheSame(@NonNull final Medication a,
                                                  @NonNull final Medication b) {
                    return a.getName().equals(b.getName())
                            && a.getDoseAmount() == b.getDoseAmount()
                            && a.getDoseUnit().equals(b.getDoseUnit())
                            && a.getHour() == b.getHour()
                            && a.getMinute() == b.getMinute()
                            && a.isActive() == b.isActive();
                }
            };
}
