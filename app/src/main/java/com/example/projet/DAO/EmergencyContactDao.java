package com.example.projet.DAO;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.projet.Entities.EmergencyContact;

import java.util.List;

@Dao
public interface EmergencyContactDao {

    @Query("SELECT * FROM emergency_contacts WHERE ownerUserId = :ownerId AND friendUserId = :friendUserId LIMIT 1")
    EmergencyContact getByOwnerAndFriend(int ownerId, int friendUserId);

    @Query("DELETE FROM emergency_contacts WHERE ownerUserId = :ownerId AND friendUserId = :friendUserId")
    int deleteByOwnerAndFriend(int ownerId, int friendUserId);

    @Query("SELECT * FROM emergency_contacts WHERE ownerUserId = :userId ORDER BY isPrimary DESC, displayName ASC")
    List<EmergencyContact> getForUser(int userId);

    @Query("SELECT * FROM emergency_contacts WHERE id = :id LIMIT 1")
    EmergencyContact getById(int id);

    @Insert
    long insert(EmergencyContact contact);

    @Update
    int update(EmergencyContact contact);

    @Delete
    int delete(EmergencyContact contact);

    @Query("UPDATE emergency_contacts SET isPrimary = 0 WHERE ownerUserId = :userId")
    int clearPrimaryForUser(int userId);
}