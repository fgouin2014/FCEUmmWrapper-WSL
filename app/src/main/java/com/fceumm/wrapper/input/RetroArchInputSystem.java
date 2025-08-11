package com.fceumm.wrapper.input;

import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;
import android.graphics.RectF;
import java.util.HashMap;
import java.util.Map;

/**
 * **100% RETROARCH NATIF** : Système d'input unifié conforme aux standards RetroArch
 * Remplace complètement le système d'input défaillant actuel
 */
public class RetroArchInputSystem {
    private static final String TAG = "RetroArchInputSystem";
    
    // **100% RETROARCH** : IDs des boutons conformes aux standards libretro
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
    
    // **100% RETROARCH** : Codes spéciaux pour les combinaisons
    public static final int RETRO_SPECIAL_MENU_TOGGLE = 100;
    public static final int RETRO_SPECIAL_QUICK_MENU = 101;
    public static final int RETRO_SPECIAL_SAVE_STATE = 102;
    public static final int RETRO_SPECIAL_LOAD_STATE = 103;
    public static final int RETRO_SPECIAL_FAST_FORWARD = 104;
    public static final int RETRO_SPECIAL_REWIND = 105;
    public static final int RETRO_SPECIAL_SCREENSHOT = 106;
    public static final int RETRO_SPECIAL_RESET = 107;
    
    // **100% RETROARCH** : État des boutons
    private boolean[] buttonStates = new boolean[16];
    private boolean[] specialStates = new boolean[8];
    
    // **100% RETROARCH** : Zones tactiles
    public static class TouchZone {
        public String name;
        public int deviceId;
        public RectF bounds;
        public boolean isPressed;
        public long pressTime;
        public float pressX, pressY;
        
        public TouchZone(String name, int deviceId, float x, float y, float width, float height) {
            this.name = name;
            this.deviceId = deviceId;
            this.bounds = new RectF(x, y, x + width, y + height);
            this.isPressed = false;
        }
        
        public boolean contains(float x, float y) {
            return bounds.contains(x, y);
        }
        
        public void press(float x, float y) {
            isPressed = true;
            pressTime = System.currentTimeMillis();
            pressX = x;
            pressY = y;
        }
        
        public void release() {
            isPressed = false;
        }
    }
    
    // **100% RETROARCH** : Zones tactiles par défaut pour NES
    private Map<String, TouchZone> touchZones = new HashMap<>();
    
    // **100% RETROARCH** : Callback pour les événements d'input
    public interface InputListener {
        void onButtonPressed(int deviceId);
        void onButtonReleased(int deviceId);
        void onSpecialPressed(int specialCode);
        void onSpecialReleased(int specialCode);
    }
    
    private InputListener inputListener;
    private Context context;
    
    // **100% RETROARCH** : Détection des combinaisons
    private boolean startPressed = false;
    private boolean selectPressed = false;
    private long lastComboTime = 0;
    private static final long COMBO_TIMEOUT = 200; // 200ms pour les combinaisons
    
    public RetroArchInputSystem(Context context) {
        this.context = context;
        initDefaultTouchZones();
        Log.i(TAG, "🎮 Système d'input RetroArch initialisé");
    }
    
    /**
     * **100% RETROARCH** : Initialiser les zones tactiles par défaut pour NES
     */
    private void initDefaultTouchZones() {
        // **ZONES NES PAR DÉFAUT** : Configuration optimale pour NES
        touchZones.clear();
        
        // **DPAD** - 4 directions séparées pour une détection précise
        touchZones.put("dpad_up", new TouchZone("dpad_up", RETRO_DEVICE_ID_JOYPAD_UP, 0.15f, 0.5f, 0.1f, 0.1f));
        touchZones.put("dpad_down", new TouchZone("dpad_down", RETRO_DEVICE_ID_JOYPAD_DOWN, 0.15f, 0.7f, 0.1f, 0.1f));
        touchZones.put("dpad_left", new TouchZone("dpad_left", RETRO_DEVICE_ID_JOYPAD_LEFT, 0.05f, 0.6f, 0.1f, 0.1f));
        touchZones.put("dpad_right", new TouchZone("dpad_right", RETRO_DEVICE_ID_JOYPAD_RIGHT, 0.25f, 0.6f, 0.1f, 0.1f));
        
        // **Boutons A et B** - Zone droite
        touchZones.put("a", new TouchZone("a", RETRO_DEVICE_ID_JOYPAD_A, 0.75f, 0.7f, 0.1f, 0.15f));
        touchZones.put("b", new TouchZone("b", RETRO_DEVICE_ID_JOYPAD_B, 0.65f, 0.7f, 0.1f, 0.15f));
        
        // **Boutons Start et Select** - Zone centrale
        touchZones.put("start", new TouchZone("start", RETRO_DEVICE_ID_JOYPAD_START, 0.45f, 0.8f, 0.1f, 0.1f));
        touchZones.put("select", new TouchZone("select", RETRO_DEVICE_ID_JOYPAD_SELECT, 0.35f, 0.8f, 0.1f, 0.1f));
        
        // **Zones spéciales pour les combinaisons**
        touchZones.put("menu_toggle", new TouchZone("menu_toggle", RETRO_SPECIAL_MENU_TOGGLE, 0.9f, 0.1f, 0.08f, 0.08f));
        touchZones.put("quick_menu", new TouchZone("quick_menu", RETRO_SPECIAL_QUICK_MENU, 0.9f, 0.2f, 0.08f, 0.08f));
        
        Log.i(TAG, "🎮 Zones tactiles NES initialisées: " + touchZones.size() + " zones");
    }
    
    /**
     * **100% RETROARCH** : Mettre à jour les zones tactiles selon l'orientation
     */
    public void updateTouchZonesForOrientation(boolean isLandscape) {
        if (isLandscape) {
            // **LANDSCAPE** : Écran plus large, zones plus espacées
            updateZone("dpad_up", 0.15f, 0.5f, 0.08f, 0.08f);
            updateZone("dpad_down", 0.15f, 0.7f, 0.08f, 0.08f);
            updateZone("dpad_left", 0.05f, 0.6f, 0.08f, 0.08f);
            updateZone("dpad_right", 0.25f, 0.6f, 0.08f, 0.08f);
            updateZone("a", 0.8f, 0.6f, 0.08f, 0.12f);
            updateZone("b", 0.7f, 0.6f, 0.08f, 0.12f);
            updateZone("start", 0.45f, 0.8f, 0.08f, 0.08f);
            updateZone("select", 0.35f, 0.8f, 0.08f, 0.08f);
        } else {
            // **PORTRAIT** : Écran plus haut, zones plus compactes
            updateZone("dpad_up", 0.15f, 0.5f, 0.1f, 0.1f);
            updateZone("dpad_down", 0.15f, 0.7f, 0.1f, 0.1f);
            updateZone("dpad_left", 0.05f, 0.6f, 0.1f, 0.1f);
            updateZone("dpad_right", 0.25f, 0.6f, 0.1f, 0.1f);
            updateZone("a", 0.75f, 0.7f, 0.1f, 0.15f);
            updateZone("b", 0.65f, 0.7f, 0.1f, 0.15f);
            updateZone("start", 0.45f, 0.8f, 0.1f, 0.1f);
            updateZone("select", 0.35f, 0.8f, 0.1f, 0.1f);
        }
        
        Log.i(TAG, "🎮 Zones tactiles mises à jour pour " + (isLandscape ? "landscape" : "portrait"));
    }
    
    private void updateZone(String name, float x, float y, float width, float height) {
        TouchZone zone = touchZones.get(name);
        if (zone != null) {
            zone.bounds.set(x, y, x + width, y + height);
        }
    }
    
    /**
     * **100% RETROARCH** : Gérer les événements tactiles
     */
    public boolean handleTouchEvent(MotionEvent event) {
        if (event.getPointerCount() == 0) return false;
        
        float x = event.getX(0) / context.getResources().getDisplayMetrics().widthPixels;
        float y = event.getY(0) / context.getResources().getDisplayMetrics().heightPixels;
        
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                return handleTouchDown(x, y, event.getPointerId(event.getActionIndex()));
                
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                return handleTouchUp(x, y, event.getPointerId(event.getActionIndex()));
                
            case MotionEvent.ACTION_MOVE:
                return handleTouchMove(x, y, event.getPointerId(event.getActionIndex()));
        }
        
        return false;
    }
    
    /**
     * **100% RETROARCH** : Gérer le toucher down
     */
    private boolean handleTouchDown(float x, float y, int pointerId) {
        Log.d(TAG, "👆 Touch DOWN: (" + x + ", " + y + ") - Pointer: " + pointerId);
        
        // **DÉTECTION DES ZONES** : Vérifier toutes les zones
        for (TouchZone zone : touchZones.values()) {
            if (zone.contains(x, y)) {
                zone.press(x, y);
                
                if (zone.deviceId >= 0 && zone.deviceId < 16) {
                    // **BOUTON NORMAL**
                    if (!buttonStates[zone.deviceId]) {
                        buttonStates[zone.deviceId] = true;
                        if (inputListener != null) {
                            inputListener.onButtonPressed(zone.deviceId);
                        }
                        Log.i(TAG, "🎮 Bouton pressé: " + zone.name + " (ID: " + zone.deviceId + ")");
                    }
                } else if (zone.deviceId >= 100) {
                    // **FONCTION SPÉCIALE**
                    int specialIndex = zone.deviceId - 100;
                    if (specialIndex >= 0 && specialIndex < specialStates.length && !specialStates[specialIndex]) {
                        specialStates[specialIndex] = true;
                        if (inputListener != null) {
                            inputListener.onSpecialPressed(zone.deviceId);
                        }
                        Log.i(TAG, "🎮 Fonction spéciale pressée: " + zone.name + " (Code: " + zone.deviceId + ")");
                    }
                }
                
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * **100% RETROARCH** : Gérer le toucher up
     */
    private boolean handleTouchUp(float x, float y, int pointerId) {
        Log.d(TAG, "👆 Touch UP: (" + x + ", " + y + ") - Pointer: " + pointerId);
        
        // **LIBÉRATION DES ZONES** : Vérifier toutes les zones
        for (TouchZone zone : touchZones.values()) {
            if (zone.isPressed) {
                zone.release();
                
                if (zone.deviceId >= 0 && zone.deviceId < 16) {
                    // **BOUTON NORMAL**
                    if (buttonStates[zone.deviceId]) {
                        buttonStates[zone.deviceId] = false;
                        if (inputListener != null) {
                            inputListener.onButtonReleased(zone.deviceId);
                        }
                        Log.i(TAG, "🎮 Bouton relâché: " + zone.name + " (ID: " + zone.deviceId + ")");
                    }
                } else if (zone.deviceId >= 100) {
                    // **FONCTION SPÉCIALE**
                    int specialIndex = zone.deviceId - 100;
                    if (specialIndex >= 0 && specialIndex < specialStates.length && specialStates[specialIndex]) {
                        specialStates[specialIndex] = false;
                        if (inputListener != null) {
                            inputListener.onSpecialReleased(zone.deviceId);
                        }
                        Log.i(TAG, "🎮 Fonction spéciale relâchée: " + zone.name + " (Code: " + zone.deviceId + ")");
                    }
                }
            }
        }
        
        return true;
    }
    
    /**
     * **100% RETROARCH** : Gérer le toucher move
     */
    private boolean handleTouchMove(float x, float y, int pointerId) {
        // **DÉTECTION DES COMBINAISONS** : Start + Select pour le menu
        checkStartSelectCombo();
        
        return false;
    }
    
    /**
     * **100% RETROARCH** : Vérifier la combinaison Start + Select
     */
    private void checkStartSelectCombo() {
        boolean startCurrentlyPressed = buttonStates[RETRO_DEVICE_ID_JOYPAD_START];
        boolean selectCurrentlyPressed = buttonStates[RETRO_DEVICE_ID_JOYPAD_SELECT];
        
        // **DÉTECTION DE LA COMBINAISON**
        if (startCurrentlyPressed && selectCurrentlyPressed) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastComboTime > COMBO_TIMEOUT) {
                lastComboTime = currentTime;
                
                // **ACTIVER LE MENU RETROARCH**
                if (inputListener != null) {
                    inputListener.onSpecialPressed(RETRO_SPECIAL_MENU_TOGGLE);
                    Log.i(TAG, "🎮 Combinaison Start + Select détectée - Menu RetroArch activé");
                }
            }
        }
    }
    
    /**
     * **100% RETROARCH** : Vérifier si un bouton est pressé
     */
    public boolean isButtonPressed(int deviceId) {
        if (deviceId >= 0 && deviceId < 16) {
            return buttonStates[deviceId];
        }
        return false;
    }
    
    /**
     * **100% RETROARCH** : Vérifier si une fonction spéciale est activée
     */
    public boolean isSpecialPressed(int specialCode) {
        int index = specialCode - 100;
        if (index >= 0 && index < specialStates.length) {
            return specialStates[index];
        }
        return false;
    }
    
    /**
     * **100% RETROARCH** : Réinitialiser tous les états
     */
    public void resetAllStates() {
        for (int i = 0; i < buttonStates.length; i++) {
            buttonStates[i] = false;
        }
        for (int i = 0; i < specialStates.length; i++) {
            specialStates[i] = false;
        }
        for (TouchZone zone : touchZones.values()) {
            zone.release();
        }
        startPressed = false;
        selectPressed = false;
        Log.i(TAG, "🎮 Tous les états d'input réinitialisés");
    }
    
    /**
     * **100% RETROARCH** : Définir le listener d'input
     */
    public void setInputListener(InputListener listener) {
        this.inputListener = listener;
    }
    
    /**
     * **100% RETROARCH** : Obtenir les zones tactiles pour le rendu
     */
    public Map<String, TouchZone> getTouchZones() {
        return touchZones;
    }
    
    /**
     * **100% RETROARCH** : Debug des zones tactiles
     */
    public void debugTouchZones() {
        Log.i(TAG, "🎮 === DEBUG ZONES TACTILES ===");
        for (TouchZone zone : touchZones.values()) {
            Log.i(TAG, "🎮 Zone: " + zone.name + 
                      " - Bounds: " + zone.bounds.toString() + 
                      " - Pressed: " + zone.isPressed + 
                      " - DeviceID: " + zone.deviceId);
        }
        Log.i(TAG, "🎮 === FIN DEBUG ===");
    }
}
