package com.example.projet.Fragments.Adapters;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projet.DTO.FriendRequestItem;
import com.example.projet.R;

import java.util.ArrayList;
import java.util.List;

public class FriendRequestsAdapter extends RecyclerView.Adapter<FriendRequestsAdapter.VH> {

    public interface Listener {
        void onAccept(FriendRequestItem item);
        void onReject(FriendRequestItem item);
    }

    private final Listener listener;
    private final List<FriendRequestItem> items = new ArrayList<>();

    public FriendRequestsAdapter(Listener listener) {
        this.listener = listener;
    }

    public void submit(List<FriendRequestItem> data) {
        items.clear();
        if (data != null) items.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend_request, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        FriendRequestItem item = items.get(position);

        h.tvUser.setText(item.username != null ? item.username : "User");
        h.tvEmail.setText(item.email != null ? item.email : "");

        h.btnAccept.setOnClickListener(v -> listener.onAccept(item));
        h.btnReject.setOnClickListener(v -> listener.onReject(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvUser, tvEmail;
        Button btnAccept, btnReject;

        VH(@NonNull View itemView) {
            super(itemView);
            tvUser = itemView.findViewById(R.id.tvUser);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            btnAccept = itemView.findViewById(R.id.btnAccept);
            btnReject = itemView.findViewById(R.id.btnReject);
        }
    }
}