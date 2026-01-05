package com.example.projet.Entities.SafetyModule;

public class FallDetector {

    public static class Config {
        public float impactG = 2.7f;
        public float freeFallG = 0.5f;
        public long freeFallWindowMs = 1200;
        public long stillnessWindowMs = 2500;
        public float stillnessDeltaG = 0.18f;
        public long cooldownMs = 120000;
    }

    public enum Event { NONE, POSSIBLE_FALL, CONFIRMED_FALL }

    private final Config cfg;

    private long lastImpactMs = 0;
    private long freeFallStartMs = 0;
    private boolean sawFreeFall = false;

    private float lastMagG = 1.0f;
    private long stillStartMs = 0;

    private long lastConfirmedMs = 0;

    public FallDetector(Config cfg) {
        this.cfg = cfg;
    }

    public Event onSample(MotionSample s) {
        float mag = (float) Math.sqrt(s.ax * s.ax + s.ay * s.ay + s.az * s.az);
        float g = mag / 9.81f;

        long now = s.tMs;

        if (now - lastConfirmedMs < cfg.cooldownMs) {
            lastMagG = g;
            return Event.NONE;
        }

        if (g < cfg.freeFallG) {
            if (!sawFreeFall) {
                sawFreeFall = true;
                freeFallStartMs = now;
            }
        } else {
            if (sawFreeFall && (now - freeFallStartMs) > cfg.freeFallWindowMs) {
                sawFreeFall = false;
            }
        }

        if (g > cfg.impactG) {
            lastImpactMs = now;
            stillStartMs = 0;
            lastMagG = g;
            return Event.POSSIBLE_FALL;
        }

        if (lastImpactMs > 0 && (now - lastImpactMs) < 6000) {
            float delta = Math.abs(g - lastMagG);
            if (delta < cfg.stillnessDeltaG) {
                if (stillStartMs == 0) stillStartMs = now;
                if ((now - stillStartMs) >= cfg.stillnessWindowMs) {
                    lastConfirmedMs = now;
                    sawFreeFall = false;
                    lastImpactMs = 0;
                    stillStartMs = 0;
                    lastMagG = g;
                    return Event.CONFIRMED_FALL;
                }
            } else {
                stillStartMs = 0;
            }
        }

        lastMagG = g;
        return Event.NONE;
    }
}
