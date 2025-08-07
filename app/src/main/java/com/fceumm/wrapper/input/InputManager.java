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
