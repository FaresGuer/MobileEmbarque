package com.example.projet.Utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.telephony.SmsManager;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.example.projet.DataBase.AppDatabase;
import com.example.projet.DataBase.UserSession;
import com.example.projet.Entities.AlertHistory;
import com.example.projet.Entities.EmergencyContact;
import com.example.projet.Entities.User;

import java.util.List;
import java.util.Locale;

/**
 * Utility class for sending SOS alerts and GPS coordinates to emergency contacts
 */
public class SOSUtil {
    
    private static final String SOS_MESSAGE_TEMPLATE = 
        "🚨 SOS ALERT 🚨\n" +
        "User: %s\n" +
        "Location: %s\n" +
        "Coordinates: %.6f, %.6f\n" +
        "Time: %s\n" +
        "Please help immediately!";

    /**
     * Gets current GPS location
     */
    public static Location getCurrentLocation(Context context) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) 
                != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) 
                != PackageManager.PERMISSION_GRANTED) {
            return null;
        }

        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager == null) return null;

        // Try GPS first
        Location location = null;
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }
        
        // Fallback to network
        if (location == null && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }

        return location;
    }

    /**
     * Formats location as address string (simplified - in real app, use Geocoder)
     */
    private static String formatLocationAddress(Location location) {
        if (location == null) return "Unknown";
        return String.format(Locale.getDefault(), "Lat: %.6f, Lon: %.6f", 
            location.getLatitude(), location.getLongitude());
    }

    /**
     * Sends SOS alert with GPS coordinates to all emergency contacts
     */
    public static void sendSOSAlert(Context context) {
        android.util.Log.d("SOSUtil", "=== sendSOSAlert() called ===");
        
        User user = UserSession.getUser();
        if (user == null) {
            android.util.Log.e("SOSUtil", "No user logged in");
            Toast.makeText(context, "No user logged in", Toast.LENGTH_SHORT).show();
            return;
        }
        
        android.util.Log.d("SOSUtil", "User logged in: " + user.getUsername() + " (ID: " + user.getId() + ")");

        // Check SMS permission
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) 
                != PackageManager.PERMISSION_GRANTED) {
            android.util.Log.e("SOSUtil", "SMS permission NOT granted");
            Toast.makeText(context, "SMS permission required for SOS alerts. Please grant permission in Settings.", Toast.LENGTH_LONG).show();
            return;
        }
        
        android.util.Log.d("SOSUtil", "SMS permission granted");

        // Get location
        Location location = getCurrentLocation(context);
        if (location == null) {
            Toast.makeText(context, "Location not available", Toast.LENGTH_SHORT).show();
            // Still send alert without location
        }

        // Get emergency contacts
        AppDatabase db = AppDatabase.getInstance(context);
        new Thread(() -> {
            List<EmergencyContact> contacts = db.emergencyContactDao().getForUser(user.getId());
            
            if (contacts.isEmpty()) {
                android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
                mainHandler.post(() -> 
                    Toast.makeText(context, "No emergency contacts found", Toast.LENGTH_LONG).show()
                );
                return;
            }

            String userName = user.getUsername();
            String locationStr = location != null ? formatLocationAddress(location) : "Unknown";
            double lat = location != null ? location.getLatitude() : 0.0;
            double lon = location != null ? location.getLongitude() : 0.0;
            String timeStr = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(new java.util.Date());

            String message = String.format(Locale.getDefault(), SOS_MESSAGE_TEMPLATE,
                userName, locationStr, lat, lon, timeStr);

            // Send SMS to each contact
            SmsManager smsManager = SmsManager.getDefault();
            int sentCount = 0;
            int failedCount = 0;
            
            android.util.Log.d("SOSUtil", "Found " + contacts.size() + " emergency contact(s)");
            
            for (EmergencyContact contact : contacts) {
                try {
                    String phoneNumber = contact.phoneNumber;
                    android.util.Log.d("SOSUtil", "Processing contact: " + contact.displayName + " - Phone: " + phoneNumber);
                    
                    if (phoneNumber == null || phoneNumber.isEmpty()) {
                        android.util.Log.w("SOSUtil", "Phone number is null or empty for " + contact.displayName);
                        failedCount++;
                        continue;
                    }
                    
                    // Clean phone number (remove spaces, dashes, etc.)
                    phoneNumber = phoneNumber.trim().replaceAll("[\\s\\-()]", "");
                    
                    // Try different formats for better compatibility
                    // Some phones work better with local numbers, others with international format
                    boolean sent = false;
                    String[] formatsToTry = {
                        phoneNumber,  // Try original first (8 digits)
                        "+216" + phoneNumber,  // Try with Tunisia code
                        "00216" + phoneNumber,  // Try with 00 prefix
                        "216" + phoneNumber  // Try without +
                    };
                    
                    // If already has country code, use as is
                    if (phoneNumber.startsWith("+") || phoneNumber.startsWith("00")) {
                        formatsToTry = new String[]{phoneNumber};
                    }
                    
                    Exception lastException = null;
                    for (String format : formatsToTry) {
                        try {
                            android.util.Log.d("SOSUtil", "Trying to send SMS to: " + format);
                            smsManager.sendTextMessage(format, null, message, null, null);
                            sentCount++;
                            sent = true;
                            android.util.Log.d("SOSUtil", "✓ SMS sent successfully to " + contact.displayName + " using format: " + format);
                            break;  // Success, stop trying other formats
                        } catch (IllegalArgumentException e) {
                            lastException = e;
                            android.util.Log.w("SOSUtil", "Format " + format + " failed (IllegalArgumentException), trying next...");
                            continue;  // Try next format
                        } catch (Exception e) {
                            lastException = e;
                            android.util.Log.w("SOSUtil", "Format " + format + " failed: " + e.getMessage());
                            // Don't continue for other exceptions (like SecurityException)
                            break;
                        }
                    }
                    
                    if (!sent) {
                        android.util.Log.e("SOSUtil", "✗ All formats failed for " + contact.displayName + ". Last error: " + (lastException != null ? lastException.getMessage() : "Unknown"));
                        failedCount++;
                    }
                    
                } catch (SecurityException e) {
                    android.util.Log.e("SOSUtil", "SecurityException: SMS permission denied for " + contact.displayName, e);
                    failedCount++;
                } catch (Exception e) {
                    android.util.Log.e("SOSUtil", "Unexpected error for " + contact.displayName + ": " + e.getMessage(), e);
                    failedCount++;
                }
            }
            
            android.util.Log.d("SOSUtil", "SMS sending complete. Sent: " + sentCount + ", Failed: " + failedCount);

            // Save to alert history
            AlertHistory alertHistory = new AlertHistory(
                System.currentTimeMillis(),
                "SOS",
                "SOS Alert Sent",
                "SOS alert sent to " + sentCount + " emergency contact(s). Location: " + locationStr,
                "CRITICAL",
                locationStr,
                user.getId()
            );
            db.alertHistoryDao().insert(alertHistory);

            final int finalSentCount = sentCount;
            final int finalFailedCount = failedCount;
            android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
            mainHandler.post(() -> {
                AlertSender.vibrate(context, 1000);
                
                String notificationMsg;
                String toastMsg;
                if (finalSentCount > 0) {
                    notificationMsg = "Alert sent to " + finalSentCount + " emergency contact(s)";
                    toastMsg = "SOS alert sent to " + finalSentCount + " contact(s)";
                    if (finalFailedCount > 0) {
                        toastMsg += " (" + finalFailedCount + " failed)";
                    }
                } else {
                    notificationMsg = "Failed to send SOS alert. Check contacts and permissions.";
                    toastMsg = "Failed to send SOS alert. Check Logcat for details.";
                }
                
                AlertSender.sendNotification(context, "SOS Alert", notificationMsg);
                Toast.makeText(context, toastMsg, Toast.LENGTH_LONG).show();
            });
        }).start();
    }

    /**
     * Sends GPS coordinates only (for double tilt gesture)
     */
    public static void sendGPSCoordinates(Context context) {
        User user = UserSession.getUser();
        if (user == null) {
            Toast.makeText(context, "No user logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check SMS permission
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) 
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context, "SMS permission required", Toast.LENGTH_LONG).show();
            return;
        }

        // Get location
        Location location = getCurrentLocation(context);
        if (location == null) {
            Toast.makeText(context, "Location not available", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get emergency contacts
        AppDatabase db = AppDatabase.getInstance(context);
        new Thread(() -> {
            List<EmergencyContact> contacts = db.emergencyContactDao().getForUser(user.getId());
            
            if (contacts.isEmpty()) {
                android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
                mainHandler.post(() -> 
                    Toast.makeText(context, "No emergency contacts found", Toast.LENGTH_LONG).show()
                );
                return;
            }

            String message = String.format(Locale.getDefault(),
                "📍 Location Update\nUser: %s\nCoordinates: %.6f, %.6f\nTime: %s",
                user.getUsername(),
                location.getLatitude(),
                location.getLongitude(),
                new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    .format(new java.util.Date()));

            // Send SMS to each contact
            SmsManager smsManager = SmsManager.getDefault();
            int sentCount = 0;
            int failedCount = 0;
            
            for (EmergencyContact contact : contacts) {
                try {
                    String phoneNumber = contact.phoneNumber;
                    if (phoneNumber == null || phoneNumber.isEmpty()) {
                        failedCount++;
                        continue;
                    }
                    
                    // Clean phone number (remove spaces, dashes, etc.)
                    phoneNumber = phoneNumber.trim().replaceAll("[\\s\\-()]", "");
                    
                    // Try different formats for better compatibility
                    boolean sent = false;
                    String[] formatsToTry = {
                        phoneNumber,  // Try original first (8 digits)
                        "+216" + phoneNumber,  // Try with Tunisia code
                        "00216" + phoneNumber,  // Try with 00 prefix
                        "216" + phoneNumber  // Try without +
                    };
                    
                    // If already has country code, use as is
                    if (phoneNumber.startsWith("+") || phoneNumber.startsWith("00")) {
                        formatsToTry = new String[]{phoneNumber};
                    }
                    
                    Exception lastException = null;
                    for (String format : formatsToTry) {
                        try {
                            android.util.Log.d("SOSUtil", "Trying to send GPS SMS to: " + format);
                            smsManager.sendTextMessage(format, null, message, null, null);
                            sentCount++;
                            sent = true;
                            android.util.Log.d("SOSUtil", "✓ GPS SMS sent successfully to " + contact.displayName + " using format: " + format);
                            break;  // Success, stop trying other formats
                        } catch (IllegalArgumentException e) {
                            lastException = e;
                            android.util.Log.w("SOSUtil", "Format " + format + " failed (IllegalArgumentException), trying next...");
                            continue;  // Try next format
                        } catch (Exception e) {
                            lastException = e;
                            android.util.Log.w("SOSUtil", "Format " + format + " failed: " + e.getMessage());
                            // Don't continue for other exceptions (like SecurityException)
                            break;
                        }
                    }
                    
                    if (!sent) {
                        android.util.Log.e("SOSUtil", "✗ All formats failed for GPS SMS to " + contact.displayName + ". Last error: " + (lastException != null ? lastException.getMessage() : "Unknown"));
                        failedCount++;
                    }
                } catch (SecurityException e) {
                    android.util.Log.e("SOSUtil", "SecurityException: SMS permission denied for " + contact.displayName, e);
                    failedCount++;
                } catch (Exception e) {
                    android.util.Log.e("SOSUtil", "Unexpected error for GPS SMS to " + contact.displayName + ": " + e.getMessage(), e);
                    failedCount++;
                }
            }

            // Save to alert history
            String locationStr = String.format(Locale.getDefault(), "%.6f, %.6f", 
                location.getLatitude(), location.getLongitude());
            AlertHistory alertHistory = new AlertHistory(
                System.currentTimeMillis(),
                "GPS",
                "GPS Location Sent",
                "GPS coordinates sent to " + sentCount + " emergency contact(s)",
                "HIGH",
                locationStr,
                user.getId()
            );
            db.alertHistoryDao().insert(alertHistory);

            final int finalSentCount = sentCount;
            android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
            mainHandler.post(() -> {
                AlertSender.vibrate(context, 500);
                Toast.makeText(context, "Location sent to " + finalSentCount + " contact(s)", 
                    Toast.LENGTH_SHORT).show();
            });
        }).start();
    }
}

