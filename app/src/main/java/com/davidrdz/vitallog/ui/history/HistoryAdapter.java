package com.davidrdz.vitallog.ui.history;

import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.davidrdz.vitallog.R;
import com.davidrdz.vitallog.analytics.ReadingCategory;
import com.davidrdz.vitallog.analytics.VitalUnit;
import com.davidrdz.vitallog.data.entity.DoseUnit;
import com.davidrdz.vitallog.ui.agenda.DoseUnitLabels;
import com.davidrdz.vitallog.ui.common.ReadingStatus;
import com.davidrdz.vitallog.ui.common.SpokenTimeFormatter;
import com.davidrdz.vitallog.ui.widget.ReadingCategoryStyle;
import com.davidrdz.vitallog.ui.widget.VitalUnitLabels;

import java.util.Objects;

/**
 * Filas de "Mi historial": encabezado por día y tarjetas de solo lectura (sin toques ni gestos)
 * con tipo en palabras, valor + unidad, hora y categoría con icono + texto + color.
 */
final class HistoryAdapter extends ListAdapter<HistoryItem, RecyclerView.ViewHolder> {

    private static final int TYPE_DAY = 0;
    private static final int TYPE_ENTRY = 1;

    @NonNull
    private final SpokenTimeFormatter formatter;
    private final long now;

    /** @param now momento actual en epoch millis, para escribir "Hoy" o "Ayer". */
    HistoryAdapter(@NonNull final SpokenTimeFormatter formatter, final long now) {
        super(DIFF);
        this.formatter = formatter;
        this.now = now;
    }

    @Override
    public int getItemViewType(final int position) {
        return getItem(position).getKind() == HistoryItem.Kind.DAY_HEADER ? TYPE_DAY : TYPE_ENTRY;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent,
                                                      final int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_DAY) {
            return new DayHolder(inflater.inflate(R.layout.item_history_day, parent, false));
        }
        return new EntryHolder(inflater.inflate(R.layout.item_history_entry, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder,
                                 final int position) {
        final HistoryItem item = getItem(position);
        if (holder instanceof DayHolder) {
            ((DayHolder) holder).day.setText(formatter.formatDayHeading(item.getTakenAt(), now));
        } else {
            bindEntry((EntryHolder) holder, item);
        }
    }

    private void bindEntry(@NonNull final EntryHolder holder, @NonNull final HistoryItem item) {
        final Resources res = holder.itemView.getResources();
        holder.time.setText(res.getString(R.string.history_time_line,
                formatter.formatTime(item.getTakenAt())));
        switch (item.getKind()) {
            case GLUCOSE:
                bindType(holder, R.drawable.ic_glucose_drop, R.color.action_glucose_fg,
                        R.string.history_type_glucose);
                holder.value.setText(res.getString(R.string.history_value_glucose,
                        item.getValue(), res.getString(VitalUnitLabels.shortLabel(
                                VitalUnit.MG_PER_DL))));
                holder.value.setContentDescription(res.getString(R.string.history_value_glucose,
                        item.getValue(), res.getString(VitalUnitLabels.spokenLabel(
                                VitalUnit.MG_PER_DL))));
                bindCategory(holder, ReadingStatus.Measure.GLUCOSE,
                        Objects.requireNonNull(item.getCategory()));
                break;
            case BLOOD_PRESSURE:
                bindType(holder, R.drawable.ic_pressure_heart, R.color.action_pressure_fg,
                        R.string.history_type_pressure);
                holder.value.setText(res.getString(R.string.history_value_pressure,
                        item.getValue(), item.getSecondValue(),
                        res.getString(VitalUnitLabels.shortLabel(VitalUnit.MMHG))));
                holder.value.setContentDescription(res.getString(
                        R.string.history_value_pressure_spoken, item.getValue(),
                        item.getSecondValue(),
                        res.getString(VitalUnitLabels.spokenLabel(VitalUnit.MMHG))));
                bindCategory(holder, ReadingStatus.Measure.BLOOD_PRESSURE,
                        Objects.requireNonNull(item.getCategory()));
                break;
            case DOSE:
                bindType(holder, R.drawable.ic_medication, R.color.action_agenda_fg,
                        R.string.history_type_dose);
                final DoseUnit unit = Objects.requireNonNull(item.getDoseUnit());
                holder.value.setText(res.getString(R.string.history_value_dose,
                        item.getMedicationName(), item.getValue(),
                        DoseUnitLabels.forAmount(res, unit, item.getValue())));
                holder.value.setContentDescription(null);
                holder.categoryRow.setVisibility(View.GONE);
                break;
            default:
                throw new IllegalArgumentException("Not an entry: " + item.getKind());
        }
    }

    private static void bindType(@NonNull final EntryHolder holder, @DrawableRes final int icon,
                                 @ColorRes final int color, @StringRes final int label) {
        holder.typeIcon.setImageResource(icon);
        ImageViewCompat.setImageTintList(holder.typeIcon, ColorStateList.valueOf(
                ContextCompat.getColor(holder.itemView.getContext(), color)));
        holder.type.setText(label);
    }

    private static void bindCategory(@NonNull final EntryHolder holder,
                                     @NonNull final ReadingStatus.Measure measure,
                                     @NonNull final ReadingCategory category) {
        final ColorStateList tint = ColorStateList.valueOf(ContextCompat.getColor(
                holder.itemView.getContext(), ReadingCategoryStyle.color(category)));
        holder.categoryRow.setVisibility(View.VISIBLE);
        holder.categoryIcon.setImageResource(ReadingCategoryStyle.icon(category));
        ImageViewCompat.setImageTintList(holder.categoryIcon, tint);
        holder.category.setText(ReadingCategoryStyle.label(measure, category));
        holder.category.setTextColor(tint);
    }

    static final class DayHolder extends RecyclerView.ViewHolder {
        final TextView day;

        DayHolder(@NonNull final View itemView) {
            super(itemView);
            day = itemView.findViewById(R.id.txt_history_day);
        }
    }

    static final class EntryHolder extends RecyclerView.ViewHolder {
        final ImageView typeIcon;
        final TextView type;
        final TextView value;
        final TextView time;
        final View categoryRow;
        final ImageView categoryIcon;
        final TextView category;

        EntryHolder(@NonNull final View itemView) {
            super(itemView);
            typeIcon = itemView.findViewById(R.id.img_history_type);
            type = itemView.findViewById(R.id.txt_history_type);
            value = itemView.findViewById(R.id.txt_history_value);
            time = itemView.findViewById(R.id.txt_history_time);
            categoryRow = itemView.findViewById(R.id.row_history_category);
            categoryIcon = itemView.findViewById(R.id.img_history_category);
            category = itemView.findViewById(R.id.txt_history_category);
        }
    }

    private static final DiffUtil.ItemCallback<HistoryItem> DIFF =
            new DiffUtil.ItemCallback<HistoryItem>() {
                @Override
                public boolean areItemsTheSame(@NonNull final HistoryItem a,
                                               @NonNull final HistoryItem b) {
                    return a.getKind() == b.getKind() && a.getId() == b.getId();
                }

                @Override
                public boolean areContentsTheSame(@NonNull final HistoryItem a,
                                                  @NonNull final HistoryItem b) {
                    return a.equals(b);
                }
            };
}
