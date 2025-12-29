package com.example.projet.DataBase;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.projet.Entities.User;

public class UserSession {
    private static final String PREF_NAME = "user_session";

    private static final String KEY_ID = "id";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PHONE = "phone";
    private static final String KEY_DOB = "dob";
    private static final String KEY_AVATAR = "avatar";

    private static User currentUser;

    public static User getUser() {
        return currentUser;
    }
    public static void saveUser(Context context, User user) {
        currentUser = user;

        SharedPreferences sp =
                context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        sp.edit()
                .putInt(KEY_ID, user.getId())
                .putString(KEY_USERNAME, user.getUsername())
                .putString(KEY_EMAIL, user.getEmail())
                .putString(KEY_PHONE, user.getPhoneNumber())
                .putString(KEY_DOB, user.getDateOfBirth())
                .putString(KEY_AVATAR, user.getAvatarPath())
                .apply();
    }
    public static User loadUser(Context context) {
        SharedPreferences sp =
                context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        int id = sp.getInt(KEY_ID, -1);
        if (id == -1) {
            currentUser = null;
            return null;
        }

        User u = new User();
        u.setId(id);
        u.setUsername(sp.getString(KEY_USERNAME, ""));
        u.setEmail(sp.getString(KEY_EMAIL, ""));
        u.setPhoneNumber(sp.getString(KEY_PHONE, ""));
        u.setDateOfBirth(sp.getString(KEY_DOB, ""));
        u.setAvatarPath(sp.getString(KEY_AVATAR, null));

        currentUser = u;
        return u;
    }
    public static void clear(Context context) {
        currentUser = null;
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit()
                .clear()
                .apply();
    }
}