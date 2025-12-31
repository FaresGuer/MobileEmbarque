package com.example.projet.Fragments.Fall;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.VibrationEffect;
import android.os.Vibrator;
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
import com.example.projet.Entities.SafetyModule.FallDetector;
import com.example.projet.Entities.SafetyModule.FallEvent;
import com.example.projet.Entities.SafetyModule.MotionSample;
import com.example.projet.Entities.SafetyModule.Interfaces.MotionSource;
import com.example.projet.Entities.SafetyModule.PhoneMotionSource;
import com.example.projet.Entities.SafetyModule.SimulatedMotionSource;
import com.example.projet.Fragments.Health.HealthFragment;
import com.example.projet.R;
import com.example.projet.Utils.SmsAlertSender;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

public class FallFragment extends Fragment {
    public interface MenuListener {
        void onOpenMenu();
    }
    private MenuListener menuListener;
    private Switch swVirtual;
    private Button btnStartStop;
    private Button btnSimFall, btnSimDrop, btnSimWalk, btnSimIdle;
    private Button btnLogs;
    private TextView tvStatus, tvMag;

    private boolean running = false;

    private MotionSource source;
    private SimulatedMotionSource simSource;

    private FallDetector detector;
    private float lastMagG = 1.0f;
    private float peakG = 1.0f;

    private FusedLocationProviderClient fusedLocationClient;

    private AlertDialog countdownDialog;
    private CountDownTimer timer;
    private boolean userCancelled = false;

    private final ActivityResultLauncher<String[]> permissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestMultiplePermissions(),
                    result -> {
                        boolean smsOk = Boolean.TRUE.equals(result.get(Manifest.permission.SEND_SMS));
                        boolean locOk = Boolean.TRUE.equals(result.get(Manifest.permission.ACCESS_FINE_LOCATION));

                        if (!smsOk || !locOk) {
                            Toast.makeText(requireContext(), "SMS and Location permissions are required for emergency alerts.", Toast.LENGTH_LONG).show();
                        }
                    }
            );
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
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_fall, container, false);
        ImageButton btnOpenMenu = v.findViewById(R.id.btnOpenMenu);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        swVirtual = v.findViewById(R.id.swVirtualMotion);
        btnStartStop = v.findViewById(R.id.btnStartStopSafety);
        btnSimFall = v.findViewById(R.id.btnSimFall);
        btnSimDrop = v.findViewById(R.id.btnSimDrop);
        btnSimWalk = v.findViewById(R.id.btnSimWalk);
        btnSimIdle = v.findViewById(R.id.btnSimIdle);
        btnLogs = v.findViewById(R.id.btnSafetyLogs);

        tvStatus = v.findViewById(R.id.tvSafetyStatus);
        tvMag = v.findViewById(R.id.tvSafetyMag);

        FallDetector.Config cfg = new FallDetector.Config();
        detector = new FallDetector(cfg);

        setVirtualButtonsEnabled(swVirtual.isChecked());

        swVirtual.setOnCheckedChangeListener((buttonView, isChecked) -> setVirtualButtonsEnabled(isChecked));

        btnStartStop.setOnClickListener(view -> {
            if (!running) startMonitoring();
            else stopMonitoring();
        });

        btnLogs.setOnClickListener(view -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new SafetyLogsFragment())
                    .addToBackStack(null)
                    .commit();
        });
        btnOpenMenu.setOnClickListener(view -> {
            if (menuListener != null) menuListener.onOpenMenu();
        });

        btnSimFall.setOnClickListener(view -> { if (simSource != null) simSource.setScenario(SimulatedMotionSource.Scenario.FALL); });
        btnSimDrop.setOnClickListener(view -> { if (simSource != null) simSource.setScenario(SimulatedMotionSource.Scenario.DROP); });
        btnSimWalk.setOnClickListener(view -> { if (simSource != null) simSource.setScenario(SimulatedMotionSource.Scenario.WALK); });
        btnSimIdle.setOnClickListener(view -> { if (simSource != null) simSource.setScenario(SimulatedMotionSource.Scenario.IDLE); });

        return v;
    }

    private void setVirtualButtonsEnabled(boolean enabled) {
        btnSimFall.setEnabled(enabled);
        btnSimDrop.setEnabled(enabled);
        btnSimWalk.setEnabled(enabled);
        btnSimIdle.setEnabled(enabled);
    }

    private void startMonitoring() {
        if (UserSession.getUser() == null) {
            Toast.makeText(requireContext(), "Please login first.", Toast.LENGTH_LONG).show();
            return;
        }

        requestEmergencyPermissionsIfNeeded();

        running = true;
        btnStartStop.setText("Stop");
        tvStatus.setText("Monitoring started");
        peakG = 1.0f;

        boolean virtual = swVirtual.isChecked();

        if (virtual) {
            simSource = new SimulatedMotionSource(50);
            simSource.setScenario(SimulatedMotionSource.Scenario.IDLE);
            source = simSource;
        } else {
            simSource = null;
            source = new PhoneMotionSource(requireContext(), false);
        }

        source.start(new MotionSource.Callback() {
            @Override
            public void onSample(MotionSample s) {
                handleSample(s);
            }

            @Override
            public void onError(String msg) {
                requireActivity().runOnUiThread(() -> Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show());
            }
        });
    }

    private void stopMonitoring() {
        running = false;
        btnStartStop.setText("Start");
        tvStatus.setText("Monitoring stopped");
        tvMag.setText("G: 1.00");

        if (timer != null) timer.cancel();
        if (countdownDialog != null && countdownDialog.isShowing()) countdownDialog.dismiss();

        if (source != null) {
            source.stop();
            source = null;
        }
    }

    private void handleSample(MotionSample s) {
        float mag = (float) Math.sqrt(s.ax * s.ax + s.ay * s.ay + s.az * s.az);
        float g = mag / 9.81f;
        lastMagG = g;
        if (g > peakG) peakG = g;

        FallDetector.Event e = detector.onSample(s);

        requireActivity().runOnUiThread(() -> {
            tvMag.setText(String.format(java.util.Locale.getDefault(), "G: %.2f", lastMagG));
        });

        if (e == FallDetector.Event.POSSIBLE_FALL) {
            requireActivity().runOnUiThread(() -> tvStatus.setText("Possible impact detected"));
            logEvent("POSSIBLE", null, false, false);
        }

        if (e == FallDetector.Event.CONFIRMED_FALL) {
            requireActivity().runOnUiThread(() -> tvStatus.setText("Fall confirmed"));
            onConfirmedFall();
        }
    }

    private void onConfirmedFall() {
        if (!running) return;

        playLocalAlarm();

        userCancelled = false;
        showCountdownDialog();
    }

    private void showCountdownDialog() {
        View d = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_fall_countdown, null, false);
        TextView tv = d.findViewById(R.id.tvCountdown);
        Button btnOk = d.findViewById(R.id.btnImOk);

        AlertDialog.Builder b = new AlertDialog.Builder(requireContext());
        b.setTitle("Fall detected");
        b.setView(d);
        b.setCancelable(false);

        countdownDialog = b.create();
        countdownDialog.show();

        btnOk.setOnClickListener(v -> {
            userCancelled = true;
            if (timer != null) timer.cancel();
            countdownDialog.dismiss();
            tvStatus.setText("User responded: OK");
            logEvent("CONFIRMED", null, false, true);
        });

        timer = new CountDownTimer(20000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long sec = millisUntilFinished / 1000;
                tv.setText("Sending emergency alert in " + sec + " seconds");
            }

            @Override
            public void onFinish() {
                if (countdownDialog != null && countdownDialog.isShowing()) countdownDialog.dismiss();
                if (!userCancelled) {
                    tvStatus.setText("Sending emergency alert");
                    fetchLocationAndSendSms();
                }
            }
        }.start();
    }

    private void playLocalAlarm() {
        try {
            ToneGenerator tg = new ToneGenerator(AudioManager.STREAM_ALARM, 90);
            tg.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 1200);
        } catch (Exception ignored) {
        }

        try {
            Vibrator vib = (Vibrator) requireContext().getSystemService(Context.VIBRATOR_SERVICE);
            if (vib != null) {
                vib.vibrate(VibrationEffect.createWaveform(new long[]{0, 400, 200, 400, 200, 600}, -1));
            }
        } catch (Exception ignored) {
        }
    }

    private void fetchLocationAndSendSms() {
        if (UserSession.getUser() == null) return;

        boolean smsGranted = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED;
        boolean locGranted = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;

        if (!smsGranted || !locGranted) {
            Toast.makeText(requireContext(), "SMS and Location permissions are required for emergency alerts.", Toast.LENGTH_LONG).show();
            logEvent("CONFIRMED", null, false, false);
            return;
        }

        int userId = UserSession.getUser().getId();

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    String msg = buildEmergencyMessage(location);
                    SmsAlertSender.sendMessageToPrimary(requireActivity(), userId, msg);
                    logEvent("CONFIRMED", location, true, false);
                })
                .addOnFailureListener(e -> {
                    String msg = buildEmergencyMessage(null);
                    SmsAlertSender.sendMessageToPrimary(requireActivity(), userId, msg);
                    logEvent("CONFIRMED", null, true, false);
                });
    }

    private String buildEmergencyMessage(Location location) {
        StringBuilder sb = new StringBuilder();
        sb.append("Emergency: Possible fall detected. ");
        sb.append("Peak G: ").append(String.format(java.util.Locale.getDefault(), "%.2f", peakG)).append(". ");

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

    private void logEvent(String type, Location location, boolean smsSent, boolean cancelled) {
        if (UserSession.getUser() == null) return;

        int userId = UserSession.getUser().getId();
        long now = System.currentTimeMillis();

        FallEvent e = new FallEvent();
        e.ownerUserId = userId;
        e.timestampMs = now;
        e.type = type;
        e.peakG = peakG;
        e.smsSent = smsSent;
        e.userCancelled = cancelled;

        if (location != null) {
            e.lat = location.getLatitude();
            e.lon = location.getLongitude();
        } else {
            e.lat = null;
            e.lon = null;
        }

        AppDatabase db = AppDatabase.getInstance(requireContext());
        new Thread(() -> {
            db.fallEventDao().insert(e);
            long cutoff = System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000;
            db.fallEventDao().deleteOlderThan(cutoff);
        }).start();
    }

    private void requestEmergencyPermissionsIfNeeded() {
        boolean smsGranted = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED;
        boolean locGranted = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;

        if (smsGranted && locGranted) return;

        permissionLauncher.launch(new String[]{
                Manifest.permission.SEND_SMS,
                Manifest.permission.ACCESS_FINE_LOCATION
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopMonitoring();
    }
}
