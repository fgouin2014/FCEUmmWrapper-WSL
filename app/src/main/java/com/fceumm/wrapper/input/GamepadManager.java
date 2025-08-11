package com.fceumm.wrapper.input;

import android.content.Context;
import android.hardware.input.InputManager;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.util.Log;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

/**
 * **100% RETROARCH NATIF** : Gestionnaire de gamepad complet
 * Intègre tous les systèmes d'input pour une compatibilité RetroArch totale
 */
public class GamepadManager {
    private static final String TAG = "GamepadManager";
    
    // **100% RETROARCH** : Types de gamepads supportés
    public enum GamepadType {
        GAMEPAD_XBOX,
        GAMEPAD_PLAYSTATION,
        GAMEPAD_GENERIC,
        GAMEPAD_NINTENDO,
        GAMEPAD_UNKNOWN
    }
    
    // **100% RETROARCH** : États des boutons
    private Context context;
    private Map<Integer, Boolean> buttonStates;
    private Map<Integer, Float> axisStates;
    private Map<Integer, GamepadType> connectedGamepads;
    
    // **100% RETROARCH** : Systèmes intégrés
    private GamepadDetectionManager detectionManager;
    private InputRemappingSystem remappingSystem;
    private TurboButtonSystem turboSystem;
    private HapticFeedbackManager hapticManager;
    
    // **100% RETROARCH** : Configuration
    private boolean gamepadEnabled = true;
    private float analogDeadzone = 0.15f;
    private float analogSensitivity = 1.0f;
    
    // **100% RETROARCH** : Callbacks
    public interface GamepadCallback {
        void onGamepadConnected(InputDevice device, GamepadType type);
        void onGamepadDisconnected(InputDevice device);
        void onButtonPressed(int deviceId, int buttonId, boolean pressed);
        void onAxisChanged(int deviceId, int axisId, float value);
    }
    
    private GamepadCallback callback;
    
    public GamepadManager(Context context) {
        this.context = context;
        this.buttonStates = new HashMap<>();
        this.axisStates = new HashMap<>();
        this.connectedGamepads = new HashMap<>();
        
        initSystems();
        Log.i(TAG, "🎮 **100% RETROARCH** - GamepadManager initialisé");
    }
    
    private void initSystems() {
        // Initialiser la détection des gamepads
        detectionManager = new GamepadDetectionManager(context);
        detectionManager.setListener(new GamepadDetectionManager.GamepadDetectionListener() {
            @Override
            public void onGamepadConnected(InputDevice device) {
                handleGamepadConnected(device);
            }
            
            @Override
            public void onGamepadDisconnected(InputDevice device) {
                handleGamepadDisconnected(device);
            }
            
            @Override
            public void onGamepadConfigurationChanged(InputDevice device) {
                Log.i(TAG, "🎮 Configuration gamepad changée: " + device.getName());
            }
        });
        
        // Initialiser les autres systèmes
        remappingSystem = new InputRemappingSystem(context);
        turboSystem = new TurboButtonSystem();
        hapticManager = new HapticFeedbackManager(context);
        
        // Activer les systèmes
        turboSystem.setTurboEnabled(true);
        hapticManager.setHapticEnabled(true);
    }
    
    /**
     * **100% RETROARCH** : Gérer la connexion d'un gamepad
     */
    private void handleGamepadConnected(InputDevice device) {
        GamepadType type = detectGamepadType(device);
        connectedGamepads.put(device.getId(), type);
        
        // Auto-configurer le gamepad
        autoConfigureGamepad(device, type);
        
        Log.i(TAG, "🎮 **100% RETROARCH** - Gamepad connecté: " + device.getName() + " (Type: " + type + ")");
        
        if (callback != null) {
            callback.onGamepadConnected(device, type);
        }
    }
    
    /**
     * **100% RETROARCH** : Gérer la déconnexion d'un gamepad
     */
    private void handleGamepadDisconnected(InputDevice device) {
        connectedGamepads.remove(device.getId());
        
        // Nettoyer les états
        clearGamepadStates(device.getId());
        
        Log.i(TAG, "🎮 **100% RETROARCH** - Gamepad déconnecté: " + device.getName());
        
        if (callback != null) {
            callback.onGamepadDisconnected(device);
        }
    }
    
    /**
     * **100% RETROARCH** : Détecter le type de gamepad
     */
    private GamepadType detectGamepadType(InputDevice device) {
        String deviceName = device.getName().toLowerCase();
        String deviceDescriptor = device.getDescriptor().toLowerCase();
        
        if (deviceName.contains("xbox") || deviceName.contains("microsoft") || 
            deviceDescriptor.contains("xbox") || deviceDescriptor.contains("microsoft")) {
            return GamepadType.GAMEPAD_XBOX;
        } else if (deviceName.contains("playstation") || deviceName.contains("sony") || 
                   deviceName.contains("dualshock") || deviceName.contains("dualsense") ||
                   deviceDescriptor.contains("playstation") || deviceDescriptor.contains("sony")) {
            return GamepadType.GAMEPAD_PLAYSTATION;
        } else if (deviceName.contains("nintendo") || deviceName.contains("joy-con") ||
                   deviceName.contains("pro controller") || deviceDescriptor.contains("nintendo")) {
            return GamepadType.GAMEPAD_NINTENDO;
        } else {
            return GamepadType.GAMEPAD_GENERIC;
        }
    }
    
    /**
     * **100% RETROARCH** : Auto-configurer le gamepad
     */
    private void autoConfigureGamepad(InputDevice device, GamepadType type) {
        Log.i(TAG, "🎮 **100% RETROARCH** - Auto-configuration pour: " + type);
        
        switch (type) {
            case GAMEPAD_XBOX:
                configureXboxGamepad();
                break;
            case GAMEPAD_PLAYSTATION:
                configurePlayStationGamepad();
                break;
            case GAMEPAD_NINTENDO:
                configureNintendoGamepad();
                break;
            default:
                configureGenericGamepad();
                break;
        }
    }
    
    /**
     * **100% RETROARCH** : Configuration Xbox
     */
    private void configureXboxGamepad() {
        // Mapping Xbox standard vers RetroArch
        remappingSystem.setRemapping(InputRemappingSystem.RETRO_DEVICE_ID_JOYPAD_A, KeyEvent.KEYCODE_BUTTON_A);
        remappingSystem.setRemapping(InputRemappingSystem.RETRO_DEVICE_ID_JOYPAD_B, KeyEvent.KEYCODE_BUTTON_B);
        remappingSystem.setRemapping(InputRemappingSystem.RETRO_DEVICE_ID_JOYPAD_X, KeyEvent.KEYCODE_BUTTON_X);
        remappingSystem.setRemapping(InputRemappingSystem.RETRO_DEVICE_ID_JOYPAD_Y, KeyEvent.KEYCODE_BUTTON_Y);
        remappingSystem.setRemapping(InputRemappingSystem.RETRO_DEVICE_ID_JOYPAD_L, KeyEvent.KEYCODE_BUTTON_L1);
        remappingSystem.setRemapping(InputRemappingSystem.RETRO_DEVICE_ID_JOYPAD_R, KeyEvent.KEYCODE_BUTTON_R1);
        remappingSystem.setRemapping(InputRemappingSystem.RETRO_DEVICE_ID_JOYPAD_L2, KeyEvent.KEYCODE_BUTTON_L2);
        remappingSystem.setRemapping(InputRemappingSystem.RETRO_DEVICE_ID_JOYPAD_R2, KeyEvent.KEYCODE_BUTTON_R2);
        remappingSystem.setRemapping(InputRemappingSystem.RETRO_DEVICE_ID_JOYPAD_START, KeyEvent.KEYCODE_BUTTON_START);
        remappingSystem.setRemapping(InputRemappingSystem.RETRO_DEVICE_ID_JOYPAD_SELECT, KeyEvent.KEYCODE_BUTTON_SELECT);
        
        Log.i(TAG, "🎮 **100% RETROARCH** - Configuration Xbox appliquée");
    }
    
    /**
     * **100% RETROARCH** : Configuration PlayStation
     */
    private void configurePlayStationGamepad() {
        // Mapping PlayStation standard vers RetroArch
        remappingSystem.setRemapping(InputRemappingSystem.RETRO_DEVICE_ID_JOYPAD_A, KeyEvent.KEYCODE_BUTTON_A);
        remappingSystem.setRemapping(InputRemappingSystem.RETRO_DEVICE_ID_JOYPAD_B, KeyEvent.KEYCODE_BUTTON_B);
        remappingSystem.setRemapping(InputRemappingSystem.RETRO_DEVICE_ID_JOYPAD_X, KeyEvent.KEYCODE_BUTTON_X);
        remappingSystem.setRemapping(InputRemappingSystem.RETRO_DEVICE_ID_JOYPAD_Y, KeyEvent.KEYCODE_BUTTON_Y);
        remappingSystem.setRemapping(InputRemappingSystem.RETRO_DEVICE_ID_JOYPAD_L, KeyEvent.KEYCODE_BUTTON_L1);
        remappingSystem.setRemapping(InputRemappingSystem.RETRO_DEVICE_ID_JOYPAD_R, KeyEvent.KEYCODE_BUTTON_R1);
        remappingSystem.setRemapping(InputRemappingSystem.RETRO_DEVICE_ID_JOYPAD_L2, KeyEvent.KEYCODE_BUTTON_L2);
        remappingSystem.setRemapping(InputRemappingSystem.RETRO_DEVICE_ID_JOYPAD_R2, KeyEvent.KEYCODE_BUTTON_R2);
        remappingSystem.setRemapping(InputRemappingSystem.RETRO_DEVICE_ID_JOYPAD_START, KeyEvent.KEYCODE_BUTTON_START);
        remappingSystem.setRemapping(InputRemappingSystem.RETRO_DEVICE_ID_JOYPAD_SELECT, KeyEvent.KEYCODE_BUTTON_SELECT);
        
        Log.i(TAG, "🎮 **100% RETROARCH** - Configuration PlayStation appliquée");
    }
    
    /**
     * **100% RETROARCH** : Configuration Nintendo
     */
    private void configureNintendoGamepad() {
        // Mapping Nintendo standard vers RetroArch
        remappingSystem.setRemapping(InputRemappingSystem.RETRO_DEVICE_ID_JOYPAD_A, KeyEvent.KEYCODE_BUTTON_A);
        remappingSystem.setRemapping(InputRemappingSystem.RETRO_DEVICE_ID_JOYPAD_B, KeyEvent.KEYCODE_BUTTON_B);
        remappingSystem.setRemapping(InputRemappingSystem.RETRO_DEVICE_ID_JOYPAD_X, KeyEvent.KEYCODE_BUTTON_X);
        remappingSystem.setRemapping(InputRemappingSystem.RETRO_DEVICE_ID_JOYPAD_Y, KeyEvent.KEYCODE_BUTTON_Y);
        remappingSystem.setRemapping(InputRemappingSystem.RETRO_DEVICE_ID_JOYPAD_L, KeyEvent.KEYCODE_BUTTON_L1);
        remappingSystem.setRemapping(InputRemappingSystem.RETRO_DEVICE_ID_JOYPAD_R, KeyEvent.KEYCODE_BUTTON_R1);
        remappingSystem.setRemapping(InputRemappingSystem.RETRO_DEVICE_ID_JOYPAD_L2, KeyEvent.KEYCODE_BUTTON_L2);
        remappingSystem.setRemapping(InputRemappingSystem.RETRO_DEVICE_ID_JOYPAD_R2, KeyEvent.KEYCODE_BUTTON_R2);
        remappingSystem.setRemapping(InputRemappingSystem.RETRO_DEVICE_ID_JOYPAD_START, KeyEvent.KEYCODE_BUTTON_START);
        remappingSystem.setRemapping(InputRemappingSystem.RETRO_DEVICE_ID_JOYPAD_SELECT, KeyEvent.KEYCODE_BUTTON_SELECT);
        
        Log.i(TAG, "🎮 **100% RETROARCH** - Configuration Nintendo appliquée");
    }
    
    /**
     * **100% RETROARCH** : Configuration générique
     */
    private void configureGenericGamepad() {
        // Mapping générique vers RetroArch
        remappingSystem.setRemapping(InputRemappingSystem.RETRO_DEVICE_ID_JOYPAD_A, KeyEvent.KEYCODE_BUTTON_A);
        remappingSystem.setRemapping(InputRemappingSystem.RETRO_DEVICE_ID_JOYPAD_B, KeyEvent.KEYCODE_BUTTON_B);
        remappingSystem.setRemapping(InputRemappingSystem.RETRO_DEVICE_ID_JOYPAD_X, KeyEvent.KEYCODE_BUTTON_X);
        remappingSystem.setRemapping(InputRemappingSystem.RETRO_DEVICE_ID_JOYPAD_Y, KeyEvent.KEYCODE_BUTTON_Y);
        remappingSystem.setRemapping(InputRemappingSystem.RETRO_DEVICE_ID_JOYPAD_L, KeyEvent.KEYCODE_BUTTON_L1);
        remappingSystem.setRemapping(InputRemappingSystem.RETRO_DEVICE_ID_JOYPAD_R, KeyEvent.KEYCODE_BUTTON_R1);
        remappingSystem.setRemapping(InputRemappingSystem.RETRO_DEVICE_ID_JOYPAD_L2, KeyEvent.KEYCODE_BUTTON_L2);
        remappingSystem.setRemapping(InputRemappingSystem.RETRO_DEVICE_ID_JOYPAD_R2, KeyEvent.KEYCODE_BUTTON_R2);
        remappingSystem.setRemapping(InputRemappingSystem.RETRO_DEVICE_ID_JOYPAD_START, KeyEvent.KEYCODE_BUTTON_START);
        remappingSystem.setRemapping(InputRemappingSystem.RETRO_DEVICE_ID_JOYPAD_SELECT, KeyEvent.KEYCODE_BUTTON_SELECT);
        
        Log.i(TAG, "🎮 **100% RETROARCH** - Configuration générique appliquée");
    }
    
    /**
     * **100% RETROARCH** : Traiter un événement de bouton
     */
    public boolean processKeyEvent(KeyEvent event) {
        if (!gamepadEnabled) return false;
        
        int deviceId = event.getDeviceId();
        int keyCode = event.getKeyCode();
        boolean pressed = event.getAction() == KeyEvent.ACTION_DOWN;
        
        // Vérifier si c'est un gamepad connecté
        if (!connectedGamepads.containsKey(deviceId)) {
            return false;
        }
        
        // Convertir le keyCode en ID RetroArch
        int retroButtonId = convertKeyCodeToRetroButton(keyCode);
        if (retroButtonId >= 0) {
            processButtonInput(deviceId, retroButtonId, pressed);
            return true;
        }
        
        return false;
    }
    
    /**
     * **100% RETROARCH** : Traiter un événement d'axe
     */
    public boolean processAxisEvent(MotionEvent event) {
        if (!gamepadEnabled) return false;
        
        int deviceId = event.getDeviceId();
        
        // Vérifier si c'est un gamepad connecté
        if (!connectedGamepads.containsKey(deviceId)) {
            return false;
        }
        
        // Traiter les axes analogiques
        for (int i = 0; i < event.getPointerCount(); i++) {
            float x = event.getAxisValue(MotionEvent.AXIS_X, i);
            float y = event.getAxisValue(MotionEvent.AXIS_Y, i);
            float hatX = event.getAxisValue(MotionEvent.AXIS_HAT_X, i);
            float hatY = event.getAxisValue(MotionEvent.AXIS_HAT_Y, i);
            float lTrigger = event.getAxisValue(MotionEvent.AXIS_LTRIGGER, i);
            float rTrigger = event.getAxisValue(MotionEvent.AXIS_RTRIGGER, i);
            
            // Appliquer la zone morte et la sensibilité
            x = applyAnalogProcessing(x);
            y = applyAnalogProcessing(y);
            hatX = applyAnalogProcessing(hatX);
            hatY = applyAnalogProcessing(hatY);
            lTrigger = applyAnalogProcessing(lTrigger);
            rTrigger = applyAnalogProcessing(rTrigger);
            
            // Traiter les axes
            processAxisInput(deviceId, MotionEvent.AXIS_X, x);
            processAxisInput(deviceId, MotionEvent.AXIS_Y, y);
            processAxisInput(deviceId, MotionEvent.AXIS_HAT_X, hatX);
            processAxisInput(deviceId, MotionEvent.AXIS_HAT_Y, hatY);
            processAxisInput(deviceId, MotionEvent.AXIS_LTRIGGER, lTrigger);
            processAxisInput(deviceId, MotionEvent.AXIS_RTRIGGER, rTrigger);
        }
        
        return true;
    }
    
    /**
     * **100% RETROARCH** : Appliquer le traitement analogique
     */
    private float applyAnalogProcessing(float value) {
        // Appliquer la sensibilité
        value *= analogSensitivity;
        
        // Appliquer la zone morte
        if (Math.abs(value) < analogDeadzone) {
            value = 0.0f;
        }
        
        // Limiter à [-1, 1]
        return Math.max(-1.0f, Math.min(1.0f, value));
    }
    
    /**
     * **100% RETROARCH** : Convertir un keyCode en bouton RetroArch
     */
    private int convertKeyCodeToRetroButton(int keyCode) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BUTTON_A:
                return InputRemappingSystem.RETRO_DEVICE_ID_JOYPAD_A;
            case KeyEvent.KEYCODE_BUTTON_B:
                return InputRemappingSystem.RETRO_DEVICE_ID_JOYPAD_B;
            case KeyEvent.KEYCODE_BUTTON_X:
                return InputRemappingSystem.RETRO_DEVICE_ID_JOYPAD_X;
            case KeyEvent.KEYCODE_BUTTON_Y:
                return InputRemappingSystem.RETRO_DEVICE_ID_JOYPAD_Y;
            case KeyEvent.KEYCODE_BUTTON_L1:
                return InputRemappingSystem.RETRO_DEVICE_ID_JOYPAD_L;
            case KeyEvent.KEYCODE_BUTTON_R1:
                return InputRemappingSystem.RETRO_DEVICE_ID_JOYPAD_R;
            case KeyEvent.KEYCODE_BUTTON_L2:
                return InputRemappingSystem.RETRO_DEVICE_ID_JOYPAD_L2;
            case KeyEvent.KEYCODE_BUTTON_R2:
                return InputRemappingSystem.RETRO_DEVICE_ID_JOYPAD_R2;
            case KeyEvent.KEYCODE_BUTTON_START:
                return InputRemappingSystem.RETRO_DEVICE_ID_JOYPAD_START;
            case KeyEvent.KEYCODE_BUTTON_SELECT:
                return InputRemappingSystem.RETRO_DEVICE_ID_JOYPAD_SELECT;
            case KeyEvent.KEYCODE_DPAD_UP:
                return InputRemappingSystem.RETRO_DEVICE_ID_JOYPAD_UP;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                return InputRemappingSystem.RETRO_DEVICE_ID_JOYPAD_DOWN;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                return InputRemappingSystem.RETRO_DEVICE_ID_JOYPAD_LEFT;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                return InputRemappingSystem.RETRO_DEVICE_ID_JOYPAD_RIGHT;
            default:
                return -1; // Bouton non reconnu
        }
    }
    
    /**
     * **100% RETROARCH** : Traiter l'input d'un bouton
     */
    private void processButtonInput(int deviceId, int buttonId, boolean pressed) {
        // Appliquer le remapping
        int remappedButton = remappingSystem.getRemappedButton(buttonId);
        
        // Mettre à jour l'état
        int stateKey = deviceId * 1000 + remappedButton;
        buttonStates.put(stateKey, pressed);
        
        // Traiter le turbo
        if (pressed && turboSystem.isTurboEnabled()) {
            turboSystem.startTurbo(remappedButton);
        } else if (!pressed) {
            turboSystem.stopTurbo(remappedButton);
        }
        
        // Feedback haptique
        if (pressed) {
            hapticManager.triggerHapticFeedback(remappedButton);
        }
        
        Log.d(TAG, "🎮 **100% RETROARCH** - Bouton: " + remappedButton + " " + (pressed ? "PRESSÉ" : "RELÂCHÉ"));
        
        if (callback != null) {
            callback.onButtonPressed(deviceId, remappedButton, pressed);
        }
    }
    
    /**
     * **100% RETROARCH** : Traiter l'input d'un axe
     */
    private void processAxisInput(int deviceId, int axisId, float value) {
        int stateKey = deviceId * 1000 + axisId;
        axisStates.put(stateKey, value);
        
        Log.d(TAG, "🎮 **100% RETROARCH** - Axe: " + axisId + " = " + value);
        
        if (callback != null) {
            callback.onAxisChanged(deviceId, axisId, value);
        }
    }
    
    /**
     * **100% RETROARCH** : Nettoyer les états d'un gamepad
     */
    private void clearGamepadStates(int deviceId) {
        // Nettoyer les états des boutons
        List<Integer> keysToRemove = new ArrayList<>();
        for (Integer key : buttonStates.keySet()) {
            if (key / 1000 == deviceId) {
                keysToRemove.add(key);
            }
        }
        for (Integer key : keysToRemove) {
            buttonStates.remove(key);
        }
        
        // Nettoyer les états des axes
        keysToRemove.clear();
        for (Integer key : axisStates.keySet()) {
            if (key / 1000 == deviceId) {
                keysToRemove.add(key);
            }
        }
        for (Integer key : keysToRemove) {
            axisStates.remove(key);
        }
    }
    
    /**
     * **100% RETROARCH** : Définir le callback
     */
    public void setGamepadCallback(GamepadCallback callback) {
        this.callback = callback;
    }
    
    /**
     * **100% RETROARCH** : Activer/désactiver les gamepads
     */
    public void setGamepadEnabled(boolean enabled) {
        this.gamepadEnabled = enabled;
        Log.i(TAG, "🎮 **100% RETROARCH** - Gamepads " + (enabled ? "activés" : "désactivés"));
    }
    
    /**
     * **100% RETROARCH** : Définir la zone morte analogique
     */
    public void setAnalogDeadzone(float deadzone) {
        this.analogDeadzone = Math.max(0.0f, Math.min(1.0f, deadzone));
        Log.i(TAG, "🎮 **100% RETROARCH** - Zone morte analogique: " + this.analogDeadzone);
    }
    
    /**
     * **100% RETROARCH** : Définir la sensibilité analogique
     */
    public void setAnalogSensitivity(float sensitivity) {
        this.analogSensitivity = Math.max(0.1f, Math.min(5.0f, sensitivity));
        Log.i(TAG, "🎮 **100% RETROARCH** - Sensibilité analogique: " + this.analogSensitivity);
    }
    
    /**
     * **100% RETROARCH** : Obtenir les gamepads connectés
     */
    public List<InputDevice> getConnectedGamepads() {
        return detectionManager.getConnectedGamepads();
    }
    
    /**
     * **100% RETROARCH** : Obtenir le type d'un gamepad
     */
    public GamepadType getGamepadType(int deviceId) {
        return connectedGamepads.getOrDefault(deviceId, GamepadType.GAMEPAD_UNKNOWN);
    }
    
    /**
     * **100% RETROARCH** : Vérifier si un bouton est pressé
     */
    public boolean isButtonPressed(int deviceId, int buttonId) {
        int stateKey = deviceId * 1000 + buttonId;
        return buttonStates.getOrDefault(stateKey, false);
    }
    
    /**
     * **100% RETROARCH** : Obtenir la valeur d'un axe
     */
    public float getAxisValue(int deviceId, int axisId) {
        int stateKey = deviceId * 1000 + axisId;
        return axisStates.getOrDefault(stateKey, 0.0f);
    }
    
    /**
     * **100% RETROARCH** : Mettre à jour les systèmes
     */
    public void update() {
        turboSystem.updateTurboStates();
    }
    
    /**
     * **100% RETROARCH** : Nettoyer les ressources
     */
    public void cleanup() {
        buttonStates.clear();
        axisStates.clear();
        connectedGamepads.clear();
        Log.i(TAG, "🎮 **100% RETROARCH** - GamepadManager nettoyé");
    }
}
