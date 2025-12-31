package com.example.projet.Entities.SafetyModule.Interfaces;

import com.example.projet.Entities.SafetyModule.MotionSample;

public interface MotionSource {
    interface Callback {
        void onSample(MotionSample s);
        void onError(String msg);
    }

    void start(Callback cb);
    void stop();
    boolean isRunning();
}
