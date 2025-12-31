package com.example.projet.Entities.SafetyModule;

public class MotionSample {
    public final long tMs;
    public final float ax, ay, az;
    public final float gx, gy, gz;

    public MotionSample(long tMs, float ax, float ay, float az, float gx, float gy, float gz) {
        this.tMs = tMs;
        this.ax = ax;
        this.ay = ay;
        this.az = az;
        this.gx = gx;
        this.gy = gy;
        this.gz = gz;
    }
}