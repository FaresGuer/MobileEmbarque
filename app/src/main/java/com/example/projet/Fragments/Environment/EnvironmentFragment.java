package com.example.projet.Fragments.Environment;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.core.widget.ImageViewCompat;
import android.content.res.ColorStateList;

import com.example.projet.R;
import com.example.projet.Sensors.EnvironmentMonitor;
import com.example.projet.Utils.AlertSender;
import com.example.projet.Repositories.EnvironmentRepository;
import com.example.projet.Entities.EnvironmentAlert;
import com.example.projet.Utils.SmsAlertSender;
import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class EnvironmentFragment extends Fragment implements EnvironmentMonitor.Listener {
    public interface MenuListener {
        void onOpenMenu();
    }

    private MenuListener menuListener;

    // UI
    private TextView tvTemperatureValue, tvLightValue, tvProximityValue, tvLastUpdated;
    private ImageView ivTemperatureStatus, ivLightStatus, ivProximityStatus;
    private Switch switchEnableAlerts;

    private EnvironmentMonitor monitor;
    private boolean alertsEnabled = true;
    private boolean proximityAvailable = false;
    private boolean proximityReceived = false;
    private EnvironmentRepository envRepo;

    private static final int REQ_SMS = 1001;

    // Pending SMS send state (used when asking permission)
    private EnvironmentAlert pendingSmsAlert = null;
    private boolean smsCanceled = false;
    private final Handler smsHandler = new Handler(Looper.getMainLooper());
    private Runnable smsRunnable = null;

    // Proximity stabilization
    private static final int PROX_STATE_UNKNOWN = 0;
    private static final int PROX_STATE_NEAR = 1;
    private static final int PROX_STATE_FAR = 2;
    private static final int PROX_STATE_NUMERIC = 3;

    private static final int PROX_WINDOW = 5;
    private static final long PROX_DEBOUNCE_MS = 600; // ms

    private int proxDisplayedState = PROX_STATE_UNKNOWN;
    private int proxPendingState = PROX_STATE_UNKNOWN;
    private long proxPendingSinceMs = 0;
    private float[] proxRaw = new float[PROX_WINDOW];
    private int proxRawIndex = 0;
    private int proxRawFilled = 0;
    private float proxLastNumeric = Float.NaN;
    private long lastProxStateChangeMs = 0;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof MenuListener) menuListener = (MenuListener) context;
        else throw new IllegalStateException("Activity must implement MenuListener");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        menuListener = null;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_environment, container, false);
        // menu button - forward clicks to hosting activity via MenuListener
        ImageButton btnOpenMenu = view.findViewById(R.id.btnOpenMenu);
        btnOpenMenu.setOnClickListener(v -> {
            Log.d("EnvironmentFragment", "btnOpenMenu clicked, menuListener=" + (menuListener != null));
            if (menuListener != null) {
                try { menuListener.onOpenMenu(); } catch (Exception ex) { Log.w("EnvironmentFragment","menuListener.onOpenMenu failed", ex); }
                return;
            }

            // Direct fallback: if hosting activity is MainActivity, call its onOpenMenu() method
            Activity act = getActivity();
            if (act instanceof com.example.projet.MainActivity) {
                try {
                    ((com.example.projet.MainActivity) act).onOpenMenu();
                    return;
                } catch (Exception ex) {
                    Log.w("EnvironmentFragment", "Direct MainActivity.onOpenMenu invocation failed", ex);
                }
            }

            // Nothing handled the menu open - show a friendly toast
            AlertSender.sendToast(requireContext(), "Menu not available");
        });

        tvTemperatureValue = view.findViewById(R.id.tvTemperatureValue);
        tvLightValue = view.findViewById(R.id.tvLightValue);
        tvProximityValue = view.findViewById(R.id.tvProximityValue);
        tvLastUpdated = view.findViewById(R.id.tvLastUpdated);

        ivTemperatureStatus = view.findViewById(R.id.ivTemperatureStatus);
        ivLightStatus = view.findViewById(R.id.ivLightStatus);
        ivProximityStatus = view.findViewById(R.id.ivProximityStatus);

        switchEnableAlerts = view.findViewById(R.id.switchEnableAlerts);
        switchEnableAlerts.setOnCheckedChangeListener((b, checked) -> alertsEnabled = checked);

        envRepo = new EnvironmentRepository(requireContext());
        monitor = new EnvironmentMonitor(requireContext());
        monitor.setListener(this);
        monitor.start();

        proximityAvailable = monitor.hasProximity();
        proximityReceived = false;
        if (proximityAvailable) {
            float maxRange = monitor.getProximityMaxRange();
            if (!Float.isNaN(maxRange)) {
                tvProximityValue.setText(String.format(Locale.getDefault(), "Far (%.1f cm)", maxRange));
                java.util.Arrays.fill(proxRaw, maxRange);
                proxRawFilled = PROX_WINDOW;
                proxRawIndex = 0;
                proxDisplayedState = PROX_STATE_FAR;
            } else {
                tvProximityValue.setText("--");
            }
            ImageViewCompat.setImageTintList(ivProximityStatus, ColorStateList.valueOf(ContextCompat.getColor(requireContext(), android.R.color.darker_gray)));
            view.postDelayed(() -> {
                if (isAdded() && proximityAvailable && !proximityReceived) {
                    tvProximityValue.setText(getString(R.string.no_reading));
                }
            }, 6000);
        } else {
            tvProximityValue.setText("--");
            ImageViewCompat.setImageTintList(ivProximityStatus, ColorStateList.valueOf(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark)));
        }

        Button btnShowAlerts = view.findViewById(R.id.btnShowAlerts);
        btnShowAlerts.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new com.example.projet.Fragments.Environment.AlertsHistoryFragment())
                        .addToBackStack(null)
                        .commit();
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        AlertSender.initChannel(requireContext());
        proximityReceived = false;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (monitor != null) monitor.stop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        smsCanceled = true;
        if (smsRunnable != null) smsHandler.removeCallbacks(smsRunnable);
        pendingSmsAlert = null;
    }

    private void updateTimestamp() {
        String ts = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
        tvLastUpdated.setText(getString(R.string.last_updated_prefix, ts));
    }

    @Override
    public void onTemperature(Float celsius) {
        if (getActivity() == null) return;
        requireActivity().runOnUiThread(() -> {
            tvTemperatureValue.setText(String.format(Locale.getDefault(), "%.1f °C", celsius));
            if (celsius >= EnvironmentMonitor.TEMP_HIGH) {
                ImageViewCompat.setImageTintList(ivTemperatureStatus, ColorStateList.valueOf(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark)));
                if (alertsEnabled) {
                    AlertSender.sendToast(requireContext(), "High temperature: " + String.format(Locale.getDefault(), "%.1f °C", celsius));
                    AlertSender.vibrate(requireContext(), 400);
                    AlertSender.sendNotification(requireContext(), "Environment Alert", "High temperature: " + String.format(Locale.getDefault(), "%.1f °C", celsius));
                    EnvironmentAlert alert = new EnvironmentAlert(System.currentTimeMillis(), "Temperature",
                            String.format(Locale.getDefault(), "%.1f °C", celsius), "HIGH",
                            "High temperature detected: " + String.format(Locale.getDefault(), "%.1f °C", celsius));
                    envRepo.insertAlert(alert);
                    showSmsConfirmation(alert);
                }
            } else if (celsius <= EnvironmentMonitor.TEMP_LOW) {
                ImageViewCompat.setImageTintList(ivTemperatureStatus, ColorStateList.valueOf(ContextCompat.getColor(requireContext(), android.R.color.holo_blue_dark)));
                if (alertsEnabled) {
                    AlertSender.sendToast(requireContext(), "Low temperature: " + String.format(Locale.getDefault(), "%.1f °C", celsius));
                    AlertSender.vibrate(requireContext(), 300);
                    EnvironmentAlert alert = new EnvironmentAlert(System.currentTimeMillis(), "Temperature",
                            String.format(Locale.getDefault(), "%.1f °C", celsius), "LOW",
                            "Low temperature detected: " + String.format(Locale.getDefault(), "%.1f °C", celsius));
                    envRepo.insertAlert(alert);
                    showSmsConfirmation(alert);
                }
            } else {
                ImageViewCompat.setImageTintList(ivTemperatureStatus, ColorStateList.valueOf(ContextCompat.getColor(requireContext(), android.R.color.holo_green_dark)));
            }
            updateTimestamp();
        });
    }

    @Override
    public void onLight(Float lux) {
        if (getActivity() == null) return;
        requireActivity().runOnUiThread(() -> {
            tvLightValue.setText(String.format(Locale.getDefault(), "%.0f lx", lux));
            if (lux < EnvironmentMonitor.LIGHT_LOW) {
                ImageViewCompat.setImageTintList(ivLightStatus, ColorStateList.valueOf(ContextCompat.getColor(requireContext(), android.R.color.holo_orange_dark)));
                if (alertsEnabled) {
                    AlertSender.sendToast(requireContext(), "Low light detected: " + String.format(Locale.getDefault(), "%.0f lx", lux));
                    AlertSender.vibrate(requireContext(), 250);
                    EnvironmentAlert alert = new EnvironmentAlert(System.currentTimeMillis(), "Light",
                            String.format(Locale.getDefault(), "%.0f lx", lux), "WARNING",
                            "Low light detected: " + String.format(Locale.getDefault(), "%.0f lx", lux));
                    envRepo.insertAlert(alert);
                    showSmsConfirmation(alert);
                }
            } else {
                ImageViewCompat.setImageTintList(ivLightStatus, ColorStateList.valueOf(ContextCompat.getColor(requireContext(), android.R.color.holo_green_dark)));
            }
            updateTimestamp();
        });
    }

    @Override
    public void onProximity(Float cm) {
        if (getActivity() == null) return;
        proximityReceived = true;
        requireActivity().runOnUiThread(() -> {
            if (cm == null || Float.isNaN(cm) || Float.isInfinite(cm)) {
                tvProximityValue.setText("--");
                ImageViewCompat.setImageTintList(ivProximityStatus, ColorStateList.valueOf(ContextCompat.getColor(requireContext(), android.R.color.darker_gray)));
                proxPendingState = PROX_STATE_UNKNOWN;
                return;
            }

            proxRaw[proxRawIndex] = cm;
            proxRawIndex = (proxRawIndex + 1) % PROX_WINDOW;
            if (proxRawFilled < PROX_WINDOW) proxRawFilled++;

            int n = Math.max(1, proxRawFilled);
            float[] tmp = new float[n];
            System.arraycopy(proxRaw, 0, tmp, 0, n);
            java.util.Arrays.sort(tmp);
            float median = tmp[n / 2];

            float maxRange = monitor != null ? monitor.getProximityMaxRange() : Float.NaN;
            float eps = !Float.isNaN(maxRange) ? Math.max(0.1f, maxRange * 0.05f) : 0.5f;
            boolean isBinary = !Float.isNaN(maxRange) && maxRange <= 10.0f;

            int sampleState;
            if (!Float.isNaN(maxRange) && Math.abs(median - maxRange) <= eps) sampleState = PROX_STATE_FAR;
            else if (median <= EnvironmentMonitor.PROXIMITY_GESTURE_CM) sampleState = PROX_STATE_NEAR;
            else sampleState = PROX_STATE_NUMERIC;

            long now = System.currentTimeMillis();
            if (sampleState == proxDisplayedState) {
                // update UI accordingly
                if (sampleState == PROX_STATE_FAR) {
                    tvProximityValue.setText(isBinary ? String.format(Locale.getDefault(), "Far (%.1f cm)", maxRange) : String.format(Locale.getDefault(), "Far (%.1f cm)", median));
                    ImageViewCompat.setImageTintList(ivProximityStatus, ColorStateList.valueOf(ContextCompat.getColor(requireContext(), android.R.color.darker_gray)));
                } else if (sampleState == PROX_STATE_NEAR) {
                    tvProximityValue.setText(isBinary ? "Near" : String.format(Locale.getDefault(), "Near (%.1f cm)", median));
                    ImageViewCompat.setImageTintList(ivProximityStatus, ColorStateList.valueOf(ContextCompat.getColor(requireContext(), android.R.color.holo_blue_dark)));
                } else {
                    proxLastNumeric = median;
                    tvProximityValue.setText(String.format(Locale.getDefault(), "%.1f cm", proxLastNumeric));
                    ImageViewCompat.setImageTintList(ivProximityStatus, ColorStateList.valueOf(ContextCompat.getColor(requireContext(), proxLastNumeric <= EnvironmentMonitor.PROXIMITY_GESTURE_CM ? android.R.color.holo_blue_dark : android.R.color.darker_gray)));
                }
                proxPendingState = PROX_STATE_UNKNOWN;
                proxPendingSinceMs = 0;
            } else {
                if (proxPendingState != sampleState) {
                    proxPendingState = sampleState;
                    proxPendingSinceMs = now;
                } else {
                    if (now - proxPendingSinceMs >= PROX_DEBOUNCE_MS) {
                        proxDisplayedState = sampleState;
                        if (sampleState == PROX_STATE_FAR) {
                            tvProximityValue.setText(isBinary ? String.format(Locale.getDefault(), "Far (%.1f cm)", maxRange) : String.format(Locale.getDefault(), "Far (%.1f cm)", median));
                            ImageViewCompat.setImageTintList(ivProximityStatus, ColorStateList.valueOf(ContextCompat.getColor(requireContext(), android.R.color.darker_gray)));
                        } else if (sampleState == PROX_STATE_NEAR) {
                            tvProximityValue.setText(isBinary ? "Near" : String.format(Locale.getDefault(), "Near (%.1f cm)", median));
                            ImageViewCompat.setImageTintList(ivProximityStatus, ColorStateList.valueOf(ContextCompat.getColor(requireContext(), android.R.color.holo_blue_dark)));
                            if (alertsEnabled && envRepo != null) {
                                if (now - lastProxStateChangeMs > 500) {
                                    EnvironmentAlert alert = new EnvironmentAlert(now, "Proximity", isBinary ? "Near" : String.format(Locale.getDefault(), "Near (%.1f cm)", median), "WARNING", "Proximity detected: near (" + String.format(Locale.getDefault(), "%.1f cm", median) + ")");
                                    envRepo.insertAlert(alert);
                                    lastProxStateChangeMs = now;
                                    showSmsConfirmation(alert);
                                }
                            }
                        } else {
                            proxLastNumeric = median;
                            tvProximityValue.setText(String.format(Locale.getDefault(), "%.1f cm", proxLastNumeric));
                            ImageViewCompat.setImageTintList(ivProximityStatus, ColorStateList.valueOf(ContextCompat.getColor(requireContext(), proxLastNumeric <= EnvironmentMonitor.PROXIMITY_GESTURE_CM ? android.R.color.holo_blue_dark : android.R.color.darker_gray)));
                        }

                        proxPendingState = PROX_STATE_UNKNOWN;
                        proxPendingSinceMs = 0;
                    }
                }
            }
            updateTimestamp();
        });
    }

    @Override
    public void onSensorUnavailable(String sensorName) {
        if (getActivity() == null) return;
        requireActivity().runOnUiThread(() -> {
            if ("Proximity".equals(sensorName)) {
                tvProximityValue.setText(getString(R.string.unavailable));
                ImageViewCompat.setImageTintList(ivProximityStatus, ColorStateList.valueOf(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark)));
                updateTimestamp();
                return;
            }

            String msg = sensorName + " sensor unavailable on this device";
            AlertSender.sendToast(requireContext(), msg);
            AlertSender.sendNotification(requireContext(), "Sensor unavailable", msg);

            if ("Temperature".equals(sensorName)) {
                tvTemperatureValue.setText(getString(R.string.unavailable));
                ImageViewCompat.setImageTintList(ivTemperatureStatus, ColorStateList.valueOf(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark)));
            }
            if ("Light".equals(sensorName)) {
                tvLightValue.setText(getString(R.string.unavailable));
                ImageViewCompat.setImageTintList(ivLightStatus, ColorStateList.valueOf(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark)));
            }

            updateTimestamp();
        });
    }

    // Show a cancelable Snackbar for 5s; if not canceled, request SMS permission (if needed) and send SMS (optionally with location)
    private void showSmsConfirmation(EnvironmentAlert alert) {
        if (!isAdded() || getView() == null) return;
        smsCanceled = false;
        pendingSmsAlert = alert;

        Snackbar snackbar = Snackbar.make(getView(), getString(R.string.sms_snackbar_text), Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction(getString(R.string.cancel), v -> {
            smsCanceled = true;
            if (smsRunnable != null) smsHandler.removeCallbacks(smsRunnable);
            pendingSmsAlert = null;
            snackbar.dismiss();
        });
        snackbar.show();

        smsRunnable = () -> {
            snackbar.dismiss();
            if (smsCanceled || pendingSmsAlert == null) return;
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
                String loc = tryGetLastKnownLocation();
                SmsAlertSender.sendNow(getActivity(), pendingSmsAlert, loc);
                pendingSmsAlert = null;
            } else {
                requestPermissions(new String[]{Manifest.permission.SEND_SMS, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQ_SMS);
            }
        };
        smsHandler.postDelayed(smsRunnable, 5000);
    }

    private String tryGetLastKnownLocation() {
        try {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return null;
            }
            LocationManager lm = (LocationManager) requireContext().getSystemService(Context.LOCATION_SERVICE);
            if (lm == null) return null;
            Location loc = null;
            try { loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER); } catch (Exception ignored) {}
            if (loc == null) try { loc = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER); } catch (Exception ignored) {}
            if (loc == null) return null;
            return String.format(Locale.getDefault(), "%.6f,%.6f", loc.getLatitude(), loc.getLongitude());
        } catch (Exception ex) {
            return null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != REQ_SMS) return;
        if (pendingSmsAlert == null || smsCanceled) { pendingSmsAlert = null; return; }

        boolean smsGranted = false;
        boolean locGranted = false;
        for (int i = 0; i < permissions.length; i++) {
            String p = permissions[i];
            if (Manifest.permission.SEND_SMS.equals(p)) smsGranted = (i < grantResults.length && grantResults[i] == PackageManager.PERMISSION_GRANTED);
            if (Manifest.permission.ACCESS_FINE_LOCATION.equals(p) || Manifest.permission.ACCESS_COARSE_LOCATION.equals(p)) locGranted = (i < grantResults.length && grantResults[i] == PackageManager.PERMISSION_GRANTED) || locGranted;
        }

        if (smsGranted) {
            String loc = locGranted ? tryGetLastKnownLocation() : null;
            SmsAlertSender.sendNow(getActivity(), pendingSmsAlert, loc);
        } else {
            SmsAlertSender.sendNow(getActivity(), pendingSmsAlert, null);
        }
        pendingSmsAlert = null;
    }
}
