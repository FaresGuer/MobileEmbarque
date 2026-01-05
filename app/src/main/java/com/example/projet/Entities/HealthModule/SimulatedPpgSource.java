package com.example.projet.Entities.HealthModule;

import android.os.Handler;
import android.os.Looper;

import com.example.projet.Entities.HealthModule.Interfaces.PpgSource;

import java.util.Random;

public class SimulatedPpgSource implements PpgSource {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Random rnd = new Random();

    private boolean running = false;
    private Callback cb;

    private final int sampleRateHz;
    private float bpm;
    private long startMs;

    private float driftPhase = 0f;

    public SimulatedPpgSource(int sampleRateHz, float initialBpm) {
        this.sampleRateHz = sampleRateHz;
        this.bpm = initialBpm;
    }

    public void setBpm(float bpm) {
        this.bpm = bpm;
    }

    @Override
    public void start(Callback cb) {
        if (running) return;
        this.cb = cb;
        running = true;
        startMs = System.currentTimeMillis();
        tick();
    }

    private void tick() {
        if (!running) return;

        long now = System.currentTimeMillis();
        float tSec = (now - startMs) / 1000f;

        float freq = bpm / 60f;

        float s1 = (float) Math.sin(2.0 * Math.PI * freq * tSec);
        float s2 = (float) Math.sin(2.0 * Math.PI * freq * 2f * tSec) * 0.25f;
        float wave = s1 + s2;

        driftPhase += 0.01f;
        float drift = (float) Math.sin(driftPhase) * 0.03f;

        float noise = (rnd.nextFloat() - 0.5f) * 0.05f;

        float spike = 0f;
        if (rnd.nextFloat() < 0.01f) {
            spike = (rnd.nextFloat() - 0.5f) * 0.5f;
        }

        float v = 0.55f + 0.12f * wave + drift + noise + spike;
        v = clamp01(v);

        if (cb != null) cb.onSample(new PpgSample(now, v));

        long delay = 1000L / sampleRateHz;
        handler.postDelayed(this::tick, delay);
    }

    private float clamp01(float x) {
        if (x < 0f) return 0f;
        if (x > 1f) return 1f;
        return x;
    }

    @Override
    public void stop() {
        running = false;
        handler.removeCallbacksAndMessages(null);
    }

    @Override
    public boolean isRunning() {
        return running;
    }
}
