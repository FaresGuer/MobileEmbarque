package com.example.projet.DataBase;

import androidx.room.TypeConverter;

import com.example.projet.Entities.Enums.FriendStatus;

public class Converters {
    @TypeConverter
    public static String fromFriendStatus(FriendStatus status) {
        return status == null ? null : status.name();
    }

    @TypeConverter
    public static FriendStatus toFriendStatus(String value) {
        return value == null ? null : FriendStatus.valueOf(value);
    }
}
