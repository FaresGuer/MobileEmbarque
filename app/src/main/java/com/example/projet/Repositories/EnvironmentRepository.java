package com.example.projet.Repositories;

import android.content.Context;

import com.example.projet.DataBase.AppDatabase;
import com.example.projet.DAO.EnvironmentAlertDao;
import com.example.projet.Entities.EnvironmentAlert;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EnvironmentRepository {
    private final EnvironmentAlertDao dao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public EnvironmentRepository(Context ctx) {
        AppDatabase db = AppDatabase.getInstance(ctx);
        dao = db.environmentAlertDao();
    }

    public void insertAlert(EnvironmentAlert alert) {
        executor.execute(() -> dao.insert(alert));
    }

    public List<EnvironmentAlert> getAllAlerts() {
        // synchronous read (call on background thread). For UI use, you should call this off main thread.
        return dao.getAllAlerts();
    }

    public void deleteAll() {
        executor.execute(dao::deleteAll);
    }
}

