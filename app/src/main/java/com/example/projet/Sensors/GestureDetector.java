package com.example.projet.Sensors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import androidx.annotation.MainThread;

/**
 * Module 4: Gesture Control & Safety Interaction Module
 * Detects gestures using Gyroscope and Proximity sensors
 * 
 * Features:
 * - Double shake → triggers SOS alert
 * - Hand wave (proximity) → toggles flashlight/alarm
 * - Double tilt → sends GPS coordinates
 */
public class GestureDetector implements SensorEventListener {
    
    public interface GestureListener {
        void onDoubleShakeDetected(); // SOS Alert
        void onHandWaveDetected(); // Toggle Flashlight/Alarm
        void onDoubleTiltDetected(); // Send GPS Coordinates
    }

    private final SensorManager sensorManager;
    private final Sensor gyroscopeSensor;
    private final Sensor accelerometerSensor; // Use accelerometer for shake (more reliable)
    private final Sensor proximitySensor;
    private GestureListener listener;
    private final Context context;

    // Shake detection variables (using accelerometer)
    private static final float SHAKE_THRESHOLD = 12.0f; // Accelerometer threshold for shake (m/s²)
    private static final long SHAKE_TIME_WINDOW = 800; // Time window for double shake (ms) - increased
    private long lastShakeTime = 0;
    private int shakeCount = 0;
    private float[] lastAccelValues = new float[3];

    // Tilt detection variables
    private static final float TILT_THRESHOLD = 5.0f; // Gyroscope threshold for tilt (lowered for easier detection)
    private static final long TILT_TIME_WINDOW = 1200; // Time window for double tilt (ms) - increased
    private long lastTiltTime = 0;
    private int tiltCount = 0;
    private float[] lastGyroValues = new float[3];

    // Proximity wave detection variables
    private static final float PROXIMITY_WAVE_THRESHOLD = 2.0f; // cm - close enough for wave
    private static final long PROXIMITY_WAVE_TIME = 300; // Time window for wave gesture (ms)
    private long lastProximityCloseTime = 0;
    private boolean wasClose = false;
    private float proximityMaxRange = 5.0f; // Default, will be updated from sensor

    // Noise filtering
    private static final float NOISE_THRESHOLD = 0.5f; // Ignore small movements

    public GestureDetector(Context ctx) {
        this.context = ctx.getApplicationContext();
        sensorManager = (SensorManager) ctx.getSystemService(Context.SENSOR_SERVICE);
        gyroscopeSensor = sensorManager != null ? sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) : null;
        accelerometerSensor = sensorManager != null ? sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) : null;
        proximitySensor = sensorManager != null ? sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY) : null;
        
        if (proximitySensor != null) {
            proximityMaxRange = proximitySensor.getMaximumRange();
        }
    }

    public void setListener(GestureListener l) {
        listener = l;
    }

    /**
     * Returns true if gyroscope sensor is available
     */
    public boolean hasGyroscope() {
        return gyroscopeSensor != null;
    }

    /**
     * Returns true if accelerometer sensor is available
     */
    public boolean hasAccelerometer() {
        return accelerometerSensor != null;
    }

    /**
     * Returns true if proximity sensor is available
     */
    public boolean hasProximity() {
        return proximitySensor != null;
    }

    @MainThread
    public void start() {
        if (sensorManager == null) {
            Log.e("GestureDetector", "SensorManager is null");
            return;
        }

        // Register accelerometer for shake detection (more reliable)
        if (accelerometerSensor != null) {
            sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_UI);
            Log.d("GestureDetector", "Accelerometer sensor registered for shake detection");
        } else {
            Log.w("GestureDetector", "Accelerometer sensor not available - shake detection may not work");
        }

        // Register gyroscope for tilt detection
        if (gyroscopeSensor != null) {
            sensorManager.registerListener(this, gyroscopeSensor, SensorManager.SENSOR_DELAY_UI);
            Log.d("GestureDetector", "Gyroscope sensor registered for tilt detection");
        } else {
            Log.w("GestureDetector", "Gyroscope sensor not available - tilt detection may not work");
        }

        if (proximitySensor != null) {
            sensorManager.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_UI);
            proximityMaxRange = proximitySensor.getMaximumRange();
            Log.d("GestureDetector", "Proximity sensor registered, maxRange=" + proximityMaxRange);
        } else {
            Log.w("GestureDetector", "Proximity sensor not available");
        }
    }

    @MainThread
    public void stop() {
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
            Log.d("GestureDetector", "Sensors unregistered");
        }
        // Reset counters
        shakeCount = 0;
        tiltCount = 0;
        lastShakeTime = 0;
        lastTiltTime = 0;
        wasClose = false;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (listener == null) return;

        int type = event.sensor.getType();

        if (type == Sensor.TYPE_ACCELEROMETER) {
            handleAccelerometerEvent(event); // Shake detection
        } else if (type == Sensor.TYPE_GYROSCOPE) {
            handleGyroscopeEvent(event); // Tilt detection
        } else if (type == Sensor.TYPE_PROXIMITY) {
            handleProximityEvent(event);
        }
    }

    private void handleAccelerometerEvent(SensorEvent event) {
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];
        
        // Calculate change from last reading
        if (lastAccelValues[0] != 0 || lastAccelValues[1] != 0 || lastAccelValues[2] != 0) {
            float deltaX = Math.abs(x - lastAccelValues[0]);
            float deltaY = Math.abs(y - lastAccelValues[1]);
            float deltaZ = Math.abs(z - lastAccelValues[2]);
            
            // Calculate total change (delta)
            float delta = (float) Math.sqrt(deltaX*deltaX + deltaY*deltaY + deltaZ*deltaZ);
            
            long currentTime = System.currentTimeMillis();
            
            // Detect shake (sudden change in acceleration)
            // Lower threshold for easier detection
            if (delta > SHAKE_THRESHOLD) {
                Log.d("GestureDetector", "Shake detected! Delta: " + String.format("%.2f", delta) + " Threshold: " + SHAKE_THRESHOLD);
                
                if (currentTime - lastShakeTime < SHAKE_TIME_WINDOW) {
                    shakeCount++;
                    Log.d("GestureDetector", "Shake count: " + shakeCount + " (within " + (currentTime - lastShakeTime) + "ms)");
                    if (shakeCount >= 2) {
                        // Double shake detected!
                        Log.d("GestureDetector", "*** DOUBLE SHAKE DETECTED! ***");
                        if (listener != null) {
                            listener.onDoubleShakeDetected();
                        }
                        shakeCount = 0;
                        lastShakeTime = 0;
                    }
                } else {
                    // New shake sequence
                    shakeCount = 1;
                    lastShakeTime = currentTime;
                    Log.d("GestureDetector", "New shake sequence started (time since last: " + (currentTime - lastShakeTime) + "ms)");
                }
            }
        }

        // Update last values
        lastAccelValues[0] = x;
        lastAccelValues[1] = y;
        lastAccelValues[2] = z;
    }

    private void handleGyroscopeEvent(SensorEvent event) {
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        long currentTime = System.currentTimeMillis();

        // Detect tilt (significant change in orientation)
        // Compare with previous values to detect rotation changes
        if (lastGyroValues[0] != 0 || lastGyroValues[1] != 0 || lastGyroValues[2] != 0) {
            float deltaX = Math.abs(x - lastGyroValues[0]);
            float deltaY = Math.abs(y - lastGyroValues[1]);
            float deltaZ = Math.abs(z - lastGyroValues[2]);
            
            // Calculate total change (delta) in angular velocity
            float delta = (float) Math.sqrt(deltaX*deltaX + deltaY*deltaY + deltaZ*deltaZ);
            
            // Also check magnitude of current angular velocity (for tilt detection)
            float magnitude = (float) Math.sqrt(x * x + y * y + z * z);
            
            // Filter noise - ignore very small movements
            if (delta < NOISE_THRESHOLD && magnitude < NOISE_THRESHOLD) {
                // Update last values even if we ignore this reading
                lastGyroValues[0] = x;
                lastGyroValues[1] = y;
                lastGyroValues[2] = z;
                return;
            }
            
            // Detect tilt: either significant change in angular velocity OR high current angular velocity
            // This detects when phone is rotated quickly (tilted)
            boolean isTilt = (delta > TILT_THRESHOLD) || (magnitude > TILT_THRESHOLD);
            
            if (isTilt) {
                Log.d("GestureDetector", "Tilt detected! Delta: " + String.format("%.2f", delta) + 
                      ", Magnitude: " + String.format("%.2f", magnitude) + 
                      ", Threshold: " + TILT_THRESHOLD);
                
                if (currentTime - lastTiltTime < TILT_TIME_WINDOW) {
                    tiltCount++;
                    Log.d("GestureDetector", "Tilt count: " + tiltCount + " (within " + (currentTime - lastTiltTime) + "ms)");
                    if (tiltCount >= 2) {
                        // Double tilt detected!
                        Log.d("GestureDetector", "*** DOUBLE TILT DETECTED! ***");
                        if (listener != null) {
                            listener.onDoubleTiltDetected();
                        }
                        tiltCount = 0;
                        lastTiltTime = 0;
                    }
                } else {
                    // New tilt sequence
                    tiltCount = 1;
                    lastTiltTime = currentTime;
                    Log.d("GestureDetector", "New tilt sequence started (time since last: " + (currentTime - lastTiltTime) + "ms)");
                }
            }
        }

        // Update last values
        lastGyroValues[0] = x;
        lastGyroValues[1] = y;
        lastGyroValues[2] = z;
    }

    private void handleProximityEvent(SensorEvent event) {
        float distance = event.values[0];
        long currentTime = System.currentTimeMillis();

        // Check if object is close (hand wave)
        // For binary sensors, distance is usually 0 (close) or maxRange (far)
        boolean isClose = false;
        if (proximityMaxRange <= 5.0f) {
            // Binary sensor: 0 = close, maxRange = far
            isClose = (distance < proximityMaxRange * 0.5f);
        } else {
            // Analog sensor: check if within threshold
            isClose = (distance < PROXIMITY_WAVE_THRESHOLD);
        }

        if (isClose && !wasClose) {
            // Hand just got close
            wasClose = true;
            lastProximityCloseTime = currentTime;
        } else if (!isClose && wasClose) {
            // Hand moved away - check if it was a quick wave
            if (currentTime - lastProximityCloseTime < PROXIMITY_WAVE_TIME) {
                // Quick wave detected!
                Log.d("GestureDetector", "Hand wave detected!");
                listener.onHandWaveDetected();
            }
            wasClose = false;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Handle accuracy changes if needed
        if (accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) {
            Log.w("GestureDetector", "Sensor accuracy unreliable: " + sensor.getName());
        }
    }
}

