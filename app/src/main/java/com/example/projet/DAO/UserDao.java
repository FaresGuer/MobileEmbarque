package com.example.projet.DAO;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.projet.Entities.User;

@Dao
public interface UserDao {

    @Insert
    long insertUser(User user);
    @Update
    int update(User user);

    @Query("SELECT * FROM users WHERE username = :username AND password = :passwordHash LIMIT 1")
    User login(String username, String passwordHash);

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    User getUserById(int id);

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    User getByEmail(String email);
    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    User getById(int id);
}