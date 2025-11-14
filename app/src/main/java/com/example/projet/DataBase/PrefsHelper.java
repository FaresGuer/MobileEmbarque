package com.example.projet.DataBase;

import android.content.Context;
import android.content.SharedPreferences;

public class PrefsHelper {

    private static final String PREFS_NAME = "user_prefs";
    private static final String KEY_REMEMBERED_USER_ID = "remembered_user_id";

    public static void saveRememberedUserId(Context context, int userId) {
        SharedPreferences prefs =
                context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt(KEY_REMEMBERED_USER_ID, userId).apply();
    }

    public static int getRememberedUserId(Context context) {
        SharedPreferences prefs =
                context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_REMEMBERED_USER_ID, -1);
    }

    public static void clearRememberedUser(Context context) {
        SharedPreferences prefs =
                context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().remove(KEY_REMEMBERED_USER_ID).apply();
    }
}