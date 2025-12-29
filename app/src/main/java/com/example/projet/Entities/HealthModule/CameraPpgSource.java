package com.example.projet.Entities.HealthModule;

import android.content.Context;
import android.util.Size;

import androidx.annotation.NonNull;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.example.projet.Entities.HealthModule.Interfaces.PpgSource;
import com.google.common.util.concurrent.ListenableFuture;

import java.nio.ByteBuffer;
import java.util.concurrent.Executor;

public class CameraPpgSource implements PpgSource {
    private final Context appContext;
    private final LifecycleOwner lifecycleOwner;
    private final PreviewView previewView;

    private boolean running = false;
    private Callback cb;

    private ProcessCameraProvider cameraProvider;
    private Camera camera;

    public CameraPpgSource(Context context, LifecycleOwner owner, PreviewView previewView) {
        this.appContext = context.getApplicationContext();
        this.lifecycleOwner = owner;
        this.previewView = previewView;
    }

    @Override
    public void start(Callback cb) {
        if (running) return;
        this.cb = cb;
        running = true;

        Executor mainExecutor = ContextCompat.getMainExecutor(appContext);
        ListenableFuture<ProcessCameraProvider> future = ProcessCameraProvider.getInstance(appContext);

        future.addListener(() -> {
            try {
                cameraProvider = future.get();
                bindUseCases(mainExecutor);
            } catch (Exception e) {
                running = false;
                if (this.cb != null) this.cb.onError("Camera init failed: " + e.getMessage());
            }
        }, mainExecutor);
    }

    private void bindUseCases(Executor analyzerExecutor) {
        if (cameraProvider == null) return;

        cameraProvider.unbindAll();

        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        ImageAnalysis analysis = new ImageAnalysis.Builder()
                .setTargetResolution(new Size(640, 480))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();

        analysis.setAnalyzer(analyzerExecutor, this::analyzeFrame);

        CameraSelector selector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        camera = cameraProvider.bindToLifecycle(lifecycleOwner, selector, preview, analysis);

        if (camera.getCameraInfo().hasFlashUnit()) {
            camera.getCameraControl().enableTorch(true);
        } else {
            if (cb != null) cb.onError("No flash unit on this device.");
        }
    }

    private void analyzeFrame(@NonNull ImageProxy image) {
        try {
            float redMean = meanRedFromYuv(image);
            long t = System.currentTimeMillis();
            if (cb != null) cb.onSample(new PpgSample(t, redMean));
        } catch (Exception e) {
            if (cb != null) cb.onError("Frame processing error: " + e.getMessage());
        } finally {
            image.close();
        }
    }

    private float meanRedFromYuv(ImageProxy image) {

        ImageProxy.PlaneProxy yPlane = image.getPlanes()[0];
        ImageProxy.PlaneProxy uPlane = image.getPlanes()[1];
        ImageProxy.PlaneProxy vPlane = image.getPlanes()[2];

        ByteBuffer yBuf = yPlane.getBuffer();
        ByteBuffer uBuf = uPlane.getBuffer();
        ByteBuffer vBuf = vPlane.getBuffer();

        int yRowStride = yPlane.getRowStride();
        int uvRowStride = uPlane.getRowStride();
        int uvPixelStride = uPlane.getPixelStride();

        int w = image.getWidth();
        int h = image.getHeight();

        // Sample every N pixels to reduce CPU
        int step = 4;

        long sumR = 0;
        long count = 0;

        for (int y = 0; y < h; y += step) {
            int yRowStart = y * yRowStride;
            int uvRowStart = (y / 2) * uvRowStride;

            for (int x = 0; x < w; x += step) {
                int yIndex = yRowStart + x;
                int uvIndex = uvRowStart + (x / 2) * uvPixelStride;

                int Y = yBuf.get(yIndex) & 0xFF;
                int U = uBuf.get(uvIndex) & 0xFF;
                int V = vBuf.get(uvIndex) & 0xFF;

                // Convert YUV to RGB (BT.601)
                int c = Y - 16;
                int d = U - 128;
                int e = V - 128;

                int r = clamp255((298 * c + 409 * e + 128) >> 8);
                // g, b not needed for classic red PPG mean
                sumR += r;
                count++;
            }
        }

        if (count == 0) return 0f;
        float meanR = (float) sumR / (float) count;
        return meanR / 255f;
    }

    private int clamp255(int v) {
        if (v < 0) return 0;
        if (v > 255) return 255;
        return v;
    }

    @Override
    public void stop() {
        running = false;

        if (camera != null) {
            try {
                if (camera.getCameraInfo().hasFlashUnit()) {
                    camera.getCameraControl().enableTorch(false);
                }
            } catch (Exception ignored) {}
        }

        if (cameraProvider != null) {
            cameraProvider.unbindAll();
        }

        camera = null;
        cameraProvider = null;
        cb = null;
    }

    @Override
    public boolean isRunning() {
        return running;
    }
}
