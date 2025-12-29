package com.example.projet.Fragments.Adapters;

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projet.Entities.HealthModule.HeartRateLog;
import com.example.projet.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
public class HeartRateLogAdapter extends RecyclerView.Adapter<HeartRateLogAdapter.VH>{
    private final List<HeartRateLog> items = new ArrayList<>();
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

    public void submit(List<HeartRateLog> data) {
        items.clear();
        if (data != null) items.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_heart_rate_log, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        HeartRateLog log = items.get(position);

        h.tvBpm.setText(log.bpm + " BPM");

        String when = sdf.format(new Date(log.timestampMs));
        h.tvTime.setText(when);

        String meta = "Quality " + String.format(Locale.getDefault(), "%.2f", log.quality)
                + " \u2022 " + (log.isVirtual ? "Virtual" : "Real")
                + " \u2022 " + (log.isAbnormal ? "Abnormal" : "Normal");

        h.tvMeta.setText(meta);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvBpm, tvMeta, tvTime;

        VH(@NonNull View itemView) {
            super(itemView);
            tvBpm = itemView.findViewById(R.id.tvBpm);
            tvMeta = itemView.findViewById(R.id.tvMeta);
            tvTime = itemView.findViewById(R.id.tvTime);
        }
    }
}
