package com.example.projet.Fragments.Friend;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projet.DTO.FriendItem;
import com.example.projet.DataBase.AppDatabase;
import com.example.projet.DataBase.UserSession;
import com.example.projet.Entities.Enums.FriendStatus;
import com.example.projet.Entities.Friend;
import com.example.projet.Entities.User;
import com.example.projet.Fragments.Adapters.FriendsUserAdapter;
import com.example.projet.R;
import com.example.projet.Repositories.FriendEmergencyRepository;

import java.util.List;

public class FriendsFragment extends Fragment {

    public interface MenuListener {
        void onOpenMenu();
    }

    private MenuListener menuListener;
    private FriendsUserAdapter adapter;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof MenuListener) {
            menuListener = (MenuListener) context;
        } else {
            throw new IllegalStateException("MainActivity must implement FriendsFragment.MenuListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        menuListener = null;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friends, container, false);

        ImageButton btnOpenMenu = view.findViewById(R.id.btnOpenMenu);
        RecyclerView rv = view.findViewById(R.id.rvFriends);
        Button btnAdd = view.findViewById(R.id.btnAdd);
        ImageButton btnRequests = view.findViewById(R.id.btnRequests);

        btnOpenMenu.setOnClickListener(v -> {
            if (menuListener != null) menuListener.onOpenMenu();
        });
        btnRequests.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new FriendRequestsFragment())
                    .addToBackStack(null)
                    .commit();
        });

        rv.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new FriendsUserAdapter(new FriendsUserAdapter.Listener() {

            @Override
            public void onLongClick(FriendItem item) {
                deleteFriend(item.friendRowId);
            }

            @Override public void onToggleFavorite(FriendItem item) { toggleFavorite(item); }
            @Override public void onToggleEmergency(FriendItem item) { toggleEmergency(item); }
            @Override public void onDeleteFriend(int friendRowId) { deleteFriend(friendRowId); }
        });

        rv.setAdapter(adapter);

        btnAdd.setOnClickListener(v -> openCreate());

        loadData();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        User owner = UserSession.getUser();
        if (owner == null) {
            Toast.makeText(requireContext(), "No user session.", Toast.LENGTH_SHORT).show();
            return;
        }

        AppDatabase db = AppDatabase.getInstance(requireContext());

        new Thread(() -> {
            List<FriendItem> data = db.friendDao().getFriendItems(owner.getId());
            requireActivity().runOnUiThread(() -> adapter.submit(data));
        }).start();
    }

    private void openCreate() {
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, AddEditFriendFragment.newCreate())
                .addToBackStack(null)
                .commit();
    }

    private void toggleFavorite(FriendItem item) {
        AppDatabase db = AppDatabase.getInstance(requireContext());
        new Thread(() -> {
            db.friendDao().setFavorite(item.friendRowId, !item.isFavorite);
            requireActivity().runOnUiThread(this::loadData);
        }).start();
    }

    private void toggleEmergency(FriendItem item) {
        int ownerId = UserSession.getUser().getId();
        FriendEmergencyRepository repo = new FriendEmergencyRepository(requireContext());

        new Thread(() -> {
            repo.toggleFriendEmergency(ownerId, item.friendUserId);
            requireActivity().runOnUiThread(this::loadData);
        }).start();
    }
    private void deleteFriend(int friendRowId) {
        AppDatabase db = AppDatabase.getInstance(requireContext());

        new Thread(() -> {
            Friend f = db.friendDao().getById(friendRowId);
            if (f == null) {
                requireActivity().runOnUiThread(this::loadData);
                return;
            }

            if (f.status == FriendStatus.ACCEPTED) {
                db.friendDao().deleteMutual(f.ownerUserId, f.friendUserId);
            } else {
                db.friendDao().delete(f);
            }
            db.emergencyContactDao().deleteByOwnerAndFriend(f.ownerUserId, f.friendUserId);

            requireActivity().runOnUiThread(this::loadData);
        }).start();
    }
}
