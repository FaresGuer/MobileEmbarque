package com.example.projet.Fragments.Environment;

import android.content.Context;
import android.os.Bundle;
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
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;
import android.content.res.ColorStateList;

import com.example.projet.R;
import com.example.projet.Sensors.EnvironmentMonitor;
import com.example.projet.Utils.AlertSender;
import com.example.projet.Repositories.EnvironmentRepository;
import com.example.projet.Entities.EnvironmentAlert;
import com.example.projet.Fragments.Environment.AlertsHistoryFragment;

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
    // Proximity stabilization state
    private static final int PROX_STATE_UNKNOWN = 0;
    private static final int PROX_STATE_NEAR = 1;
    private static final int PROX_STATE_FAR = 2;
    private static final int PROX_STATE_NUMERIC = 3;

    private int proxDisplayedState = PROX_STATE_UNKNOWN;
    private int proxPendingState = PROX_STATE_UNKNOWN;
    // time-based debounce: require majority to persist for this many ms before committing
    private static final long PROX_DEBOUNCE_MS = 600;
    private long proxPendingSinceMs = 0;
    private float proxLastNumeric = Float.NaN;
    private long lastProxStateChangeMs = 0;
    // Sliding-window majority filter
    private static final int PROX_WINDOW = 5;
    // raw sample circular buffer for median filter
    private final float[] proxRaw = new float[PROX_WINDOW];
    private int proxRawIndex = 0;
    private int proxRawFilled = 0;

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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_environment, container, false);
        ImageButton btnOpenMenu = view.findViewById(R.id.btnOpenMenu);
        btnOpenMenu.setOnClickListener(v -> {
            if (menuListener != null) menuListener.onOpenMenu();
        });

        // bind views
        tvTemperatureValue = view.findViewById(R.id.tvTemperatureValue);
        tvLightValue = view.findViewById(R.id.tvLightValue);
        tvProximityValue = view.findViewById(R.id.tvProximityValue);
        tvLastUpdated = view.findViewById(R.id.tvLastUpdated);

        ivTemperatureStatus = view.findViewById(R.id.ivTemperatureStatus);
        ivLightStatus = view.findViewById(R.id.ivLightStatus);
        ivProximityStatus = view.findViewById(R.id.ivProximityStatus);

        switchEnableAlerts = view.findViewById(R.id.switchEnableAlerts);
        switchEnableAlerts.setOnCheckedChangeListener((buttonView, isChecked) -> alertsEnabled = isChecked);

        monitor = new EnvironmentMonitor(requireContext());
        monitor.setListener(this);
        // initialize repository before starting monitor to avoid races where a proximity event
        // arrives before envRepo is set and causes a NullPointerException.
        envRepo = new EnvironmentRepository(requireContext());
        // start monitor now so proximity/other events are received
        monitor.start();

        Button btnShowAlerts = view.findViewById(R.id.btnShowAlerts);
        btnShowAlerts.setOnClickListener(v -> {
            // open alerts history fragment
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new AlertsHistoryFragment())
                        .addToBackStack(null)
                        .commit();
            }
        });

        // Proximity initial state: if sensor exists show waiting message, otherwise show '--'
        proximityAvailable = monitor.hasProximity();
        proximityReceived = false;
        if (proximityAvailable) {
            // show an immediate initial value (assume 'Far' = maxRange) so UI is responsive even before events
            float maxRange = monitor.getProximityMaxRange();
            if (!Float.isNaN(maxRange)) {
                tvProximityValue.setText(String.format(Locale.getDefault(), "Far (%.1f cm)", maxRange));
                // pre-fill the proximity samples buffer so the majority filter immediately recognizes FAR
                for (int i = 0; i < PROX_WINDOW; i++) proxRaw[i] = maxRange;
                proxRawFilled = PROX_WINDOW;
                proxRawIndex = 0;
                tvProximityValue.setText(String.format(Locale.getDefault(), "Far (%.1f cm)", maxRange));
            } else {
                tvProximityValue.setText("--");
            }
            ImageViewCompat.setImageTintList(ivProximityStatus, ColorStateList.valueOf(ContextCompat.getColor(requireContext(), android.R.color.darker_gray)));
            proxDisplayedState = !Float.isNaN(maxRange) ? PROX_STATE_FAR : PROX_STATE_UNKNOWN;
             // if no reading arrives within 6s, mark as 'No reading' (some devices are slower)
             view.postDelayed(() -> {
                 if (isAdded() && proximityAvailable && !proximityReceived) {
                     tvProximityValue.setText("No reading");
                 }
             }, 6000);
         } else {
             tvProximityValue.setText("--");
             ImageViewCompat.setImageTintList(ivProximityStatus, ColorStateList.valueOf(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark)));
         }

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

    private void updateTimestamp() {
        String ts = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
        tvLastUpdated.setText("Last updated: " + ts);
    }

    // EnvironmentMonitor.Listener
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
                    // save alert
                    EnvironmentAlert alert = new EnvironmentAlert(System.currentTimeMillis(), "Temperature",
                            String.format(Locale.getDefault(), "%.1f °C", celsius), "HIGH",
                            "High temperature detected: " + String.format(Locale.getDefault(), "%.1f °C", celsius));
                    envRepo.insertAlert(alert);
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
            // Update UI value for proximity but DO NOT send notifications or toasts for proximity events
            // validate the value
            if (cm == null || Float.isNaN(cm) || Float.isInfinite(cm)) {
                // invalid reading -> show placeholder
                tvProximityValue.setText("--");
                ImageViewCompat.setImageTintList(ivProximityStatus, ColorStateList.valueOf(ContextCompat.getColor(requireContext(), android.R.color.darker_gray)));
                // reset stabilization
                proxPendingState = PROX_STATE_UNKNOWN;
                return;
            }

            // push raw sample into circular buffer
            proxRaw[proxRawIndex] = cm;
            proxRawIndex = (proxRawIndex + 1) % PROX_WINDOW;
            if (proxRawFilled < PROX_WINDOW) proxRawFilled++;

            // Compute median safely of the filled samples
            int n = Math.max(1, proxRawFilled);
            float[] tmp = new float[n];
            System.arraycopy(proxRaw, 0, tmp, 0, n);
            java.util.Arrays.sort(tmp);
            float median = tmp[n / 2];

            // Interpret the median value
            float maxRange = monitor != null ? monitor.getProximityMaxRange() : Float.NaN;
            float eps = !Float.isNaN(maxRange) ? Math.max(0.1f, maxRange * 0.05f) : 0.5f;

            // Detect if sensor behaves as a binary proximity (common on phones: values are 0 or maxRange)
            boolean isBinary = !Float.isNaN(maxRange) && maxRange <= 10.0f;

            // Compute sample state for this median
            int sampleState;
            if (!Float.isNaN(maxRange) && Math.abs(median - maxRange) <= eps) sampleState = PROX_STATE_FAR;
            else if (median <= EnvironmentMonitor.PROXIMITY_GESTURE_CM) sampleState = PROX_STATE_NEAR;
            else sampleState = PROX_STATE_NUMERIC;

            long now = System.currentTimeMillis();
            // If sample state matches displayed state, commit immediate UI update and clear pending
            if (sampleState == proxDisplayedState) {
                // Update UI according to displayed state
                if (sampleState == PROX_STATE_FAR) {
                    if (isBinary) tvProximityValue.setText(String.format(Locale.getDefault(), "Far (%.1f cm)", maxRange));
                    else tvProximityValue.setText(String.format(Locale.getDefault(), "Far (%.1f cm)", median));
                    ImageViewCompat.setImageTintList(ivProximityStatus, ColorStateList.valueOf(ContextCompat.getColor(requireContext(), android.R.color.darker_gray)));
                } else if (sampleState == PROX_STATE_NEAR) {
                    tvProximityValue.setText(isBinary ? "Near" : String.format(Locale.getDefault(), "Near (%.1f cm)", median));
                    ImageViewCompat.setImageTintList(ivProximityStatus, ColorStateList.valueOf(ContextCompat.getColor(requireContext(), android.R.color.holo_blue_dark)));
                } else {
                    proxLastNumeric = median;
                    tvProximityValue.setText(String.format(Locale.getDefault(), "%.1f cm", proxLastNumeric));
                    ImageViewCompat.setImageTintList(ivProximityStatus, ColorStateList.valueOf(ContextCompat.getColor(requireContext(), proxLastNumeric <= EnvironmentMonitor.PROXIMITY_GESTURE_CM
                            ? android.R.color.holo_blue_dark
                            : android.R.color.darker_gray)));
                }
                // clear pending
                proxPendingState = PROX_STATE_UNKNOWN;
                proxPendingSinceMs = 0;
            } else {
                // new sampleState differs from displayed -> start/continue pending timer
                if (proxPendingState != sampleState) {
                    proxPendingState = sampleState;
                    proxPendingSinceMs = now;
                } else {
                    // same pending state; commit if persisted long enough
                    if (now - proxPendingSinceMs >= PROX_DEBOUNCE_MS) {
                        // commit state change
                        proxDisplayedState = sampleState;
                        // update UI and optionally record alert
                        if (sampleState == PROX_STATE_FAR) {
                            if (isBinary) tvProximityValue.setText(String.format(Locale.getDefault(), "Far (%.1f cm)", maxRange));
                            else tvProximityValue.setText(String.format(Locale.getDefault(), "Far (%.1f cm)", median));
                            ImageViewCompat.setImageTintList(ivProximityStatus, ColorStateList.valueOf(ContextCompat.getColor(requireContext(), android.R.color.darker_gray)));
                        } else if (sampleState == PROX_STATE_NEAR) {
                            tvProximityValue.setText(isBinary ? "Near" : String.format(Locale.getDefault(), "Near (%.1f cm)", median));
                            ImageViewCompat.setImageTintList(ivProximityStatus, ColorStateList.valueOf(ContextCompat.getColor(requireContext(), android.R.color.holo_blue_dark)));
                            if (alertsEnabled && envRepo != null) {
                                // avoid duplicate alerts: require at least 500ms since last change
                                if (now - lastProxStateChangeMs > 500) {
                                    EnvironmentAlert alert = new EnvironmentAlert(now, "Proximity",
                                            isBinary ? "Near" : String.format(Locale.getDefault(), "Near (%.1f cm)", median), "WARNING",
                                            "Proximity detected: near (" + String.format(Locale.getDefault(), "%.1f cm", median) + ")");
                                    envRepo.insertAlert(alert);
                                    lastProxStateChangeMs = now;
                                }
                            }
                        } else {
                            proxLastNumeric = median;
                            tvProximityValue.setText(String.format(Locale.getDefault(), "%.1f cm", proxLastNumeric));
                            ImageViewCompat.setImageTintList(ivProximityStatus, ColorStateList.valueOf(ContextCompat.getColor(requireContext(), proxLastNumeric <= EnvironmentMonitor.PROXIMITY_GESTURE_CM
                                    ? android.R.color.holo_blue_dark
                                    : android.R.color.darker_gray)));
                        }
                        // clear pending after commit
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
            // For proximity we only update the UI to show a placeholder and DO NOT send alerts
            if ("Proximity".equals(sensorName)) {
                tvProximityValue.setText("Unavailable");
                ImageViewCompat.setImageTintList(ivProximityStatus, ColorStateList.valueOf(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark)));
                updateTimestamp();
                return;
            }

            String msg = sensorName + " sensor unavailable on this device";
            // user-visible alerts for non-proximity sensors
            AlertSender.sendToast(requireContext(), msg);
            AlertSender.sendNotification(requireContext(), "Sensor unavailable", msg);

            // reflect in UI and mark status icon
            if ("Temperature".equals(sensorName)) {
                tvTemperatureValue.setText("Unavailable");
                ImageViewCompat.setImageTintList(ivTemperatureStatus, ColorStateList.valueOf(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark)));
            }
            if ("Light".equals(sensorName)) {
                tvLightValue.setText("Unavailable");
                ImageViewCompat.setImageTintList(ivLightStatus, ColorStateList.valueOf(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark)));
            }

            updateTimestamp();
        });
    }
}
