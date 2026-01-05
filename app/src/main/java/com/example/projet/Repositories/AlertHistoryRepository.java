package com.example.projet.Repositories;

import android.content.Context;

import com.example.projet.DataBase.AppDatabase;
import com.example.projet.Entities.AlertHistory;

import java.util.List;

public class AlertHistoryRepository {
    private final AppDatabase db;

    public AlertHistoryRepository(Context context) {
        db = AppDatabase.getInstance(context.getApplicationContext());
    }

    public void insertAlert(AlertHistory alert) {
        new Thread(() -> {
            db.alertHistoryDao().insert(alert);
        }).start();
    }

    public List<AlertHistory> getAlertsForUser(int userId) {
        return db.alertHistoryDao().getAlertsForUser(userId);
    }

    public List<AlertHistory> getAllAlerts() {
        return db.alertHistoryDao().getAllAlerts();
    }

    public List<AlertHistory> getAlertsByType(String alertType, int userId) {
        return db.alertHistoryDao().getAlertsByType(alertType, userId);
    }

    public void deleteAlertsForUser(int userId) {
        new Thread(() -> {
            db.alertHistoryDao().deleteAlertsForUser(userId);
        }).start();
    }

    public void deleteAll() {
        new Thread(() -> {
            db.alertHistoryDao().deleteAll();
        }).start();
    }
}

