package com.example.projet.DAO;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.projet.Entities.AlertHistory;

import java.util.List;

@Dao
public interface AlertHistoryDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(AlertHistory alert);

    @Query("SELECT * FROM alert_history WHERE userId = :userId ORDER BY timestamp DESC")
    List<AlertHistory> getAlertsForUser(int userId);

    @Query("SELECT * FROM alert_history ORDER BY timestamp DESC")
    List<AlertHistory> getAllAlerts();

    @Query("SELECT * FROM alert_history WHERE alertType = :alertType AND userId = :userId ORDER BY timestamp DESC")
    List<AlertHistory> getAlertsByType(String alertType, int userId);

    @Query("DELETE FROM alert_history WHERE userId = :userId")
    void deleteAlertsForUser(int userId);

    @Query("DELETE FROM alert_history")
    void deleteAll();
}

