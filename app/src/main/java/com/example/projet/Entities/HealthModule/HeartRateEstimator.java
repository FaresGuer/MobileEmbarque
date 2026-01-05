package com.example.projet.Entities.HealthModule;

import java.util.ArrayList;
import java.util.List;

public class HeartRateEstimator {
    public static class Result {
        public final int bpm;
        public final float quality;
        public final boolean valid;

        public Result(int bpm, float quality, boolean valid) {
            this.bpm = bpm;
            this.quality = quality;
            this.valid = valid;
        }
    }

    private final long windowMs;
    private final List<PpgSample> buf = new ArrayList<>();

    public HeartRateEstimator(long windowMs) {
        this.windowMs = windowMs;
    }

    public synchronized void add(PpgSample s) {
        buf.add(s);
        trim(s.tMs);
    }

    private void trim(long nowMs) {
        long minT = nowMs - windowMs;
        int i = 0;
        while (i < buf.size() && buf.get(i).tMs < minT) i++;
        if (i > 0) buf.subList(0, i).clear();
    }

    public synchronized Result estimate() {
        if (buf.size() < 60) return new Result(0, 0f, false);


        int n = buf.size();
        float[] x = new float[n];
        long[] t = new long[n];
        for (int i = 0; i < n; i++) {
            x[i] = buf.get(i).v;
            t[i] = buf.get(i).tMs;
        }

        float[] y = highPassByMovingAverage(x, 15);


        float[] z = movingAverage(y, 5);

        List<Integer> peaks = findPeaks(z, t);

        if (peaks.size() < 3) {
            return new Result(0, 0.1f, false);
        }

        List<Float> intervalsSec = new ArrayList<>();
        for (int i = 1; i < peaks.size(); i++) {
            long dt = t[peaks.get(i)] - t[peaks.get(i - 1)];
            intervalsSec.add(dt / 1000f);
        }

        float mean = mean(intervalsSec);
        float var = variance(intervalsSec, mean);

        if (mean <= 0f) return new Result(0, 0f, false);

        int bpm = Math.round(60f / mean);

        float stability = (float) Math.exp(-var * 10f); // heuristic
        float peakCountScore = Math.min(1f, peaks.size() / 8f);
        float quality = clamp01(0.2f + 0.5f * stability + 0.3f * peakCountScore);

        boolean valid = bpm >= 40 && bpm <= 200 && quality >= 0.4f;
        return new Result(bpm, quality, valid);
    }

    private float[] highPassByMovingAverage(float[] x, int win) {
        float[] ma = movingAverage(x, win);
        float[] y = new float[x.length];
        for (int i = 0; i < x.length; i++) y[i] = x[i] - ma[i];
        return y;
    }

    private float[] movingAverage(float[] x, int win) {
        float[] y = new float[x.length];
        float sum = 0f;
        int half = win / 2;

        for (int i = 0; i < x.length; i++) {
            sum = 0f;
            int c = 0;
            for (int k = i - half; k <= i + half; k++) {
                if (k >= 0 && k < x.length) {
                    sum += x[k];
                    c++;
                }
            }
            y[i] = (c == 0) ? x[i] : (sum / c);
        }
        return y;
    }

    private List<Integer> findPeaks(float[] z, long[] t) {
        List<Integer> peaks = new ArrayList<>();

        long minDistMs = 300;

        float amp = percentileAbs(z, 90);
        float thr = Math.max(0.01f, amp * 0.35f);

        int lastPeak = -1;

        for (int i = 1; i < z.length - 1; i++) {
            boolean isPeak = z[i] > z[i - 1] && z[i] > z[i + 1] && z[i] > thr;
            if (!isPeak) continue;

            if (lastPeak == -1) {
                peaks.add(i);
                lastPeak = i;
            } else {
                long dt = t[i] - t[lastPeak];
                if (dt >= minDistMs) {
                    peaks.add(i);
                    lastPeak = i;
                } else {

                    if (z[i] > z[lastPeak]) {
                        peaks.set(peaks.size() - 1, i);
                        lastPeak = i;
                    }
                }
            }
        }

        return peaks;
    }

    private float percentileAbs(float[] x, int p) {
        float[] a = new float[x.length];
        for (int i = 0; i < x.length; i++) a[i] = Math.abs(x[i]);
        java.util.Arrays.sort(a);
        int idx = (int) Math.round((p / 100f) * (a.length - 1));
        if (idx < 0) idx = 0;
        if (idx >= a.length) idx = a.length - 1;
        return a[idx];
    }

    private float mean(List<Float> xs) {
        float s = 0f;
        for (float v : xs) s += v;
        return s / xs.size();
    }

    private float variance(List<Float> xs, float mean) {
        float s = 0f;
        for (float v : xs) {
            float d = v - mean;
            s += d * d;
        }
        return s / xs.size();
    }

    private float clamp01(float x) {
        if (x < 0f) return 0f;
        if (x > 1f) return 1f;
        return x;
    }
}
