package com.example.projet.DAO;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.projet.Entities.EnvironmentAlert;

import java.util.List;

@Dao
public interface EnvironmentAlertDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(EnvironmentAlert alert);

    @Query("SELECT * FROM environment_alerts ORDER BY timestamp DESC")
    List<EnvironmentAlert> getAllAlerts();

    @Query("DELETE FROM environment_alerts")
    void deleteAll();
}

