package com.example.projet.Fragments.Adapters;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projet.Entities.EmergencyContact;
import com.example.projet.R;

import java.util.ArrayList;
import java.util.List;

public class EmergencyContactsAdapter extends RecyclerView.Adapter<EmergencyContactsAdapter.VH> {

    public interface Listener {
        void onClick(EmergencyContact c);
        void onLongClick(EmergencyContact c);
    }

    private final Listener listener;
    private final List<EmergencyContact> items = new ArrayList<>();

    public EmergencyContactsAdapter(Listener listener) {
        this.listener = listener;
    }

    public void submit(List<EmergencyContact> data) {
        items.clear();
        if (data != null) items.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_emergency_contact, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        EmergencyContact c = items.get(position);

        h.tvName.setText(c.displayName);
        h.tvPhone.setText(c.phoneNumber);

        h.tvPrimary.setVisibility(c.isPrimary ? View.VISIBLE : View.GONE);

        h.itemView.setOnClickListener(v -> listener.onClick(c));
        h.itemView.setOnLongClickListener(v -> {
            listener.onLongClick(c);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvName, tvPhone, tvRelationship, tvPrimary;

        VH(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvPhone = itemView.findViewById(R.id.tvPhone);
            tvRelationship = itemView.findViewById(R.id.tvRelationship);
            tvPrimary = itemView.findViewById(R.id.tvPrimary);
        }
    }
}