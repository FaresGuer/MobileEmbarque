package com.example.projet.Entities;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

/**
 * Entity to store all types of alerts history
 * Supports: SOS, GPS, Flashlight, Environment alerts
 */
@Entity(tableName = "alert_history")
public class AlertHistory {
    @PrimaryKey(autoGenerate = true)
    private long id;

    private long timestamp; // epoch millis
    private String alertType; // SOS, GPS, FLASHLIGHT, ENVIRONMENT, FALL, etc.
    private String title; // Alert title
    private String message; // Alert message/details
    private String severity; // CRITICAL, HIGH, MEDIUM, LOW, INFO
    private String location; // GPS coordinates if available
    private int userId; // User who triggered the alert

    public AlertHistory() {
    }

    @Ignore
    public AlertHistory(long timestamp, String alertType, String title, String message, String severity, String location, int userId) {
        this.timestamp = timestamp;
        this.alertType = alertType;
        this.title = title;
        this.message = message;
        this.severity = severity;
        this.location = location;
        this.userId = userId;
    }

    // Getters and Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getAlertType() {
        return alertType;
    }

    public void setAlertType(String alertType) {
        this.alertType = alertType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
}

