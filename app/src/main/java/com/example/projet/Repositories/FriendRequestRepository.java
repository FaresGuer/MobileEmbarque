package com.example.projet.Repositories;

import android.content.Context;

import com.example.projet.DataBase.AppDatabase;
import com.example.projet.Entities.Enums.FriendStatus;
import com.example.projet.Entities.Friend;

public class FriendRequestRepository {
    private final AppDatabase db;

    public FriendRequestRepository(Context context) {
        this.db = AppDatabase.getInstance(context.getApplicationContext());
    }

    public void acceptRequest(int requestRowId) {
        db.runInTransaction(() -> {
            Friend req = db.friendDao().getById(requestRowId);
            if (req == null) return;

            // Only accept pending
            if (req.status != FriendStatus.PENDING) return;

            // Update the existing request row to ACCEPTED
            req.status = FriendStatus.ACCEPTED;
            db.friendDao().update(req);

            // Ensure reverse row exists as ACCEPTED
            Friend reverse = db.friendDao().getByOwnerAndFriend(req.friendUserId, req.ownerUserId);
            if (reverse == null) {
                Friend f2 = new Friend();
                f2.ownerUserId = req.friendUserId;   // receiver becomes owner of their own friend list
                f2.friendUserId = req.ownerUserId;   // requester
                f2.status = FriendStatus.ACCEPTED;
                f2.isFavorite = false;
                db.friendDao().insert(f2);
            } else {
                reverse.status = FriendStatus.ACCEPTED;
                db.friendDao().update(reverse);
            }
        });
    }

    public void rejectRequest(int requestRowId) {
        db.runInTransaction(() -> {
            Friend req = db.friendDao().getById(requestRowId);
            if (req == null) return;
            req.status = FriendStatus.REJECTED;
            db.friendDao().update(req);
        });
    }
}
