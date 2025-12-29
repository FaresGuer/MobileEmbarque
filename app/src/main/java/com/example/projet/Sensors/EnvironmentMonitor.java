package com.example.projet.Sensors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.MainThread;

public class EnvironmentMonitor implements SensorEventListener {
    public interface Listener {
        void onTemperature(Float celsius);
        void onLight(Float lux);
        void onProximity(Float cm);
        void onSensorUnavailable(String sensorName);
    }

    private final SensorManager sensorManager;
    private final Sensor tempSensor;
    private final Sensor lightSensor;
    private final Sensor proximitySensor;
    private Listener listener;
    private final Context context;

    // Thresholds (defaults)
    public static final float TEMP_HIGH = 35.0f;
    public static final float TEMP_LOW = 0.0f;
    public static final float LIGHT_LOW = 10.0f;
    public static final float PROXIMITY_GESTURE_CM = 5.0f;

    private long lastUpdateMs = 0;
    private static final long UI_THROTTLE_MS = 800;

    public EnvironmentMonitor(Context ctx) {
        this.context = ctx.getApplicationContext();
        sensorManager = (SensorManager) ctx.getSystemService(Context.SENSOR_SERVICE);
        tempSensor = sensorManager != null ? sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE) : null;
        lightSensor = sensorManager != null ? sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT) : null;
        proximitySensor = sensorManager != null ? sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY) : null;
    }

    /**
     * Returns maximum range of the proximity sensor in the same units as events (usually cm), or Float.NaN if unknown.
     */
    public float getProximityMaxRange() {
        return proximitySensor != null ? proximitySensor.getMaximumRange() : Float.NaN;
    }

    public void setListener(Listener l) {
        listener = l;
    }

    /**
     * Returns true if a proximity sensor is available on this device.
     */
    public boolean hasProximity() {
        return proximitySensor != null;
    }

    @MainThread
    public void start() {
        if (sensorManager == null) return;
        Log.d("EnvMonitor", "start(): tempSensor=" + tempSensor + " lightSensor=" + lightSensor + " proximitySensor=" + proximitySensor);
        if (tempSensor != null) {
            sensorManager.registerListener(this, tempSensor, SensorManager.SENSOR_DELAY_NORMAL);
            Log.d("EnvMonitor", "registered temp sensor");
        } else if (listener != null) listener.onSensorUnavailable("Temperature");

        if (lightSensor != null) {
            sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
            Log.d("EnvMonitor", "registered light sensor");
        } else if (listener != null) listener.onSensorUnavailable("Light");

        // Use SENSOR_DELAY_NORMAL for proximity to maximize compatibility
        if (proximitySensor != null) {
            sensorManager.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL);
            Log.d("EnvMonitor", "registered proximity sensor, maxRange=" + proximitySensor.getMaximumRange());
            // NOTE: do NOT send a synthetic proximity reading here. Some devices treat proximity as binary
            // (0 or maxRange) and showing a synthetic value can be confusing. UI will remain in a waiting
            // state until a real event arrives. If sensor is missing, notify listener.
        } else if (listener != null) listener.onSensorUnavailable("Proximity");
    }

    @MainThread
    public void stop() {
        if (sensorManager != null) sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (listener == null) return;
        int type = event.sensor.getType();
        // Don't throttle proximity events (they can be binary and we want immediate feedback). Throttle other sensors.
        if (type != Sensor.TYPE_PROXIMITY) {
            long now = SystemClock.uptimeMillis();
            if (now - lastUpdateMs < UI_THROTTLE_MS) return; // throttle updates
            lastUpdateMs = now;
        }

        if (type == Sensor.TYPE_AMBIENT_TEMPERATURE) {
            float v = event.values[0];
            listener.onTemperature(v);
        } else if (type == Sensor.TYPE_LIGHT) {
            float v = event.values[0];
            listener.onLight(v);
        } else if (type == Sensor.TYPE_PROXIMITY) {
            float v = event.values[0];
            Log.d("EnvMonitor", "Proximity event value=" + v + " maxRange=" + (proximitySensor!=null?proximitySensor.getMaximumRange():"null"));
            listener.onProximity(v);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // no-op
    }
}
