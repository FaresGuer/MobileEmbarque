package com.example.projet.Services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.projet.R;
import com.example.projet.Sensors.GestureDetector;
import com.example.projet.Utils.FlashlightUtil;
import com.example.projet.Utils.SOSUtil;

/**
 * Background service for gesture detection
 * Works even when screen is locked
 */
public class GestureService extends Service implements GestureDetector.GestureListener {
    
    private static final String CHANNEL_ID = "gesture_service_channel";
    private static final int NOTIFICATION_ID = 2001;
    
    private GestureDetector gestureDetector;
    private boolean isRunning = false;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        gestureDetector = new GestureDetector(this);
        gestureDetector.setListener(this);
        FlashlightUtil.initialize(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!isRunning) {
            try {
                startForeground(NOTIFICATION_ID, createNotification());
                gestureDetector.start();
                isRunning = true;
                Log.d("GestureService", "Service started successfully");
            } catch (Exception e) {
                Log.e("GestureService", "Error starting service", e);
                isRunning = false;
                stopSelf();
            }
        }
        return START_STICKY; // Restart if killed
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (gestureDetector != null) {
            gestureDetector.stop();
        }
        FlashlightUtil.turnOff(this);
        isRunning = false;
        Log.d("GestureService", "Service stopped");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null; // Not a bound service
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Gesture Control Service",
                NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Background service for gesture detection");
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private Notification createNotification() {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Gesture Control Active")
            .setContentText("Shake, wave, or tilt to trigger actions")
            .setSmallIcon(android.R.drawable.ic_menu_recent_history)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build();
    }

    // GestureDetector.GestureListener implementation
    @Override
    public void onDoubleShakeDetected() {
        Log.d("GestureService", "*** DOUBLE SHAKE DETECTED - SENDING SOS ***");
        android.widget.Toast.makeText(this, "SOS Alert Triggered!", android.widget.Toast.LENGTH_SHORT).show();
        SOSUtil.sendSOSAlert(this);
    }

    @Override
    public void onHandWaveDetected() {
        Log.d("GestureService", "Hand wave detected - toggling flashlight");
        FlashlightUtil.toggleFlashlight(this);
    }

    @Override
    public void onDoubleTiltDetected() {
        Log.d("GestureService", "*** DOUBLE TILT DETECTED - SENDING GPS COORDINATES ***");
        android.widget.Toast.makeText(this, "GPS Coordinates Sent!", android.widget.Toast.LENGTH_SHORT).show();
        SOSUtil.sendGPSCoordinates(this);
    }
}

