package com.example.projet.Fragments.AlertHistory;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projet.DataBase.UserSession;
import com.example.projet.Entities.AlertHistory;
import com.example.projet.Entities.User;
import com.example.projet.Fragments.Control.ControlFragment;
import com.example.projet.R;
import com.example.projet.Repositories.AlertHistoryRepository;

import java.util.ArrayList;
import java.util.List;

public class AlertHistoryFragment extends Fragment {

    public interface MenuListener {
        void onOpenMenu();
    }

    private MenuListener menuListener;
    private AlertHistoryRepository repository;
    private AlertHistoryAdapter adapter;
    private RecyclerView rvAlerts;
    private TextView tvEmpty;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof MenuListener) {
            menuListener = (MenuListener) context;
        } else {
            throw new IllegalStateException("MainActivity must implement MenuListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        menuListener = null;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_alert_history, container, false);

        ImageButton btnOpenMenu = view.findViewById(R.id.btnOpenMenu);
        btnOpenMenu.setOnClickListener(v -> {
            if (menuListener != null) menuListener.onOpenMenu();
        });

        repository = new AlertHistoryRepository(requireContext());
        rvAlerts = view.findViewById(R.id.rvAlerts);
        tvEmpty = view.findViewById(R.id.tvEmpty);

        rvAlerts.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new AlertHistoryAdapter(new ArrayList<>());
        rvAlerts.setAdapter(adapter);

        Button btnClear = view.findViewById(R.id.btnClearAlerts);
        btnClear.setOnClickListener(v -> {
            User user = com.example.projet.DataBase.UserSession.getUser();
            if (user != null) {
                repository.deleteAlertsForUser(user.getId());
                loadAlerts();
            }
        });

        loadAlerts();

        return view;
    }

    private void loadAlerts() {
        User user = com.example.projet.DataBase.UserSession.getUser();
        if (user == null) {
            tvEmpty.setText("Please login to view alerts");
            tvEmpty.setVisibility(View.VISIBLE);
            rvAlerts.setVisibility(View.GONE);
            return;
        }

        new Thread(() -> {
            List<AlertHistory> alerts = repository.getAlertsForUser(user.getId());
            requireActivity().runOnUiThread(() -> {
                if (alerts.isEmpty()) {
                    tvEmpty.setVisibility(View.VISIBLE);
                    rvAlerts.setVisibility(View.GONE);
                } else {
                    tvEmpty.setVisibility(View.GONE);
                    rvAlerts.setVisibility(View.VISIBLE);
                    adapter.setItems(alerts);
                }
            });
        }).start();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadAlerts();
    }
}

