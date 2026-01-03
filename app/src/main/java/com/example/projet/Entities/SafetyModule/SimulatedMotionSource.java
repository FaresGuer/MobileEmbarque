package com.example.projet.Entities.SafetyModule;

import android.os.Handler;
import android.os.Looper;

import com.example.projet.Entities.SafetyModule.Interfaces.MotionSource;

import java.util.Random;

public class SimulatedMotionSource implements MotionSource {

    public enum Scenario { IDLE, WALK, DROP, FALL }

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Random rnd = new Random();

    private boolean running = false;
    private Callback cb;

    private int sampleRateHz = 50;
    private Scenario scenario = Scenario.IDLE;
    private long scenarioStartMs = 0;

    public SimulatedMotionSource(int sampleRateHz) {
        this.sampleRateHz = sampleRateHz;
    }

    public void setScenario(Scenario s) {
        scenario = s;
        scenarioStartMs = System.currentTimeMillis();
    }

    @Override
    public void start(Callback cb) {
        if (running) return;
        this.cb = cb;
        running = true;
        scenarioStartMs = System.currentTimeMillis();
        tick();
    }

    private void tick() {
        if (!running) return;

        long now = System.currentTimeMillis();
        float t = (now - scenarioStartMs) / 1000f;

        float ax = 0f, ay = 0f, az = 9.81f;

        if (scenario == Scenario.WALK) {
            float w = (float) Math.sin(2.0 * Math.PI * 1.8f * t);
            az = 9.81f + 1.2f * w + noise(0.25f);
            ax = noise(0.35f);
            ay = noise(0.35f);
        } else if (scenario == Scenario.DROP) {
            if (t < 0.25f) {
                az = 1.0f + noise(0.25f);
            } else if (t < 0.32f) {
                az = 30.0f + noise(2.0f);
            } else {
                az = 9.81f + noise(0.2f);
                ax = noise(0.2f);
                ay = noise(0.2f);
            }
        } else if (scenario == Scenario.FALL) {
            if (t < 0.35f) {
                az = 1.0f + noise(0.25f);
            } else if (t < 0.45f) {
                az = 35.0f + noise(3.0f);
                ax = 8.0f + noise(1.0f);
                ay = 6.0f + noise(1.0f);
            } else {
                az = 9.81f + noise(0.15f);
                ax = noise(0.15f);
                ay = noise(0.15f);
            }
        }

        if (cb != null) cb.onSample(new MotionSample(now, ax, ay, az, 0f, 0f, 0f));

        long delay = 1000L / sampleRateHz;
        handler.postDelayed(this::tick, delay);
    }

    private float noise(float amp) {
        return (rnd.nextFloat() - 0.5f) * 2f * amp;
    }

    @Override
    public void stop() {
        running = false;
        handler.removeCallbacksAndMessages(null);
        cb = null;
    }

    @Override
    public boolean isRunning() {
        return running;
    }
}
