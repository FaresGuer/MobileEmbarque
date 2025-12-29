package com.example.projet.DAO;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.projet.DTO.FriendItem;
import com.example.projet.DTO.FriendRequestItem;
import com.example.projet.Entities.EmergencyContact;
import com.example.projet.Entities.Friend;

import java.util.List;

@Dao
public interface FriendDao {

    @Query("SELECT * FROM friends WHERE friendUserId = :userId AND status = 'PENDING'")
    List<Friend> getIncomingRequests(int userId);
    @Query("SELECT * FROM friends WHERE ownerUserId = :ownerId AND friendUserId = :friendId LIMIT 1")
    Friend getAnyRelation(int ownerId, int friendId);
    @Query("SELECT * FROM friends WHERE ownerUserId = :userId AND status = 'PENDING'")
    List<Friend> getOutgoingRequests(int userId);

    @Query("SELECT * FROM friends WHERE ownerUserId = :userId AND status = 'ACCEPTED'")
    List<Friend> getFriends(int userId);
    @Query("SELECT * FROM friends WHERE ownerUserId = :userId AND status = 'ACCEPTED' ORDER BY isFavorite DESC, id DESC")
    List<Friend> getForUser(int userId);

    @Query("SELECT * FROM friends WHERE ownerUserId = :ownerId AND friendUserId = :friendId LIMIT 1")
    Friend getByOwnerAndFriend(int ownerId, int friendId);

    @Insert
    long insert(Friend f);

    @Delete
    int delete(Friend f);
    @Query("DELETE FROM friends WHERE (ownerUserId = :a AND friendUserId = :b) OR (ownerUserId = :b AND friendUserId = :a)")
    int deleteMutual(int a, int b);
    @Update
    int update(Friend f);
    @Query(
            "SELECT " +
                    "f.id AS friendRowId, " +
                    "f.friendUserId AS friendUserId, " +
                    "u.username AS username, " +
                    "u.email AS email, " +
                    "f.isFavorite AS isFavorite, " +
                    "CASE WHEN ec.id IS NULL THEN 0 ELSE 1 END AS isEmergency " +
                    "FROM friends f " +
                    "INNER JOIN users u ON u.id = f.friendUserId " +
                    "LEFT JOIN emergency_contacts ec " +
                    "ON ec.ownerUserId = f.ownerUserId AND ec.friendUserId = f.friendUserId " +
                    "WHERE f.ownerUserId = :ownerId AND f.status = 'ACCEPTED' " +
                    "ORDER BY f.isFavorite DESC, u.username ASC"
    )
    List<FriendItem> getFriendItems(int ownerId);
    @Query(
            "SELECT f.id AS friendRowId, f.ownerUserId AS requesterUserId, u.username AS username, u.email AS email " +
                    "FROM friends f INNER JOIN users u ON u.id = f.ownerUserId " +
                    "WHERE f.friendUserId = :meId AND f.status = 'PENDING' " +
                    "ORDER BY f.id DESC"
    )
    List<FriendRequestItem> getIncomingRequestItems(int meId);

    @Query("SELECT * FROM friends WHERE id = :id LIMIT 1")
    Friend getById(int id);
    @Query("UPDATE friends SET isFavorite = :fav WHERE id = :friendRowId")
    int setFavorite(int friendRowId, boolean fav);

}

