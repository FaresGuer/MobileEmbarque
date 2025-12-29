package com.example.projet.Entities.HealthModule.Interfaces;

import com.example.projet.Entities.HealthModule.PpgSample;

public interface PpgSource {
    interface Callback {
        void onSample(PpgSample s);
        void onError(String msg);
    }

    void start(Callback cb);
    void stop();
    boolean isRunning();
}
