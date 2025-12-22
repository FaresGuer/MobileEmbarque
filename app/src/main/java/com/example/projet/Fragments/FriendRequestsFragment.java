package com.example.projet.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.example.projet.DTO.FriendRequestItem;

import com.example.projet.DataBase.AppDatabase;
import com.example.projet.DataBase.UserSession;
import com.example.projet.Fragments.Adapters.FriendRequestsAdapter;

import com.example.projet.R;
import com.example.projet.Repositories.FriendRequestRepository;


import java.util.List;

public class FriendRequestsFragment extends Fragment {


    private FriendRequestsAdapter adapter;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friend_requests, container, false);
        ImageButton btnBack = view.findViewById(R.id.btnBack);
        RecyclerView rv = view.findViewById(R.id.rvRequests);

        btnBack.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack()
        );

        rv.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new FriendRequestsAdapter(new FriendRequestsAdapter.Listener() {
            @Override
            public void onAccept(FriendRequestItem item) {
                accept(item.friendRowId);
            }

            @Override
            public void onReject(FriendRequestItem item) {
                reject(item.friendRowId);
            }
        });

        rv.setAdapter(adapter);

        load();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        load();
    }

    private void load() {
        if (UserSession.getUser() == null) {
            Toast.makeText(requireContext(), "No user session.", Toast.LENGTH_SHORT).show();
            return;
        }

        int myId = UserSession.getUser().getId();
        AppDatabase db = AppDatabase.getInstance(requireContext());

        new Thread(() -> {
            List<FriendRequestItem> data = db.friendDao().getIncomingRequestItems(myId);
            requireActivity().runOnUiThread(() -> adapter.submit(data));
        }).start();
    }

    private void accept(int requestRowId) {
        FriendRequestRepository repo = new FriendRequestRepository(requireContext());
        new Thread(() -> {
            repo.acceptRequest(requestRowId);
            requireActivity().runOnUiThread(this::load);
        }).start();
    }

    private void reject(int requestRowId) {
        FriendRequestRepository repo = new FriendRequestRepository(requireContext());
        new Thread(() -> {
            repo.rejectRequest(requestRowId);
            requireActivity().runOnUiThread(this::load);
        }).start();
    }
}
