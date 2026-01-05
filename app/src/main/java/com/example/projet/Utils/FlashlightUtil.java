package com.example.projet.Utils;

import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.example.projet.DataBase.AppDatabase;
import com.example.projet.DataBase.UserSession;
import com.example.projet.Entities.AlertHistory;
import com.example.projet.Entities.User;

/**
 * Utility class for controlling the device flashlight
 */
public class FlashlightUtil {
    
    private static boolean isFlashlightOn = false;
    private static CameraManager cameraManager;
    private static String cameraId;

    /**
     * Initialize flashlight (call this before using)
     */
    public static void initialize(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
            if (cameraManager != null) {
                try {
                    cameraId = cameraManager.getCameraIdList()[0];
                } catch (CameraAccessException e) {
                    android.util.Log.e("FlashlightUtil", "Error getting camera ID", e);
                    cameraId = null;
                }
            }
        }
    }

    /**
     * Toggle flashlight on/off
     */
    public static void toggleFlashlight(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            toggleFlashlightModern(context);
        } else {
            toggleFlashlightLegacy(context);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private static void toggleFlashlightModern(Context context) {
        if (cameraManager == null || cameraId == null) {
            initialize(context);
            if (cameraManager == null || cameraId == null) {
                Toast.makeText(context, "Flashlight not available", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        try {
            isFlashlightOn = !isFlashlightOn;
            cameraManager.setTorchMode(cameraId, isFlashlightOn);
            
            if (isFlashlightOn) {
                AlertSender.vibrate(context, 200);
                Toast.makeText(context, "Flashlight ON", Toast.LENGTH_SHORT).show();
                saveFlashlightAlert(context, true);
            } else {
                Toast.makeText(context, "Flashlight OFF", Toast.LENGTH_SHORT).show();
                saveFlashlightAlert(context, false);
            }
        } catch (CameraAccessException e) {
            android.util.Log.e("FlashlightUtil", "Error toggling flashlight", e);
            Toast.makeText(context, "Error controlling flashlight", Toast.LENGTH_SHORT).show();
            isFlashlightOn = false;
        }
    }

    @SuppressWarnings("deprecation")
    private static void toggleFlashlightLegacy(Context context) {
        android.hardware.Camera camera = null;
        try {
            camera = android.hardware.Camera.open();
            android.hardware.Camera.Parameters params = camera.getParameters();
            
            if (isFlashlightOn) {
                params.setFlashMode(android.hardware.Camera.Parameters.FLASH_MODE_OFF);
                isFlashlightOn = false;
                Toast.makeText(context, "Flashlight OFF", Toast.LENGTH_SHORT).show();
                saveFlashlightAlert(context, false);
            } else {
                params.setFlashMode(android.hardware.Camera.Parameters.FLASH_MODE_TORCH);
                isFlashlightOn = true;
                AlertSender.vibrate(context, 200);
                Toast.makeText(context, "Flashlight ON", Toast.LENGTH_SHORT).show();
                saveFlashlightAlert(context, true);
            }
            
            camera.setParameters(params);
        } catch (Exception e) {
            android.util.Log.e("FlashlightUtil", "Error toggling flashlight (legacy)", e);
            Toast.makeText(context, "Flashlight not available", Toast.LENGTH_SHORT).show();
        } finally {
            if (camera != null) {
                camera.release();
            }
        }
    }

    /**
     * Turn flashlight off (cleanup)
     */
    public static void turnOff(Context context) {
        if (isFlashlightOn) {
            toggleFlashlight(context);
        }
    }

    /**
     * Check if flashlight is currently on
     */
    public static boolean isFlashlightOn() {
        return isFlashlightOn;
    }

    /**
     * Save flashlight toggle to alert history
     */
    private static void saveFlashlightAlert(Context context, boolean turnedOn) {
        User user = UserSession.getUser();
        if (user == null) return;

        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(context);
            AlertHistory alert = new AlertHistory(
                System.currentTimeMillis(),
                "FLASHLIGHT",
                "Flashlight " + (turnedOn ? "Turned ON" : "Turned OFF"),
                "Flashlight was " + (turnedOn ? "activated" : "deactivated") + " via gesture control",
                "LOW",
                null,
                user.getId()
            );
            db.alertHistoryDao().insert(alert);
        }).start();
    }
}

