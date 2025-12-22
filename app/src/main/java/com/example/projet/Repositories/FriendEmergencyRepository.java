package com.example.projet.Repositories;

import android.content.Context;

import com.example.projet.DataBase.AppDatabase;
import com.example.projet.Entities.EmergencyContact;
import com.example.projet.Entities.User;

public class FriendEmergencyRepository {

    private final AppDatabase db;

    public FriendEmergencyRepository(Context context) {
        db = AppDatabase.getInstance(context.getApplicationContext());
    }

    public void setFriendEmergency(int ownerUserId, int friendUserId, boolean makeEmergency) {
        db.runInTransaction(() -> {
            EmergencyContact existing = db.emergencyContactDao().getByOwnerAndFriend(ownerUserId, friendUserId);

            if (makeEmergency) {
                if (existing != null) return;

                User friendUser = db.userDao().getUserById(friendUserId);
                if (friendUser == null) return;

                EmergencyContact ec = new EmergencyContact();
                ec.ownerUserId = ownerUserId;
                ec.friendUserId = friendUserId;
                ec.displayName = friendUser.getUsername();
                ec.phoneNumber = friendUser.getPhoneNumber();
                ec.isPrimary = false;

                db.emergencyContactDao().insert(ec);

            } else {
                if (existing == null) return;
                db.emergencyContactDao().delete(existing);
            }
        });
    }
    public void toggleFriendEmergency(int ownerUserId, int friendUserId) {
        db.runInTransaction(() -> {
            EmergencyContact existing = db.emergencyContactDao().getByOwnerAndFriend(ownerUserId, friendUserId);

            if (existing != null) {
                db.emergencyContactDao().delete(existing);
                return;
            }

            User friendUser = db.userDao().getUserById(friendUserId);
            if (friendUser == null) return;

            String phone = friendUser.getPhoneNumber();
            if (phone == null || phone.trim().isEmpty()) return;

            EmergencyContact ec = new EmergencyContact();
            ec.ownerUserId = ownerUserId;
            ec.friendUserId = friendUserId;
            ec.displayName = friendUser.getUsername();
            ec.phoneNumber = phone;
            ec.isPrimary = false;

            db.emergencyContactDao().insert(ec);
        });
    }
}
