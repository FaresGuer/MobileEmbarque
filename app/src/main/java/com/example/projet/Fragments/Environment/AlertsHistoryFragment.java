package com.example.projet.Fragments.Environment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projet.Entities.EnvironmentAlert;
import com.example.projet.R;
import com.example.projet.Repositories.EnvironmentRepository;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AlertsHistoryFragment extends Fragment {

    private EnvironmentRepository repo;
    private AlertsAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_alerts_history, container, false);
        repo = new EnvironmentRepository(requireContext());

        RecyclerView rv = view.findViewById(R.id.rvAlerts);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new AlertsAdapter(null);
        rv.setAdapter(adapter);

        Button btnClear = view.findViewById(R.id.btnClearAlerts);
        btnClear.setOnClickListener(v -> {
            // clear DB on background thread, then refresh UI
            repo.deleteAll();
            // reload
            loadAlerts();
        });

        loadAlerts();

        return view;
    }

    private void loadAlerts() {
        new Thread(() -> {
            List<EnvironmentAlert> list = repo.getAllAlerts();
            requireActivity().runOnUiThread(() -> {
                adapter.setItems(list);
            });
        }).start();
    }
}
