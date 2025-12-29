package com.example.projet.Entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.example.projet.Entities.Enums.FriendStatus;

@Entity(
        tableName = "friends",
        indices = { @Index(value = {"ownerUserId"}), @Index(value = {"friendUserId"}) }
)
public class Friend {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public int ownerUserId;
    public int friendUserId;
    @NonNull
    public FriendStatus status=FriendStatus.PENDING;
    public boolean isFavorite;
}
