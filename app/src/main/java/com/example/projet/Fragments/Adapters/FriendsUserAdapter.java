package com.example.projet.Fragments.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projet.DTO.FriendItem;
import com.example.projet.R;

import java.util.ArrayList;
import java.util.List;

public class FriendsUserAdapter extends RecyclerView.Adapter<FriendsUserAdapter.VH> {

    public interface Listener {
        void onLongClick(FriendItem item);
        void onToggleFavorite(FriendItem item);
        void onToggleEmergency(FriendItem item);
        void onDeleteFriend(int friendRowId);
    }

    private final Listener listener;
    private final List<FriendItem> items = new ArrayList<>();

    public FriendsUserAdapter(Listener listener) {
        this.listener = listener;
    }

    public void submit(List<FriendItem> data) {
        items.clear();
        if (data != null) items.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend_user, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        FriendItem item = items.get(position);

        h.tvName.setText(item.username != null ? item.username : "User");
        h.tvEmail.setText(item.email != null ? item.email : "");

        h.tvFav.setVisibility(item.isFavorite ? View.VISIBLE : View.GONE);

        h.itemView.setOnLongClickListener(v -> {
            listener.onLongClick(item);
            return true;
        });
        h.itemView.setOnLongClickListener(v -> {
            PopupMenu menu = new PopupMenu(v.getContext(), v);
            menu.getMenuInflater().inflate(R.menu.menu_friend_item, menu.getMenu());

            menu.getMenu().findItem(R.id.action_toggle_favorite)
                    .setTitle(item.isFavorite ? "Remove favorite" : "Make favorite");

            menu.getMenu().findItem(R.id.action_toggle_emergency)
                    .setTitle(item.isEmergency ? "Remove emergency contact" : "Make emergency contact");

            menu.setOnMenuItemClickListener(mi -> {
                int id = mi.getItemId();
                if (id == R.id.action_toggle_favorite) {
                    listener.onToggleFavorite(item);
                    return true;
                }
                if (id == R.id.action_toggle_emergency) {
                    listener.onToggleEmergency(item);
                    return true;
                }
                if (id == R.id.action_delete) {
                    listener.onDeleteFriend(item.friendRowId);
                    return true;
                }
                return false;
            });

            menu.show();
            return true;
        });

    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvName, tvEmail, tvFav;

        VH(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvFav = itemView.findViewById(R.id.tvFav);
        }
    }
}