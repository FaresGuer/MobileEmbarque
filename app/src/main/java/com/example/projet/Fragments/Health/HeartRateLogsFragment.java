package com.example.projet.Fragments.Health;

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

import com.example.projet.DataBase.AppDatabase;
import com.example.projet.DataBase.UserSession;
import com.example.projet.Entities.HealthModule.HeartRateLog;
import com.example.projet.Fragments.Adapters.HeartRateLogAdapter;
import com.example.projet.R;

import java.util.List;

public class HeartRateLogsFragment extends Fragment {
    private HeartRateLogAdapter adapter;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_heart_rate_logs, container, false);

        ImageButton btnBack = v.findViewById(R.id.btnBack);
        RecyclerView rv = v.findViewById(R.id.rvLogs);

        adapter = new HeartRateLogAdapter();
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(adapter);

        btnBack.setOnClickListener(x -> requireActivity().getSupportFragmentManager().popBackStack());

        loadData();
        return v;
    }

    private void loadData() {
        if (UserSession.getUser() == null) {
            Toast.makeText(requireContext(), "No user session.", Toast.LENGTH_SHORT).show();
            return;
        }

        int userId = UserSession.getUser().getId();
        AppDatabase db = AppDatabase.getInstance(requireContext());

        new Thread(() -> {
            List<HeartRateLog> logs = db.heartRateLogDao().getForUser(userId);
            requireActivity().runOnUiThread(() -> adapter.submit(logs));
        }).start();
    }
}
