package com.example.projet.DataBase;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.projet.DAO.EmergencyContactDao;
import com.example.projet.DAO.FriendDao;
import com.example.projet.DAO.HeartRateLogDao;
import com.example.projet.DAO.UserDao;
import com.example.projet.Entities.EmergencyContact;
import com.example.projet.Entities.Friend;
import com.example.projet.Entities.HealthModule.HeartRateLog;
import com.example.projet.Entities.User;

@Database(entities = {User.class, EmergencyContact.class, Friend.class, HeartRateLog.class}, version = 2)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase instance;

    public abstract UserDao userDao();
    public abstract EmergencyContactDao emergencyContactDao();
    public abstract FriendDao friendDao();
    public abstract HeartRateLogDao heartRateLogDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "Trackini"
                    )
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}