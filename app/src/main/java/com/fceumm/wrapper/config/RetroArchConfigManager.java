package com.fceumm.wrapper.config;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Gestionnaire de configuration RetroArch exact
 * Gère les configurations globales, par core et par jeu
 */
public class RetroArchConfigManager {
    private static final String TAG = "RetroArchConfigManager";
    
    // Noms des fichiers de configuration
    private static final String GLOBAL_CONFIG_NAME = "retroarch_global";
    private static final String CORE_CONFIG_PREFIX = "core_";
    private static final String GAME_CONFIG_PREFIX = "game_";
    
    // Paramètres overlay RetroArch
    public static final String INPUT_OVERLAY_ENABLE = "input_overlay_enable";
    public static final String INPUT_OVERLAY_PATH = "input_overlay_path";
    public static final String INPUT_OVERLAY_SCALE = "input_overlay_scale";
    public static final String INPUT_OVERLAY_OPACITY = "input_overlay_opacity";
    public static final String INPUT_OVERLAY_SHOW_MOUSE_CURSOR = "input_overlay_show_mouse_cursor";
    public static final String INPUT_OVERLAY_AUTO_SCALE = "input_overlay_auto_scale";
    public static final String INPUT_OVERLAY_AUTO_ROTATE = "input_overlay_auto_rotate";
    public static final String INPUT_OVERLAY_SHOW_INPUTS = "input_overlay_show_inputs"; // **100% RETROARCH** : Debug des zones
    
    // Valeurs par défaut RetroArch
    private static final boolean DEFAULT_OVERLAY_ENABLE = true;
    private static final String DEFAULT_OVERLAY_PATH = "overlays/gamepads/flat/nes.cfg";
    private static final float DEFAULT_OVERLAY_SCALE = 1.5f;  // **CORRECTION CRITIQUE** : Scale plus élevé pour boutons plus gros
    private static final float DEFAULT_OVERLAY_OPACITY = 0.8f;
    private static final boolean DEFAULT_OVERLAY_SHOW_MOUSE_CURSOR = false;
    private static final boolean DEFAULT_OVERLAY_AUTO_SCALE = true;
    private static final boolean DEFAULT_OVERLAY_AUTO_ROTATE = true;
    private static final boolean DEFAULT_OVERLAY_SHOW_INPUTS = true; // **100% RETROARCH** : Debug activé par défaut
    
    private Context context;
    private SharedPreferences globalConfig;
    private String currentCore;
    private String currentGame;
    
    public RetroArchConfigManager(Context context) {
        this.context = context;
        this.globalConfig = context.getSharedPreferences(GLOBAL_CONFIG_NAME, Context.MODE_PRIVATE);
        initializeDefaultConfig();
    }
    
    /**
     * Initialiser la configuration par défaut
     */
    private void initializeDefaultConfig() {
        if (!globalConfig.contains(INPUT_OVERLAY_ENABLE)) {
            SharedPreferences.Editor editor = globalConfig.edit();
            editor.putBoolean(INPUT_OVERLAY_ENABLE, DEFAULT_OVERLAY_ENABLE);
            editor.putString(INPUT_OVERLAY_PATH, DEFAULT_OVERLAY_PATH);
            editor.putFloat(INPUT_OVERLAY_SCALE, DEFAULT_OVERLAY_SCALE);
            editor.putFloat(INPUT_OVERLAY_OPACITY, DEFAULT_OVERLAY_OPACITY);
            editor.putBoolean(INPUT_OVERLAY_SHOW_MOUSE_CURSOR, DEFAULT_OVERLAY_SHOW_MOUSE_CURSOR);
            editor.putBoolean(INPUT_OVERLAY_AUTO_SCALE, DEFAULT_OVERLAY_AUTO_SCALE);
            editor.putBoolean(INPUT_OVERLAY_AUTO_ROTATE, DEFAULT_OVERLAY_AUTO_ROTATE);
            editor.putBoolean(INPUT_OVERLAY_SHOW_INPUTS, DEFAULT_OVERLAY_SHOW_INPUTS); // **CRITIQUE** : Debug activé
            editor.apply();
            
            Log.i(TAG, "✅ Configuration RetroArch par défaut initialisée");
        }
        
        // **DIAGNOSTIC** : Vérifier que le debug est bien activé
        boolean debugEnabled = globalConfig.getBoolean(INPUT_OVERLAY_SHOW_INPUTS, DEFAULT_OVERLAY_SHOW_INPUTS);
        Log.i(TAG, "🔍 **DIAGNOSTIC** Debug des zones: " + debugEnabled + " (défaut: " + DEFAULT_OVERLAY_SHOW_INPUTS + ")");
    }
    
    /**
     * Définir le core actuel
     */
    public void setCurrentCore(String coreName) {
        this.currentCore = coreName;
        Log.d(TAG, "🎮 Core actuel défini: " + coreName);
    }
    
    /**
     * Définir le jeu actuel
     */
    public void setCurrentGame(String gameName) {
        this.currentGame = gameName;
        Log.d(TAG, "🎯 Jeu actuel défini: " + gameName);
    }
    
    /**
     * Obtenir la configuration avec hiérarchie RetroArch
     * Priorité: Jeu > Core > Global > Défaut
     */
    private SharedPreferences getConfigWithHierarchy() {
        // 1. Configuration par jeu (priorité max)
        if (currentGame != null) {
            String gameConfigName = GAME_CONFIG_PREFIX + sanitizeFileName(currentGame);
            SharedPreferences gameConfig = context.getSharedPreferences(gameConfigName, Context.MODE_PRIVATE);
            if (gameConfig.contains(INPUT_OVERLAY_ENABLE)) {
                Log.d(TAG, "📁 Utilisation configuration par jeu: " + gameConfigName);
                return gameConfig;
            }
        }
        
        // 2. Configuration par core
        if (currentCore != null) {
            String coreConfigName = CORE_CONFIG_PREFIX + sanitizeFileName(currentCore);
            SharedPreferences coreConfig = context.getSharedPreferences(coreConfigName, Context.MODE_PRIVATE);
            if (coreConfig.contains(INPUT_OVERLAY_ENABLE)) {
                Log.d(TAG, "⚙️ Utilisation configuration par core: " + coreConfigName);
                return coreConfig;
            }
        }
        
        // 3. Configuration globale
        Log.d(TAG, "🌐 Utilisation configuration globale");
        return globalConfig;
    }
    
    /**
     * Nettoyer le nom de fichier pour SharedPreferences
     */
    private String sanitizeFileName(String fileName) {
        return fileName.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
    
    /**
     * Obtenir le chemin de l'overlay avec hiérarchie
     */
    public String getOverlayPath() {
        SharedPreferences config = getConfigWithHierarchy();
        return config.getString(INPUT_OVERLAY_PATH, DEFAULT_OVERLAY_PATH);
    }
    
    /**
     * Obtenir l'échelle de l'overlay
     */
    public float getOverlayScale() {
        SharedPreferences config = getConfigWithHierarchy();
        return config.getFloat(INPUT_OVERLAY_SCALE, DEFAULT_OVERLAY_SCALE);
    }
    
    /**
     * Obtenir l'opacité de l'overlay
     */
    public float getOverlayOpacity() {
        SharedPreferences config = getConfigWithHierarchy();
        return config.getFloat(INPUT_OVERLAY_OPACITY, DEFAULT_OVERLAY_OPACITY);
    }
    
    /**
     * Vérifier si l'overlay est activé
     */
    public boolean isOverlayEnabled() {
        SharedPreferences config = getConfigWithHierarchy();
        return config.getBoolean(INPUT_OVERLAY_ENABLE, DEFAULT_OVERLAY_ENABLE);
    }
    
    /**
     * Vérifier si l'auto-scale est activé
     */
    public boolean isAutoScaleEnabled() {
        SharedPreferences config = getConfigWithHierarchy();
        return config.getBoolean(INPUT_OVERLAY_AUTO_SCALE, DEFAULT_OVERLAY_AUTO_SCALE);
    }
    
    /**
     * Vérifier si l'auto-rotate est activé
     */
    public boolean isAutoRotateEnabled() {
        SharedPreferences config = getConfigWithHierarchy();
        return config.getBoolean(INPUT_OVERLAY_AUTO_ROTATE, DEFAULT_OVERLAY_AUTO_ROTATE);
    }
    
    /**
     * Vérifier si l'affichage des inputs est activé (debug)
     */
    public boolean isOverlayShowInputsEnabled() {
        SharedPreferences config = getConfigWithHierarchy();
        boolean debugEnabled = config.getBoolean(INPUT_OVERLAY_SHOW_INPUTS, DEFAULT_OVERLAY_SHOW_INPUTS);
        Log.i(TAG, "🔍 **DIAGNOSTIC** isOverlayShowInputsEnabled() = " + debugEnabled + 
              " (défaut: " + DEFAULT_OVERLAY_SHOW_INPUTS + ")");
        return debugEnabled;
    }
    
    /**
     * Définir le chemin de l'overlay (configuration actuelle)
     */
    public void setOverlayPath(String path) {
        SharedPreferences config = getCurrentConfig();
        SharedPreferences.Editor editor = config.edit();
        editor.putString(INPUT_OVERLAY_PATH, path);
        editor.apply();
        Log.i(TAG, "🔧 Chemin overlay défini: " + path);
    }
    
    /**
     * Définir l'échelle de l'overlay
     */
    public void setOverlayScale(float scale) {
        SharedPreferences config = getCurrentConfig();
        SharedPreferences.Editor editor = config.edit();
        editor.putFloat(INPUT_OVERLAY_SCALE, scale);
        editor.apply();
        Log.i(TAG, "🔧 Échelle overlay définie: " + scale);
    }
    
    /**
     * Définir l'opacité de l'overlay
     */
    public void setOverlayOpacity(float opacity) {
        SharedPreferences config = getCurrentConfig();
        SharedPreferences.Editor editor = config.edit();
        editor.putFloat(INPUT_OVERLAY_OPACITY, opacity);
        editor.apply();
        Log.i(TAG, "🔧 Opacité overlay définie: " + opacity);
    }
    
    /**
     * Activer/désactiver l'overlay
     */
    public void setOverlayEnabled(boolean enabled) {
        SharedPreferences config = getCurrentConfig();
        SharedPreferences.Editor editor = config.edit();
        editor.putBoolean(INPUT_OVERLAY_ENABLE, enabled);
        editor.apply();
        Log.i(TAG, "🔧 Overlay " + (enabled ? "activé" : "désactivé"));
    }
    
    /**
     * Obtenir la configuration actuelle (pour les modifications)
     */
    private SharedPreferences getCurrentConfig() {
        if (currentGame != null) {
            String gameConfigName = GAME_CONFIG_PREFIX + sanitizeFileName(currentGame);
            return context.getSharedPreferences(gameConfigName, Context.MODE_PRIVATE);
        } else if (currentCore != null) {
            String coreConfigName = CORE_CONFIG_PREFIX + sanitizeFileName(currentCore);
            return context.getSharedPreferences(coreConfigName, Context.MODE_PRIVATE);
        } else {
            return globalConfig;
        }
    }
    
    /**
     * Charger la configuration depuis un fichier .cfg RetroArch
     */
    public void loadConfigFromFile(String configPath) {
        try {
            File configFile = new File(context.getFilesDir(), configPath);
            if (configFile.exists()) {
                Properties props = new Properties();
                FileInputStream fis = new FileInputStream(configFile);
                props.load(fis);
                fis.close();
                
                // Appliquer les paramètres overlay
                if (props.containsKey("input_overlay_enable")) {
                    setOverlayEnabled(Boolean.parseBoolean(props.getProperty("input_overlay_enable")));
                }
                if (props.containsKey("input_overlay_path")) {
                    setOverlayPath(props.getProperty("input_overlay_path"));
                }
                if (props.containsKey("input_overlay_scale")) {
                    setOverlayScale(Float.parseFloat(props.getProperty("input_overlay_scale")));
                }
                if (props.containsKey("input_overlay_opacity")) {
                    setOverlayOpacity(Float.parseFloat(props.getProperty("input_overlay_opacity")));
                }
                
                Log.i(TAG, "✅ Configuration chargée depuis: " + configPath);
            }
        } catch (IOException e) {
            Log.e(TAG, "❌ Erreur lors du chargement de la configuration: " + e.getMessage());
        }
    }
    
    /**
     * Sauvegarder la configuration dans un fichier .cfg
     */
    public void saveConfigToFile(String configPath) {
        try {
            File configFile = new File(context.getFilesDir(), configPath);
            Properties props = new Properties();
            
            // Paramètres overlay actuels
            props.setProperty("input_overlay_enable", String.valueOf(isOverlayEnabled()));
            props.setProperty("input_overlay_path", getOverlayPath());
            props.setProperty("input_overlay_scale", String.valueOf(getOverlayScale()));
            props.setProperty("input_overlay_opacity", String.valueOf(getOverlayOpacity()));
            props.setProperty("input_overlay_auto_scale", String.valueOf(isAutoScaleEnabled()));
            props.setProperty("input_overlay_auto_rotate", String.valueOf(isAutoRotateEnabled()));
            
            FileOutputStream fos = new FileOutputStream(configFile);
            props.store(fos, "RetroArch Configuration");
            fos.close();
            
            Log.i(TAG, "✅ Configuration sauvegardée dans: " + configPath);
        } catch (IOException e) {
            Log.e(TAG, "❌ Erreur lors de la sauvegarde de la configuration: " + e.getMessage());
        }
    }
    
    /**
     * Obtenir un résumé de la configuration actuelle
     */
    public String getConfigSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("🎮 Configuration RetroArch:\n");
        summary.append("  Core: ").append(currentCore != null ? currentCore : "Aucun").append("\n");
        summary.append("  Jeu: ").append(currentGame != null ? currentGame : "Aucun").append("\n");
        summary.append("  Overlay: ").append(isOverlayEnabled() ? "Activé" : "Désactivé").append("\n");
        summary.append("  Chemin: ").append(getOverlayPath()).append("\n");
        summary.append("  Échelle: ").append(getOverlayScale()).append("\n");
        summary.append("  Opacité: ").append(getOverlayOpacity()).append("\n");
        summary.append("  Auto-scale: ").append(isAutoScaleEnabled() ? "Oui" : "Non").append("\n");
        summary.append("  Auto-rotate: ").append(isAutoRotateEnabled() ? "Oui" : "Non");
        
        return summary.toString();
    }
} 