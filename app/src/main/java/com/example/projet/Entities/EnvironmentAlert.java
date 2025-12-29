package com.example.projet.Entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "environment_alerts")
public class EnvironmentAlert {
    @PrimaryKey(autoGenerate = true)
    private long id;

    private long timestamp; // epoch millis
    private String sensor; // Temperature, Light, Proximity
    private String value; // textual value (e.g., "38.5 °C" or "Near (0.0 cm)")
    private String severity; // e.g., INFO/WARNING/CRITICAL
    private String message; // human readable message

    public EnvironmentAlert(long timestamp, String sensor, String value, String severity, String message) {
        this.timestamp = timestamp;
        this.sensor = sensor;
        this.value = value;
        this.severity = severity;
        this.message = message;
    }

    // getters and setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getSensor() { return sensor; }
    public void setSensor(String sensor) { this.sensor = sensor; }

    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}

