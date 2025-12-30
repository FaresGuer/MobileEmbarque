package com.example.projet.Utils;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.telephony.SmsManager;
import android.util.Log;
import androidx.core.content.ContextCompat;
import android.content.pm.PackageManager;

import com.example.projet.DataBase.AppDatabase;
import com.example.projet.DataBase.UserSession;
import com.example.projet.Entities.EnvironmentAlert;
import com.example.projet.Entities.EmergencyContact;
import com.example.projet.Entities.User;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Helper to confirm and send SMS alerts to the user's emergency contacts.
 * Shows a confirmation dialog or (preferably) a Snackbar in the fragment. If sending is
 * allowed the helper will attempt to send programmatically; otherwise it opens the SMS composer.
 */
public class SmsAlertSender {
    private static final String TAG = "SmsAlertSender";

    public static void confirmAndSend(Activity activity, EnvironmentAlert alert) {
        // simple fallback dialog (kept for compatibility)
        if (activity == null || alert == null) return;
        AlertDialog.Builder b = new AlertDialog.Builder(activity);
        b.setTitle("Send SMS alert");
        b.setMessage("This will send SMS to your emergency contacts.");
        b.setPositiveButton("Send", (d, w) -> sendNow(activity, alert, null));
        b.setNegativeButton("Cancel", (d, w) -> d.dismiss());
        b.show();
    }

    // Public programmatic send entrypoint (location may be null). Runs sending on a background thread.
    public static void sendNow(Activity activity, EnvironmentAlert alert, String location) {
        if (activity == null || alert == null) return;
        Context ctx = activity.getApplicationContext();
        User me = UserSession.getUser();
        if (me == null) return;
        String message = buildMessage(alert, me, location);
        new Thread(() -> sendSmsToEmergencyContacts(ctx, me.getId(), message, activity)).start();
    }

    private static String buildMessage(EnvironmentAlert alert, User me, String location) {
        String base = String.format(Locale.getDefault(), "%s — %s (%s)", alert.getSensor(), alert.getMessage(), alert.getValue());
        String full = String.format(Locale.getDefault(), "Alert from %s: %s", me.getUsername(), base);
        if (location != null && !location.trim().isEmpty()) {
            full = full + "; Location: " + location;
        }
        return full;
    }

    // Send to emergency contacts for the given owner user id (deduplicated list of phone numbers)
    private static void sendSmsToEmergencyContacts(Context ctx, int ownerUserId, String message, Activity activity) {
        AppDatabase db = AppDatabase.getInstance(ctx);
        List<EmergencyContact> ecs;
        try {
            ecs = db.emergencyContactDao().getForUser(ownerUserId);
        } catch (Exception ex) {
            Log.w(TAG, "Failed to load emergency contacts", ex);
            return;
        }

        if (ecs == null || ecs.isEmpty()) {
            Log.i(TAG, "No emergency contacts to send SMS to");
            return;
        }

        Set<String> phones = new HashSet<>();
        for (EmergencyContact ec : ecs) {
            if (ec == null) continue;
            String phone = ec.phoneNumber;
            if (phone == null) continue;
            phone = phone.trim();
            if (phone.isEmpty()) continue;
            phones.add(phone);
            Log.d(TAG, "Candidate emergency phone id=" + ec.id + " -> " + phone);
        }

        if (phones.isEmpty()) {
            Log.i(TAG, "No valid phone numbers in emergency contacts");
            return;
        }

        StringBuilder recipients = new StringBuilder();
        int idx = 0;
        for (String p : phones) {
            if (idx++ > 0) recipients.append(';');
            recipients.append(p);
        }

        boolean hasSmsPermission = ContextCompat.checkSelfPermission(ctx, android.Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED;
        if (hasSmsPermission) {
            SmsManager smsManager = SmsManager.getDefault();
            String[] nums = recipients.toString().split(";");
            for (String phone : nums) {
                try {
                    smsManager.sendTextMessage(phone, null, message, null, null);
                    Log.i(TAG, "SMS sent to " + phone);
                } catch (Exception ex) {
                    Log.w(TAG, "Failed to send SMS to " + phone + ", opening composer", ex);
                    if (activity != null) {
                        activity.runOnUiThread(() -> openSmsComposer(activity, phone, message));
                    }
                }
            }
        } else {
            if (activity != null) {
                activity.runOnUiThread(() -> openSmsComposer(activity, recipients.toString(), message));
            }
        }
    }

    private static void openSmsComposer(Activity activity, String phone, String message) {
        try {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("smsto:" + Uri.encode(phone)));
            intent.putExtra("sms_body", message);
            activity.startActivity(intent);
        } catch (Exception ex) {
            Log.w(TAG, "Failed to open SMS composer", ex);
        }
    }
    public static void sendMessageToPrimary(Activity activity, int ownerUserId, String message) {
        if (activity == null || message == null || message.trim().isEmpty()) return;

        Context ctx = activity.getApplicationContext();
        new Thread(() -> sendSmsToPrimaryContact(ctx, ownerUserId, message, activity)).start();
    }

    private static void sendSmsToPrimaryContact(Context ctx, int ownerUserId, String message, Activity activity) {
        AppDatabase db = AppDatabase.getInstance(ctx);
        EmergencyContact primary;
        try {
            primary = db.emergencyContactDao().getPrimaryForUser(ownerUserId);
        } catch (Exception ex) {
            Log.w(TAG, "Failed to load primary emergency contact", ex);
            return;
        }

        if (primary == null || primary.phoneNumber == null || primary.phoneNumber.trim().isEmpty()) {
            Log.i(TAG, "No primary emergency contact found");
            return;
        }

        String phone = primary.phoneNumber.trim();

        boolean canSend = ContextCompat.checkSelfPermission(ctx, Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED;

        if (canSend) {
            try {
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(phone, null, message, null, null);
                Log.i(TAG, "SMS sent to primary: " + phone);
            } catch (Exception ex) {
                Log.w(TAG, "Failed to send SMS to primary, opening composer", ex);
                if (activity != null) {
                    activity.runOnUiThread(() -> openSmsComposer(activity, phone, message));
                }
            }
        } else {
            if (activity != null) {
                activity.runOnUiThread(() -> openSmsComposer(activity, phone, message));
            }
        }
    }
}
