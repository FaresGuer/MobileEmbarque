package com.example.projet.Entities.SafetyModule;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "fall_event")
public class FallEvent {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public int ownerUserId;
    public long timestampMs;

    public String type;
    public float peakG;

    public Double lat;
    public Double lon;

    public boolean smsSent;
    public boolean userCancelled;
}
