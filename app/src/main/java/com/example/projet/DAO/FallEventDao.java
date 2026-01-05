package com.example.projet.DAO;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.projet.Entities.SafetyModule.FallEvent;

import java.util.List;

@Dao
public interface FallEventDao {

    @Insert
    long insert(FallEvent e);

    @Query("SELECT * FROM fall_event WHERE ownerUserId = :userId ORDER BY timestampMs DESC")
    List<FallEvent> getAllForUser(int userId);

    @Query("DELETE FROM fall_event WHERE timestampMs < :cutoffMs")
    int deleteOlderThan(long cutoffMs);
}
