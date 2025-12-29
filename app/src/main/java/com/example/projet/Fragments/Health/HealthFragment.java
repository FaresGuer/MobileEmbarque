package com.example.projet.Fragments.Health;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.projet.DataBase.AppDatabase;
import com.example.projet.DataBase.UserSession;
import com.example.projet.Entities.HealthModule.HeartRateLog;
import com.example.projet.Entities.HealthModule.PpgSample;
import com.example.projet.Fragments.Fall.FallFragment;
import com.example.projet.R;
import com.example.projet.Entities.HealthModule.CameraPpgSource;
import com.example.projet.Entities.HealthModule.HeartRateEstimator;
import com.example.projet.Entities.HealthModule.Interfaces.PpgSource;
import com.example.projet.Entities.HealthModule.SimulatedPpgSource;

import androidx.camera.view.PreviewView;

import java.util.Locale;

public class HealthFragment extends Fragment {
    public interface MenuListener {
        void onOpenMenu();
    }

    private static final int REQ_CAMERA = 10;
    private MenuListener menuListener;
    private long lastSavedMs = 0;
    private PreviewView previewView;
    private ActivityResultLauncher<String> cameraPermissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    isGranted -> {
                        if (isGranted) {
                            source = new CameraPpgSource(
                                    requireContext(),
                                    getViewLifecycleOwner(),
                                    previewView
                            );
                            beginSource();
                        } else {
                            Toast.makeText(
                                    requireContext(),
                                    "Camera permission denied.",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }
            );
    private TextView tvBpm, tvQuality;
    private Switch swVirtual;
    private Button btnStart;

    private PpgSource source;
    private HeartRateEstimator estimator;
    private final Handler uiHandler = new Handler(Looper.getMainLooper());

    private boolean running = false;

    private final Runnable updateUiTask = new Runnable() {
        @Override
        public void run() {
            if (!running) return;

            HeartRateEstimator.Result r = estimator.estimate();
            SaveLog(r, swVirtual.isChecked());
            if (r.valid) {
                tvBpm.setText(String.valueOf(r.bpm));
                tvQuality.setText("Quality " + String.format(Locale.getDefault(), "%.2f", r.quality));
            } else {
                tvBpm.setText("--");
                tvQuality.setText("Quality: " + String.format("%.2f", r.quality) + " (low)");
            }

            uiHandler.postDelayed(this, 1000);
        }
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_heart_rate, container, false);
        ImageButton btnOpenMenu = v.findViewById(R.id.btnOpenMenu);
        ImageButton btnOpenLogs = v.findViewById(R.id.btnLogs);

        previewView = v.findViewById(R.id.previewView);
        tvBpm = v.findViewById(R.id.tvBpm);
        tvQuality = v.findViewById(R.id.tvQuality);
        swVirtual = v.findViewById(R.id.swVirtual);
        btnStart = v.findViewById(R.id.btnStart);

        estimator = new HeartRateEstimator(12_000);

        btnStart.setOnClickListener(x -> {
            if (!running) startMeasurement();
            else stopMeasurement();
        });
        btnOpenMenu.setOnClickListener(x -> {
            if (menuListener != null) menuListener.onOpenMenu();
        });
        btnOpenLogs.setOnClickListener(x -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new HeartRateLogsFragment())
                    .addToBackStack(null)
                    .commit();
        });
        return v;
    }

    private void startMeasurement() {
        estimator = new HeartRateEstimator(12_000);

        boolean virtualMode = swVirtual.isChecked();

        if (virtualMode) {
            source = new SimulatedPpgSource(30, 78f);
            beginSource();
            return;
        }

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
            return;
        }

        source = new CameraPpgSource(requireContext(), getViewLifecycleOwner(), previewView);
        beginSource();
    }

    private void beginSource() {
        running = true;
        btnStart.setText("Stop");

        source.start(new PpgSource.Callback() {
            @Override
            public void onSample(PpgSample s) {
                estimator.add(s);
            }

            @Override
            public void onError(String msg) {
                uiHandler.post(() -> Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show());
            }
        });

        uiHandler.post(updateUiTask);
        Toast.makeText(requireContext(), "Place finger on camera and flash, hold still.", Toast.LENGTH_SHORT).show();
    }

    private void stopMeasurement() {
        running = false;
        btnStart.setText("Start");
        uiHandler.removeCallbacks(updateUiTask);

        if (source != null) {
            source.stop();
            source = null;
        }

        tvBpm.setText("--");
        tvQuality.setText("Quality: --");
        AppDatabase db = AppDatabase.getInstance(requireContext());
        long cutoff = System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000; // 30 days
        new Thread(() -> db.heartRateLogDao().deleteOlderThan(cutoff)).start();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopMeasurement();
    }

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
    private void SaveLog(HeartRateEstimator.Result r, boolean isVirtual) {
        if (!r.valid) return;
        if (r.quality < 0.55f) return;

        long now = System.currentTimeMillis();
        if (now - lastSavedMs < 5000) return;
        lastSavedMs = now;

        if (UserSession.getUser() == null) return;

        int userId = UserSession.getUser().getId();
        boolean abnormal = (r.bpm < 50 || r.bpm > 120);

        HeartRateLog log = new HeartRateLog();
        log.ownerUserId = userId;
        log.timestampMs = now;
        log.bpm = r.bpm;
        log.quality = r.quality;
        log.isAbnormal = abnormal;
        log.isVirtual = isVirtual;
        log.contextTag = null;

        AppDatabase db = AppDatabase.getInstance(requireContext());
        new Thread(() -> db.heartRateLogDao().insert(log)).start();
    }

}
