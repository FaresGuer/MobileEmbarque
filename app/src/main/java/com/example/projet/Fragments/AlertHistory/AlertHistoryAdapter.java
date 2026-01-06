package com.example.projet.Fragments.AlertHistory;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projet.Entities.AlertHistory;
import com.example.projet.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AlertHistoryAdapter extends RecyclerView.Adapter<AlertHistoryAdapter.VH> {

    private List<AlertHistory> items;

    public AlertHistoryAdapter(List<AlertHistory> items) {
        this.items = items;
    }

    public void setItems(List<AlertHistory> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_alert_history, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        AlertHistory alert = items.get(position);
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String timestamp = sdf.format(new Date(alert.getTimestamp()));

        // Set title
        holder.title.setText(alert.getTitle());
        
        // Set message
        holder.message.setText(alert.getMessage());
        
        // Set timestamp
        holder.timestamp.setText(timestamp);
        
        // Set alert type badge
        holder.alertType.setText(alert.getAlertType());
        
        // Set severity color
        int color = getSeverityColor(alert.getSeverity());
        holder.alertType.setTextColor(color);
        holder.severityIndicator.setBackgroundColor(color);
        
        // Set location if available
        if (alert.getLocation() != null && !alert.getLocation().isEmpty()) {
            holder.location.setText("📍 " + alert.getLocation());
            holder.location.setVisibility(View.VISIBLE);
        } else {
            holder.location.setVisibility(View.GONE);
        }
    }

    private int getSeverityColor(String severity) {
        if (severity == null) return Color.GRAY;
        switch (severity.toUpperCase()) {
            case "CRITICAL":
                return Color.parseColor("#D32F2F"); // Red
            case "HIGH":
                return Color.parseColor("#F57C00"); // Orange
            case "MEDIUM":
                return Color.parseColor("#FBC02D"); // Yellow
            case "LOW":
                return Color.parseColor("#388E3C"); // Green
            case "INFO":
            default:
                return Color.parseColor("#1976D2"); // Blue
        }
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView title, message, timestamp, alertType, location;
        View severityIndicator;

        VH(@NonNull View v) {
            super(v);
            title = v.findViewById(R.id.tvAlertTitle);
            message = v.findViewById(R.id.tvAlertMessage);
            timestamp = v.findViewById(R.id.tvAlertTimestamp);
            alertType = v.findViewById(R.id.tvAlertType);
            location = v.findViewById(R.id.tvAlertLocation);
            severityIndicator = v.findViewById(R.id.viewSeverityIndicator);
        }
    }
}

