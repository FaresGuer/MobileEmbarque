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

import com.example.projet.R;
import com.example.projet.Entities.HealthModule.CameraPpgSource;
import com.example.projet.Entities.HealthModule.HeartRateEstimator;
import com.example.projet.Entities.HealthModule.Interfaces.PpgSource;
import com.example.projet.Entities.HealthModule.SimulatedPpgSource;
import android.location.Location;

import com.example.projet.Utils.SmsAlertSender;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import androidx.camera.view.PreviewView;

import java.util.Locale;

public class HealthFragment extends Fragment {
    public interface MenuListener {
        void onOpenMenu();
    }

    private MenuListener menuListener;
    private long lastSavedMs = 0;
    private static final int HR_LOW = 50;
    private static final int HR_HIGH = 120;
    private static final float MIN_QUALITY = 0.65f;

    private static final long ABNORMAL_PERSIST_MS = 8000L;
    private static final long ALERT_COOLDOWN_MS = 120000L;

    private Long abnormalSinceMs = null;
    private long lastAlertMs = 0L;
    private FusedLocationProviderClient fusedLocationClient;
    private PreviewView previewView;
    private final ActivityResultLauncher<String[]> smsLocationPermissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestMultiplePermissions(),
                    result -> {
                        boolean smsGranted = Boolean.TRUE.equals(result.get(Manifest.permission.SEND_SMS));
                        boolean locGranted = Boolean.TRUE.equals(result.get(Manifest.permission.ACCESS_FINE_LOCATION));

                        if (smsGranted && locGranted) {
                            Toast.makeText(requireContext(), "Emergency permissions granted.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(requireContext(), "SMS and Location permissions are required for emergency alerts.", Toast.LENGTH_LONG).show();
                        }
                    }
            );
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
            checkAndSendPrimarySms(r);
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
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
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
            source = new SimulatedPpgSource(30, 140f);
            beginSource();
            requestSmsLocation();
            return;
        }

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
            return;
        }

        source = new CameraPpgSource(requireContext(), getViewLifecycleOwner(), previewView);
        beginSource();
        requestSmsLocation();
    }
    private void requestSmsLocation() {
        boolean smsGranted = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED;
        boolean locGranted = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;

        if (smsGranted && locGranted) return;

        smsLocationPermissionLauncher.launch(new String[]{
                Manifest.permission.SEND_SMS,
                Manifest.permission.ACCESS_FINE_LOCATION
        });
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
    private void checkAndSendPrimarySms(HeartRateEstimator.Result r) {
        if (!running) return;
        if (r == null || !r.valid) {
            abnormalSinceMs = null;
            return;
        }
        if (r.quality < MIN_QUALITY) {
            abnormalSinceMs = null;
            return;
        }

        boolean abnormal = (r.bpm < HR_LOW) || (r.bpm > HR_HIGH);
        long now = System.currentTimeMillis();

        if (!abnormal) {
            abnormalSinceMs = null;
            return;
        }

        if (abnormalSinceMs == null) abnormalSinceMs = now;

        if (now - abnormalSinceMs < ABNORMAL_PERSIST_MS) return;
        if (now - lastAlertMs < ALERT_COOLDOWN_MS) return;

        lastAlertMs = now;
        sendPrimarySmsWithLocation(r);
    }

    private void sendPrimarySmsWithLocation(HeartRateEstimator.Result r) {
        if (UserSession.getUser() == null) return;

        boolean smsGranted = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED;
        boolean locGranted = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;

        if (!smsGranted || !locGranted) {
            Toast.makeText(requireContext(), "SMS and Location permissions are required for emergency alerts.", Toast.LENGTH_LONG).show();
            return;
        }

        int userId = UserSession.getUser().getId();

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    String msg = buildHrEmergencyMessage(r, location);
                    SmsAlertSender.sendMessageToPrimary(requireActivity(), userId, msg);
                })
                .addOnFailureListener(e -> {
                    String msg = buildHrEmergencyMessage(r, null);
                    SmsAlertSender.sendMessageToPrimary(requireActivity(), userId, msg);
                });
    }

    private String buildHrEmergencyMessage(HeartRateEstimator.Result r, Location location) {
        String state = (r.bpm < HR_LOW) ? "LOW" : "HIGH";

        StringBuilder sb = new StringBuilder();
        sb.append("Emergency: Heart rate ").append(state)
                .append(" (").append(r.bpm).append(" bpm). ")
                .append("Quality ").append(String.format(Locale.getDefault(), "%.2f", r.quality)).append(". ");

        if (location != null) {
            double lat = location.getLatitude();
            double lon = location.getLongitude();
            sb.append("Location: ").append(lat).append(", ").append(lon).append(". ");
            sb.append("Map: ").append("https://maps.google.com/?q=").append(lat).append(",").append(lon);
        } else {
            sb.append("Location unavailable.");
        }

        return sb.toString();
    }

}
