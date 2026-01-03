package com.example.projet.Fragments.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projet.Entities.SafetyModule.FallEvent;
import com.example.projet.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SafetyLogsAdapter extends RecyclerView.Adapter<SafetyLogsAdapter.VH> {

    private List<FallEvent> items;

    public SafetyLogsAdapter(List<FallEvent> items) {
        this.items = items;
    }

    public void setItems(List<FallEvent> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_fall_log, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        FallEvent e = items.get(position);

        String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(new Date(e.timestampMs));

        h.tvType.setText(e.type);
        h.tvTime.setText(time);
        h.tvG.setText(String.format(Locale.getDefault(), "Peak G: %.2f", e.peakG));

        String loc;
        if (e.lat != null && e.lon != null) loc = "Loc: " + e.lat + ", " + e.lon;
        else loc = "Loc: N/A";
        h.tvLoc.setText(loc);

        String flags = "SMS: " + (e.smsSent ? "Yes" : "No") + "  Cancelled: " + (e.userCancelled ? "Yes" : "No");
        h.tvFlags.setText(flags);
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvType, tvTime, tvG, tvLoc, tvFlags;
        VH(@NonNull View itemView) {
            super(itemView);
            tvType = itemView.findViewById(R.id.tvLogType);
            tvTime = itemView.findViewById(R.id.tvLogTime);
            tvG = itemView.findViewById(R.id.tvLogG);
            tvLoc = itemView.findViewById(R.id.tvLogLoc);
            tvFlags = itemView.findViewById(R.id.tvLogFlags);
        }
    }
}