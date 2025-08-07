package com.fceumm.wrapper.input;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import java.util.HashMap;
import java.util.Map;

public class TurboButtonSystem {
    private static final String TAG = "TurboButtonSystem";
    private Handler handler;
    private Map<Integer, TurboButtonConfig> turboConfigs;
    private Map<Integer, Boolean> turboStates;
    private boolean turboEnabled = false;
    private int turboSpeed = 10; // Frames entre chaque activation

    public static class TurboButtonConfig {
        public boolean enabled = false;
        public int speed = 10;
        public boolean active = false;
    }

    public TurboButtonSystem() {
        this.handler = new Handler(Looper.getMainLooper());
        this.turboConfigs = new HashMap<>();
        this.turboStates = new HashMap<>();
        initDefaultConfigs();
    }

    private void initDefaultConfigs() {
        // Configuration par défaut pour les boutons principaux
        int[] defaultButtons = {0, 1, 8, 9}; // B, Y, A, X
        for (int button : defaultButtons) {
            TurboButtonConfig config = new TurboButtonConfig();
            config.enabled = false;
            config.speed = 10;
            turboConfigs.put(button, config);
            turboStates.put(button, false);
        }
    }

    public void setTurboEnabled(boolean enabled) {
        this.turboEnabled = enabled;
        if (!enabled) {
            // Arrêter tous les turbos actifs
            for (Integer buttonId : turboStates.keySet()) {
                stopTurbo(buttonId);
            }
        }
    }

    public boolean isTurboEnabled() {
        return turboEnabled;
    }

    public void setTurboButton(int buttonId, boolean enabled) {
        TurboButtonConfig config = turboConfigs.get(buttonId);
        if (config != null) {
            config.enabled = enabled;
            if (!enabled) {
                stopTurbo(buttonId);
            }
        }
    }

    public void setTurboSpeed(int buttonId, int speed) {
        TurboButtonConfig config = turboConfigs.get(buttonId);
        if (config != null) {
            config.speed = Math.max(1, speed);
        }
    }

    public boolean isTurboActive(int buttonId) {
        return turboStates.getOrDefault(buttonId, false);
    }

    public void startTurbo(int buttonId) {
        if (!turboEnabled) return;
        
        TurboButtonConfig config = turboConfigs.get(buttonId);
        if (config != null && config.enabled) {
            config.active = true;
            turboStates.put(buttonId, true);
            scheduleTurboPulse(buttonId, config.speed);
        }
    }

    public void stopTurbo(int buttonId) {
        TurboButtonConfig config = turboConfigs.get(buttonId);
        if (config != null) {
            config.active = false;
        }
        turboStates.put(buttonId, false);
    }

    private void scheduleTurboPulse(int buttonId, int speed) {
        handler.postDelayed(() -> {
            if (isTurboActive(buttonId)) {
                pulseButton(buttonId);
                scheduleTurboPulse(buttonId, speed);
            }
        }, speed * 16); // 16ms par frame (60 FPS)
    }

    private void pulseButton(int buttonId) {
        // Simuler un appui rapide du bouton
        // Cette méthode sera appelée par InputManager
        Log.d(TAG, "Turbo pulse pour bouton: " + buttonId);
    }

    public void updateTurboStates() {
        // Mettre à jour les états des boutons turbo
        // Appelé régulièrement par InputManager
    }
}
