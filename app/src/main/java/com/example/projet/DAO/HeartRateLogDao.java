package com.example.projet.DAO;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.projet.Entities.HealthModule.HeartRateLog;

import java.util.List;

@Dao
public interface HeartRateLogDao {
    @Insert
    long insert(HeartRateLog log);

    @Query("SELECT * FROM heart_rate_logs WHERE ownerUserId = :userId ORDER BY timestampMs DESC")
    List<HeartRateLog> getForUser(int userId);

    @Query("SELECT * FROM heart_rate_logs WHERE ownerUserId = :userId AND isAbnormal = 1 ORDER BY timestampMs DESC")
    List<HeartRateLog> getAbnormalForUser(int userId);

    @Query("DELETE FROM heart_rate_logs WHERE ownerUserId = :userId")
    int deleteAllForUser(int userId);

    @Query("DELETE FROM heart_rate_logs WHERE timestampMs < :olderThanMs")
    int deleteOlderThan(long olderThanMs);
}
