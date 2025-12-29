package com.example.projet.Fragments.Environment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projet.Entities.EnvironmentAlert;
import com.example.projet.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AlertsAdapter extends RecyclerView.Adapter<AlertsAdapter.VH> {

    private List<EnvironmentAlert> items;

    public AlertsAdapter(List<EnvironmentAlert> items) {
        this.items = items;
    }

    public void setItems(List<EnvironmentAlert> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_environment_alert, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        EnvironmentAlert a = items.get(position);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String ts = sdf.format(new Date(a.getTimestamp()));
        holder.title.setText("[" + a.getSensor() + "] " + a.getMessage());
        holder.subtitle.setText(ts + " — " + a.getValue());
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView title, subtitle;
        VH(@NonNull View v) {
            super(v);
            title = v.findViewById(R.id.tvAlertTitle);
            subtitle = v.findViewById(R.id.tvAlertSubtitle);
        }
    }
}

