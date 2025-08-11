# Script pour recréer les fichiers Java avec le bon encodage
Write-Host "Recréation des fichiers Java avec encodage UTF-8 sans BOM..." -ForegroundColor Yellow

# Supprimer les fichiers existants
$javaFiles = @(
    "app/src/main/java/com/fceumm/wrapper/input/GamepadDetectionManager.java",
    "app/src/main/java/com/fceumm/wrapper/input/InputRemappingSystem.java", 
    "app/src/main/java/com/fceumm/wrapper/input/TurboButtonSystem.java",
    "app/src/main/java/com/fceumm/wrapper/input/HapticFeedbackManager.java",
    "app/src/main/java/com/fceumm/wrapper/input/InputManager.java"
)

foreach ($file in $javaFiles) {
    if (Test-Path $file) {
        Remove-Item $file -Force
        Write-Host "Supprime: $file" -ForegroundColor Red
    }
}

# Recréer GamepadDetectionManager.java
$content = @'
package com.fceumm.wrapper.input;

import android.content.Context;
import android.hardware.input.InputManager;
import android.view.InputDevice;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

public class GamepadDetectionManager {
    private static final String TAG = "GamepadDetectionManager";
    private Context context;
    private InputManager inputManager;
    private List<InputDevice> connectedGamepads;
    private GamepadDetectionListener listener;

    public interface GamepadDetectionListener {
        void onGamepadConnected(InputDevice device);
        void onGamepadDisconnected(InputDevice device);
        void onGamepadConfigurationChanged(InputDevice device);
    }

    public GamepadDetectionManager(Context context) {
        this.context = context;
        this.inputManager = (InputManager) context.getSystemService(Context.INPUT_SERVICE);
        this.connectedGamepads = new ArrayList<>();
        scanForGamepads();
    }

    public void setListener(GamepadDetectionListener listener) {
        this.listener = listener;
    }

    public void scanForGamepads() {
        connectedGamepads.clear();
        int[] deviceIds = inputManager.getInputDeviceIds();
        
        for (int deviceId : deviceIds) {
            InputDevice device = inputManager.getInputDevice(deviceId);
            if (device != null && isGamepad(device)) {
                connectedGamepads.add(device);
                if (listener != null) {
                    listener.onGamepadConnected(device);
                }
            }
        }
    }

    private boolean isGamepad(InputDevice device) {
        if (device == null) return false;
        
        // Vérifier les sources d'entrée
        int sources = device.getSources();
        return (sources & InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD ||
               (sources & InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK;
    }

    public List<InputDevice> getConnectedGamepads() {
        return new ArrayList<>(connectedGamepads);
    }

    public InputDevice getGamepadById(int deviceId) {
        for (InputDevice device : connectedGamepads) {
            if (device.getId() == deviceId) {
                return device;
            }
        }
        return null;
    }

    public void refreshGamepads() {
        scanForGamepads();
    }
}
'@

$content | Out-File -FilePath "app/src/main/java/com/fceumm/wrapper/input/GamepadDetectionManager.java" -Encoding UTF8 -NoNewline
Write-Host "Recree: GamepadDetectionManager.java" -ForegroundColor Green

# Recréer InputRemappingSystem.java
$content = @'
package com.fceumm.wrapper.input;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import java.util.HashMap;
import java.util.Map;

public class InputRemappingSystem {
    private static final String TAG = "InputRemappingSystem";
    private static final String PREFS_NAME = "input_remapping";
    
    // RetroArch constants
    public static final int RETRO_DEVICE_ID_JOYPAD_B = 0;
    public static final int RETRO_DEVICE_ID_JOYPAD_Y = 1;
    public static final int RETRO_DEVICE_ID_JOYPAD_SELECT = 2;
    public static final int RETRO_DEVICE_ID_JOYPAD_START = 3;
    public static final int RETRO_DEVICE_ID_JOYPAD_UP = 4;
    public static final int RETRO_DEVICE_ID_JOYPAD_DOWN = 5;
    public static final int RETRO_DEVICE_ID_JOYPAD_LEFT = 6;
    public static final int RETRO_DEVICE_ID_JOYPAD_RIGHT = 7;
    public static final int RETRO_DEVICE_ID_JOYPAD_A = 8;
    public static final int RETRO_DEVICE_ID_JOYPAD_X = 9;
    public static final int RETRO_DEVICE_ID_JOYPAD_L = 10;
    public static final int RETRO_DEVICE_ID_JOYPAD_R = 11;
    public static final int RETRO_DEVICE_ID_JOYPAD_L2 = 12;
    public static final int RETRO_DEVICE_ID_JOYPAD_R2 = 13;
    public static final int RETRO_DEVICE_ID_JOYPAD_L3 = 14;
    public static final int RETRO_DEVICE_ID_JOYPAD_R3 = 15;

    private Context context;
    private SharedPreferences prefs;
    private Map<Integer, Integer> remappingMap;
    private boolean remappingEnabled = true;

    public InputRemappingSystem(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.remappingMap = new HashMap<>();
        loadRemappingConfig();
    }

    public void setRemappingEnabled(boolean enabled) {
        this.remappingEnabled = enabled;
    }

    public boolean isRemappingEnabled() {
        return remappingEnabled;
    }

    public void setRemapping(int originalButton, int newButton) {
        if (remappingEnabled) {
            remappingMap.put(originalButton, newButton);
            saveRemappingConfig();
        }
    }

    public int getRemappedButton(int originalButton) {
        if (!remappingEnabled) {
            return originalButton;
        }
        return remappingMap.getOrDefault(originalButton, originalButton);
    }

    public void clearRemapping(int button) {
        remappingMap.remove(button);
        saveRemappingConfig();
    }

    public void clearAllRemappings() {
        remappingMap.clear();
        saveRemappingConfig();
    }

    private void loadRemappingConfig() {
        for (int i = 0; i < 16; i++) {
            int remappedButton = prefs.getInt("remap_" + i, i);
            if (remappedButton != i) {
                remappingMap.put(i, remappedButton);
            }
        }
    }

    private void saveRemappingConfig() {
        SharedPreferences.Editor editor = prefs.edit();
        for (Map.Entry<Integer, Integer> entry : remappingMap.entrySet()) {
            editor.putInt("remap_" + entry.getKey(), entry.getValue());
        }
        editor.apply();
    }

    private String getButtonName(int buttonId) {
        switch (buttonId) {
            case RETRO_DEVICE_ID_JOYPAD_B: return "B";
            case RETRO_DEVICE_ID_JOYPAD_Y: return "Y";
            case RETRO_DEVICE_ID_JOYPAD_SELECT: return "SELECT";
            case RETRO_DEVICE_ID_JOYPAD_START: return "START";
            case RETRO_DEVICE_ID_JOYPAD_UP: return "UP";
            case RETRO_DEVICE_ID_JOYPAD_DOWN: return "DOWN";
            case RETRO_DEVICE_ID_JOYPAD_LEFT: return "LEFT";
            case RETRO_DEVICE_ID_JOYPAD_RIGHT: return "RIGHT";
            case RETRO_DEVICE_ID_JOYPAD_A: return "A";
            case RETRO_DEVICE_ID_JOYPAD_X: return "X";
            case RETRO_DEVICE_ID_JOYPAD_L: return "L";
            case RETRO_DEVICE_ID_JOYPAD_R: return "R";
            case RETRO_DEVICE_ID_JOYPAD_L2: return "L2";
            case RETRO_DEVICE_ID_JOYPAD_R2: return "R2";
            case RETRO_DEVICE_ID_JOYPAD_L3: return "L3";
            case RETRO_DEVICE_ID_JOYPAD_R3: return "R3";
            default: return "UNKNOWN";
        }
    }
}
'@

$content | Out-File -FilePath "app/src/main/java/com/fceumm/wrapper/input/InputRemappingSystem.java" -Encoding UTF8 -NoNewline
Write-Host "Recree: InputRemappingSystem.java" -ForegroundColor Green

# Recréer TurboButtonSystem.java
$content = @'
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
'@

$content | Out-File -FilePath "app/src/main/java/com/fceumm/wrapper/input/TurboButtonSystem.java" -Encoding UTF8 -NoNewline
Write-Host "Recree: TurboButtonSystem.java" -ForegroundColor Green

# Recréer HapticFeedbackManager.java
$content = @'
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
'@

$content | Out-File -FilePath "app/src/main/java/com/fceumm/wrapper/input/HapticFeedbackManager.java" -Encoding UTF8 -NoNewline
Write-Host "Recree: HapticFeedbackManager.java" -ForegroundColor Green

# Recréer InputManager.java
$content = @'
package com.fceumm.wrapper.input;

import android.content.Context;
import android.util.Log;
import android.view.InputDevice;
import java.util.List;

public class InputManager {
    private static final String TAG = "InputManager";
    private Context context;
    private GamepadDetectionManager gamepadDetection;
    private InputRemappingSystem remappingSystem;
    private TurboButtonSystem turboSystem;
    private HapticFeedbackManager hapticManager;

    public InputManager(Context context) {
        this.context = context;
        initSystems();
    }

    private void initSystems() {
        gamepadDetection = new GamepadDetectionManager(context);
        remappingSystem = new InputRemappingSystem(context);
        turboSystem = new TurboButtonSystem();
        hapticManager = new HapticFeedbackManager(context);

        // Configuration automatique des gamepads
        gamepadDetection.setListener(new GamepadDetectionManager.GamepadDetectionListener() {
            @Override
            public void onGamepadConnected(InputDevice device) {
                autoConfigureGamepad(device);
            }

            @Override
            public void onGamepadDisconnected(InputDevice device) {
                Log.d(TAG, "Gamepad deconnecte: " + device.getName());
            }

            @Override
            public void onGamepadConfigurationChanged(InputDevice device) {
                Log.d(TAG, "Configuration gamepad changee: " + device.getName());
            }
        });
    }

    private void autoConfigureGamepad(InputDevice device) {
        String deviceName = device.getName().toLowerCase();
        Log.d(TAG, "Configuration automatique pour: " + device.getName());

        if (deviceName.contains("xbox") || deviceName.contains("microsoft")) {
            configureXboxGamepad();
        } else if (deviceName.contains("playstation") || deviceName.contains("sony")) {
            configurePlayStationGamepad();
        } else {
            configureGenericGamepad();
        }
    }

    private void configureXboxGamepad() {
        // Configuration spécifique Xbox
        Log.d(TAG, "Configuration Xbox detectee");
    }

    private void configurePlayStationGamepad() {
        // Configuration spécifique PlayStation
        Log.d(TAG, "Configuration PlayStation detectee");
    }

    private void configureGenericGamepad() {
        // Configuration générique
        Log.d(TAG, "Configuration generique appliquee");
    }

    public void processInput(int buttonId, boolean pressed) {
        if (pressed) {
            handleNormalInput(buttonId, true);
            handleTurboInput(buttonId);
            hapticManager.triggerHapticFeedback(buttonId);
        } else {
            handleNormalInput(buttonId, false);
        }
    }

    private void handleNormalInput(int buttonId, boolean pressed) {
        // Appliquer le remapping
        int remappedButton = remappingSystem.getRemappedButton(buttonId);
        
        // Envoyer au core
        sendInputToCore(remappedButton, pressed);
    }

    private void handleTurboInput(int buttonId) {
        if (turboSystem.isTurboEnabled()) {
            turboSystem.startTurbo(buttonId);
        }
    }

    private void sendInputToCore(int buttonId, boolean pressed) {
        // Appel natif vers le core libretro
        nativeSendInput(buttonId, pressed);
    }

    private native void nativeSendInput(int buttonId, boolean pressed);

    public GamepadDetectionManager getGamepadDetection() {
        return gamepadDetection;
    }

    public InputRemappingSystem getRemappingSystem() {
        return remappingSystem;
    }

    public TurboButtonSystem getTurboSystem() {
        return turboSystem;
    }

    public HapticFeedbackManager getHapticManager() {
        return hapticManager;
    }

    public void update() {
        turboSystem.updateTurboStates();
    }

    public void cleanup() {
        hapticManager.cancelHapticFeedback();
    }
}
'@

$content | Out-File -FilePath "app/src/main/java/com/fceumm/wrapper/input/InputManager.java" -Encoding UTF8 -NoNewline
Write-Host "Recree: InputManager.java" -ForegroundColor Green

Write-Host "Recréation terminee!" -ForegroundColor Green
Write-Host "Vous pouvez maintenant compiler le projet avec: ./gradlew clean assembleDebug installDebug" -ForegroundColor Yellow

