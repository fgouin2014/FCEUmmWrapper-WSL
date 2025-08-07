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
