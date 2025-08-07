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
