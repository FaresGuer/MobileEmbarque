package com.example.projet.Entities.SafetyModule;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.example.projet.Entities.SafetyModule.Interfaces.MotionSource;

public class PhoneMotionSource implements MotionSource, SensorEventListener {

    private final SensorManager sm;
    private final Sensor accel;
    private final Sensor gyro;

    private boolean running = false;
    private Callback cb;

    private float lastGx = 0f, lastGy = 0f, lastGz = 0f;
    private long lastGyroMs = 0;

    public PhoneMotionSource(Context ctx, boolean useGyro) {
        sm = (SensorManager) ctx.getSystemService(Context.SENSOR_SERVICE);
        accel = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyro = useGyro ? sm.getDefaultSensor(Sensor.TYPE_GYROSCOPE) : null;
    }

    @Override
    public void start(Callback cb) {
        if (running) return;
        if (accel == null) {
            if (cb != null) cb.onError("Accelerometer not available on this device.");
            return;
        }
        this.cb = cb;
        running = true;

        sm.registerListener(this, accel, SensorManager.SENSOR_DELAY_GAME);
        if (gyro != null) sm.registerListener(this, gyro, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    public void stop() {
        if (!running) return;
        running = false;
        sm.unregisterListener(this);
        cb = null;
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (!running || cb == null) return;

        long now = System.currentTimeMillis();

        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            lastGx = event.values[0];
            lastGy = event.values[1];
            lastGz = event.values[2];
            lastGyroMs = now;
            return;
        }

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float ax = event.values[0];
            float ay = event.values[1];
            float az = event.values[2];

            float gx = 0f, gy = 0f, gz = 0f;
            if (gyro != null && (now - lastGyroMs) < 250) {
                gx = lastGx;
                gy = lastGy;
                gz = lastGz;
            }

            cb.onSample(new MotionSample(now, ax, ay, az, gx, gy, gz));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}
