package com.example.projet.Utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.os.Vibrator;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

public class AlertSender {
    private static final String CHANNEL_ID = "env_alerts";
    private static final int NOTIF_ID = 1001;

    public static void initChannel(Context ctx) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Environment Alerts", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Notifications for environmental hazards");
            NotificationManager nm = ctx.getSystemService(NotificationManager.class);
            if (nm != null) nm.createNotificationChannel(channel);
        }
    }

    public static void sendToast(Context ctx, String msg) {
        Toast.makeText(ctx, msg, Toast.LENGTH_LONG).show();
    }

    public static void vibrate(Context ctx, long ms) {
        try {
            if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.VIBRATE) != PackageManager.PERMISSION_GRANTED) {
                // Permission not granted (rare for VIBRATE), skip
                return;
            }
            Vibrator v = (Vibrator) ctx.getSystemService(Context.VIBRATOR_SERVICE);
            if (v != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    v.vibrate(android.os.VibrationEffect.createOneShot(ms, android.os.VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    //noinspection deprecation
                    v.vibrate(ms);
                }
            }
        } catch (SecurityException se) {
            // ignore if permission rejected
        } catch (Exception ignored) {}
    }

    public static void sendNotification(Context ctx, String title, String body) {
        initChannel(ctx);
        // On Android 13+ need POST_NOTIFICATIONS runtime permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                // Can't post notification - fallback to toast
                sendToast(ctx, title + ": " + body);
                return;
            }
        }
        NotificationCompat.Builder b = new NotificationCompat.Builder(ctx, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle(title)
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);
        try {
            NotificationManagerCompat.from(ctx).notify(NOTIF_ID, b.build());
        } catch (SecurityException se) {
            // permission may have been revoked; fallback to toast
            sendToast(ctx, title + ": " + body);
        }
    }
}
