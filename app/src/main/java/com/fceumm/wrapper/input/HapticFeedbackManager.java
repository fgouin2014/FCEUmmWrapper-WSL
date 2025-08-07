package com.fceumm.wrapper.input;

import android.content.Context;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import java.util.HashMap;
import java.util.Map;

public class HapticFeedbackManager {
    private static final String TAG = "HapticFeedbackManager";
    private Context context;
    private Vibrator vibrator;
    private boolean hapticEnabled = true;
    private int hapticIntensity = 50; // 0-100
    private Map<Integer, Integer> buttonHapticPatterns;

    public HapticFeedbackManager(Context context) {
        this.context = context;
        this.vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        this.buttonHapticPatterns = new HashMap<>();
        initDefaultPatterns();
    }

    private void initDefaultPatterns() {
        // Patterns haptiques par défaut pour chaque bouton
        buttonHapticPatterns.put(0, 20); // B - court
        buttonHapticPatterns.put(1, 30); // Y - moyen
        buttonHapticPatterns.put(8, 25); // A - moyen-court
        buttonHapticPatterns.put(9, 35); // X - long
        buttonHapticPatterns.put(10, 40); // L - très long
        buttonHapticPatterns.put(11, 40); // R - très long
    }

    public void setHapticEnabled(boolean enabled) {
        this.hapticEnabled = enabled;
        if (!enabled) {
            cancelHapticFeedback();
        }
    }

    public boolean isHapticEnabled() {
        return hapticEnabled;
    }

    public void setHapticIntensity(int intensity) {
        this.hapticIntensity = Math.max(0, Math.min(100, intensity));
    }

    public int getHapticIntensity() {
        return hapticIntensity;
    }

    public void setButtonHapticPattern(int buttonId, int duration) {
        buttonHapticPatterns.put(buttonId, duration);
    }

    public void triggerHapticFeedback(int buttonId) {
        if (!hapticEnabled || vibrator == null) return;

        int duration = buttonHapticPatterns.getOrDefault(buttonId, 20);
        int intensity = (int) (hapticIntensity * 2.55); // Convertir 0-100 en 0-255

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            VibrationEffect effect = VibrationEffect.createOneShot(duration, intensity);
            vibrator.vibrate(effect);
        } else {
            vibrator.vibrate(duration);
        }
    }

    public void triggerCustomHapticPattern(long[] pattern, int[] amplitudes) {
        if (!hapticEnabled || vibrator == null) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            VibrationEffect effect = VibrationEffect.createWaveform(pattern, amplitudes, -1);
            vibrator.vibrate(effect);
        } else {
            vibrator.vibrate(pattern, -1);
        }
    }

    public void cancelHapticFeedback() {
        if (vibrator != null) {
            vibrator.cancel();
        }
    }
}
