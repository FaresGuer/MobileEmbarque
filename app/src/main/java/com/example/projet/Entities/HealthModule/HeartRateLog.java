package com.example.projet.Entities.HealthModule;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "heart_rate_logs",
        indices = {
                @Index("ownerUserId"),
                @Index("timestampMs"),
                @Index(value = {"ownerUserId", "timestampMs"})
        }
)
public class HeartRateLog {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public int ownerUserId;

    public long timestampMs;

    public int bpm;

    public float quality;

    public boolean isAbnormal;

    public boolean isVirtual;

    public String contextTag;
}
