package com.fceumm.wrapper.input;

import android.util.Log;
import java.util.HashMap;
import java.util.Map;

/**
 * Gestionnaire de ports et périphériques basé sur RetroArch
 * Gère la configuration des ports, la détection automatique et le mapping des périphériques
 */
public class InputPortManager {
    private static final String TAG = "InputPortManager";
    
    // Configuration des ports (basée sur RetroArch)
    public static final int MAX_PORTS = 4;
    public static final int PORT_AUTO = -1;
    
    // Types de périphériques (basés sur RetroArch)
    public enum DeviceType {
        NONE(0),
        JOYPAD(1),
        MOUSE(2),
        KEYBOARD(3),
        LIGHTGUN(4),
        ANALOG(5),
        POINTER(6);
        
        private final int value;
        
        DeviceType(int value) {
            this.value = value;
        }
        
        public int getValue() {
            return value;
        }
    }
    
    // Configuration d'un port
    public static class PortConfig {
        public int portIndex;
        public DeviceType deviceType;
        public String deviceName;
        public String deviceDriver;
        public boolean autoConfigured;
        public int vid; // Vendor ID
        public int pid; // Product ID
        
        public PortConfig(int portIndex) {
            this.portIndex = portIndex;
            this.deviceType = DeviceType.NONE;
            this.deviceName = "";
            this.deviceDriver = "";
            this.autoConfigured = false;
            this.vid = 0;
            this.pid = 0;
        }
    }
    
    // Gestionnaire des ports
    private Map<Integer, PortConfig> portConfigs;
    private int currentPort = 0;
    
    public InputPortManager() {
        portConfigs = new HashMap<>();
        
        // Initialiser les ports par défaut
        for (int i = 0; i < MAX_PORTS; i++) {
            portConfigs.put(i, new PortConfig(i));
        }
        
        Log.i(TAG, "InputPortManager initialisé avec " + MAX_PORTS + " ports");
    }
    
    /**
     * Configure un port avec un périphérique spécifique
     * Basé sur input_config_set_device de RetroArch
     */
    public void setPortDevice(int port, DeviceType deviceType, String deviceName) {
        if (port < 0 || port >= MAX_PORTS) {
            Log.e(TAG, "Port invalide: " + port);
            return;
        }
        
        PortConfig config = portConfigs.get(port);
        if (config != null) {
            config.deviceType = deviceType;
            config.deviceName = deviceName;
            config.autoConfigured = false;
            
            Log.i(TAG, "Port " + port + " configuré: " + deviceType + " - " + deviceName);
        }
    }
    
    /**
     * Configure automatiquement un port (auto-détection)
     * Basé sur input_config_get_device de RetroArch
     */
    public void setPortAutoConfigure(int port) {
        if (port < 0 || port >= MAX_PORTS) {
            Log.e(TAG, "Port invalide: " + port);
            return;
        }
        
        PortConfig config = portConfigs.get(port);
        if (config != null) {
            config.deviceType = DeviceType.JOYPAD;
            config.deviceName = "Auto";
            config.autoConfigured = true;
            
            Log.i(TAG, "Port " + port + " configuré en auto-détection");
        }
    }
    
    /**
     * Obtient la configuration d'un port
     */
    public PortConfig getPortConfig(int port) {
        if (port < 0 || port >= MAX_PORTS) {
            return null;
        }
        return portConfigs.get(port);
    }
    
    /**
     * Définit le port actuel
     */
    public void setCurrentPort(int port) {
        if (port >= 0 && port < MAX_PORTS) {
            currentPort = port;
            Log.d(TAG, "Port actuel changé vers: " + port);
        }
    }
    
    /**
     * Obtient le port actuel
     */
    public int getCurrentPort() {
        return currentPort;
    }
    
    /**
     * Trouve un port libre pour un nouveau périphérique
     * Basé sur pad_connection_find_vacant_pad de RetroArch
     */
    public int findVacantPort() {
        for (int i = 0; i < MAX_PORTS; i++) {
            PortConfig config = portConfigs.get(i);
            if (config != null && config.deviceType == DeviceType.NONE) {
                return i;
            }
        }
        return -1; // Aucun port libre
    }
    
    /**
     * Configure un périphérique avec VID/PID (comme RetroArch)
     */
    public void setDeviceVIDPID(int port, int vid, int pid) {
        if (port < 0 || port >= MAX_PORTS) {
            return;
        }
        
        PortConfig config = portConfigs.get(port);
        if (config != null) {
            config.vid = vid;
            config.pid = pid;
            
            // Détection automatique basée sur VID/PID (comme RetroArch)
            if (vid == 0x057e && pid == 0x0330) {
                config.deviceName = "Nintendo Pro Controller";
                config.deviceType = DeviceType.JOYPAD;
            } else if (vid == 0x054c && (pid == 0x0268 || pid == 0x05c4)) {
                config.deviceName = "Sony DualShock";
                config.deviceType = DeviceType.JOYPAD;
            } else {
                config.deviceName = "Unknown Device";
                config.deviceType = DeviceType.JOYPAD;
            }
            
            Log.i(TAG, "Périphérique détecté sur port " + port + ": " + config.deviceName);
        }
    }
    
    /**
     * Réinitialise tous les ports
     */
    public void resetAllPorts() {
        for (int i = 0; i < MAX_PORTS; i++) {
            portConfigs.put(i, new PortConfig(i));
        }
        Log.i(TAG, "Tous les ports réinitialisés");
    }
    
    /**
     * Obtient le nom d'affichage d'un périphérique
     */
    public String getDeviceDisplayName(int port) {
        PortConfig config = getPortConfig(port);
        if (config != null) {
            if (config.autoConfigured) {
                return "Auto (Port " + port + ")";
            } else if (!config.deviceName.isEmpty()) {
                return config.deviceName + " (Port " + port + ")";
            } else {
                return "Aucun périphérique (Port " + port + ")";
            }
        }
        return "Port invalide";
    }
    
    /**
     * Vérifie si un port est actif
     */
    public boolean isPortActive(int port) {
        PortConfig config = getPortConfig(port);
        return config != null && config.deviceType != DeviceType.NONE;
    }
    
    /**
     * Obtient le type de périphérique d'un port
     */
    public DeviceType getPortDeviceType(int port) {
        PortConfig config = getPortConfig(port);
        return config != null ? config.deviceType : DeviceType.NONE;
    }
} 