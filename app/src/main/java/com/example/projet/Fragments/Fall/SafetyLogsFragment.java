package com.example.projet.Fragments.Fall;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projet.DataBase.AppDatabase;
import com.example.projet.DataBase.UserSession;
import com.example.projet.Entities.SafetyModule.FallEvent;
import com.example.projet.Fragments.Adapters.SafetyLogsAdapter;
import com.example.projet.R;

import java.util.ArrayList;
import java.util.List;

public class SafetyLogsFragment extends Fragment {

    private RecyclerView rv;
    private SafetyLogsAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_safety_logs, container, false);
        ImageButton btnBack = v.findViewById(R.id.btnBack);
        rv = v.findViewById(R.id.rvSafetyLogs);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new SafetyLogsAdapter(new ArrayList<>());
        rv.setAdapter(adapter);
        btnBack.setOnClickListener(view ->
                requireActivity().getSupportFragmentManager().popBackStack()
        );
        load();
        return v;
    }

    private void load() {
        if (UserSession.getUser() == null) return;

        int userId = UserSession.getUser().getId();
        AppDatabase db = AppDatabase.getInstance(requireContext());

        new Thread(() -> {
            List<FallEvent> list = db.fallEventDao().getAllForUser(userId);
            requireActivity().runOnUiThread(() -> adapter.setItems(list));
        }).start();
    }
}