package com.example.projet.Services;

import android.content.Context;

import com.example.projet.Sensors.EnvironmentMonitor;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class EnvironmentService implements EnvironmentMonitor.Listener {
    public interface Listener {
        void onTemperature(Float celsius);
        void onLight(Float lux);
        void onProximity(Float cm);
        void onSensorUnavailable(String sensorName);
    }

    private static EnvironmentService INSTANCE;
    private final Context appContext;
    private final EnvironmentMonitor monitor;
    private final List<Listener> listeners = new CopyOnWriteArrayList<>();

    // last-known values
    private volatile Float lastTemperature = null;
    private volatile Float lastLight = null;
    private volatile Float lastProximity = null;

    private EnvironmentService(Context ctx) {
        appContext = ctx.getApplicationContext();
        monitor = new EnvironmentMonitor(appContext);
        monitor.setListener(this);
    }

    public static synchronized EnvironmentService getInstance(Context ctx) {
        if (INSTANCE == null) {
            INSTANCE = new EnvironmentService(ctx);
        }
        return INSTANCE;
    }

    public void start() {
        monitor.start();
    }

    public void stop() {
        monitor.stop();
    }

    public void registerListener(Listener l) {
        if (l == null) return;
        listeners.add(l);
        // provide last-known values immediately
        if (lastTemperature != null) l.onTemperature(lastTemperature);
        if (lastLight != null) l.onLight(lastLight);
        if (lastProximity != null) l.onProximity(lastProximity);
    }

    public void unregisterListener(Listener l) {
        if (l == null) return;
        listeners.remove(l);
    }

    public Float getLastTemperature() { return lastTemperature; }
    public Float getLastLight() { return lastLight; }
    public Float getLastProximity() { return lastProximity; }

    // EnvironmentMonitor.Listener implementation - forward to registered listeners
    @Override
    public void onTemperature(Float celsius) {
        lastTemperature = celsius;
        for (Listener l : listeners) {
            try { l.onTemperature(celsius); } catch (Exception ignored) {}
        }
    }

    @Override
    public void onLight(Float lux) {
        lastLight = lux;
        for (Listener l : listeners) {
            try { l.onLight(lux); } catch (Exception ignored) {}
        }
    }

    @Override
    public void onProximity(Float cm) {
        lastProximity = cm;
        for (Listener l : listeners) {
            try { l.onProximity(cm); } catch (Exception ignored) {}
        }
    }

    @Override
    public void onSensorUnavailable(String sensorName) {
        for (Listener l : listeners) {
            try { l.onSensorUnavailable(sensorName); } catch (Exception ignored) {}
        }
    }
}
