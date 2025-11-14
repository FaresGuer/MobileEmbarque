package com.example.projet.DataBase;

import com.example.projet.Entities.User;

public class UserSession {

    private static User currentUser;

    public static void setUser(User user) {
        currentUser = user;
    }

    public static User getUser() {
        return currentUser;
    }

    public static void clear() {
        currentUser = null;
    }
}