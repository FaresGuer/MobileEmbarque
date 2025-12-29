package com.example.projet.Entities;

import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "emergency_contacts",
        indices = {
                @Index("ownerUserId"),
                @Index(value = {"ownerUserId", "friendUserId"}, unique = true)
        }
)
public class EmergencyContact {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public int ownerUserId;
    @Nullable
    public Integer friendUserId;
    public String displayName;
    public String phoneNumber;
    public boolean isPrimary;

    public EmergencyContact(int ownerUserId, String displayName, String phoneNumber, boolean isPrimary) {
        this.ownerUserId = ownerUserId;
        this.displayName = displayName;
        this.phoneNumber = phoneNumber;
        this.isPrimary = isPrimary;
    }

    public EmergencyContact() {

    }
}
