package com.fceumm.wrapper.video;

import android.util.Log;
import android.view.Surface;

/**
 * **100% RETROARCH** : Gestionnaire vidéo unifié conforme aux standards RetroArch
 * 
 * Remplace les anciens gestionnaires vidéo complexes par une interface simplifiée
 * qui délègue le rendu au moteur C++ OpenGL ES 3.0 natif.
 * 
 * Fonctionnalités RetroArch :
 * - Rendu OpenGL ES 3.0 natif avec EGL
 * - Shaders GLSL pour les effets vidéo
 * - Gestion des ratios d'aspect
 * - Filtres vidéo (linéaire/nearest)
 * - V-Sync configurable
 * - Mise à l'échelle en temps réel
 */
public class RetroArchVideoManager {
    private static final String TAG = "RetroArchVideoManager";
    
    // Chargement de la bibliothèque native
    static {
        System.loadLibrary("fceummwrapper");
    }
    
    // Constantes RetroArch pour la qualité vidéo
    public static final int VIDEO_FILTER_NEAREST = 0;
    public static final int VIDEO_FILTER_LINEAR = 1;
    public static final int VIDEO_FILTER_BILINEAR = 2;
    public static final int VIDEO_FILTER_TRILINEAR = 3;
    
    // Ratios d'aspect RetroArch
    public static final float ASPECT_RATIO_4_3 = 4.0f / 3.0f;
    public static final float ASPECT_RATIO_16_9 = 16.0f / 9.0f;
    public static final float ASPECT_RATIO_16_10 = 16.0f / 10.0f;
    public static final float ASPECT_RATIO_21_9 = 21.0f / 9.0f;
    
    // Résolutions par défaut
    public static final int DEFAULT_WIDTH = 256;
    public static final int DEFAULT_HEIGHT = 240;
    public static final int HD_WIDTH = 1280;
    public static final int HD_HEIGHT = 720;
    public static final int FULLHD_WIDTH = 1920;
    public static final int FULLHD_HEIGHT = 1080;
    
    // Variables d'état
    private boolean isInitialized = false;
    private Surface currentSurface = null;
    
    /**
     * Initialise le gestionnaire vidéo avec une surface Android
     * 
     * @param surface Surface Android pour le rendu
     * @return true si l'initialisation réussit
     */
    public boolean initialize(Surface surface) {
        if (isInitialized) {
            Log.w(TAG, "Déjà initialisé");
            return true;
        }
        
        if (surface == null) {
            Log.e(TAG, "Surface invalide");
            return false;
        }
        
        Log.i(TAG, "Initialisation du gestionnaire vidéo RetroArch...");
        
        boolean result = initVideo(surface);
        if (result) {
            isInitialized = true;
            currentSurface = surface;
            Log.i(TAG, "Gestionnaire vidéo RetroArch initialisé avec succès");
        } else {
            Log.e(TAG, "Échec de l'initialisation du gestionnaire vidéo");
        }
        
        return result;
    }
    
    /**
     * Arrête le gestionnaire vidéo
     */
    public void shutdown() {
        if (!isInitialized) {
            return;
        }
        
        Log.i(TAG, "Arrêt du gestionnaire vidéo RetroArch...");
        shutdownVideo();
        isInitialized = false;
        currentSurface = null;
        Log.i(TAG, "Gestionnaire vidéo RetroArch arrêté");
    }
    
    /**
     * Définit le ratio d'aspect pour le rendu vidéo
     * 
     * @param ratio Ratio d'aspect (ex: 4.0f/3.0f pour 4:3)
     */
    public void setAspectRatio(float ratio) {
        if (!isInitialized) {
            Log.w(TAG, "Gestionnaire non initialisé");
            return;
        }
        
        Log.d(TAG, "Définition du ratio d'aspect: " + ratio);
        setAspectRatioNative(ratio);
    }
    
    /**
     * Définit l'échelle de rendu
     * 
     * @param scaleX Échelle horizontale
     * @param scaleY Échelle verticale
     */
    public void setScale(float scaleX, float scaleY) {
        if (!isInitialized) {
            Log.w(TAG, "Gestionnaire non initialisé");
            return;
        }
        
        Log.d(TAG, "Définition de l'échelle: (" + scaleX + ", " + scaleY + ")");
        setScaleNative(scaleX, scaleY);
    }
    
    /**
     * Définit le mode de filtre vidéo
     * 
     * @param mode Mode de filtre (VIDEO_FILTER_NEAREST, VIDEO_FILTER_LINEAR, etc.)
     */
    public void setFilterMode(int mode) {
        if (!isInitialized) {
            Log.w(TAG, "Gestionnaire non initialisé");
            return;
        }
        
        Log.d(TAG, "Définition du mode de filtre: " + mode);
        setFilterModeNative(mode);
    }
    
    /**
     * Active ou désactive la synchronisation verticale
     * 
     * @param enabled true pour activer V-Sync, false pour désactiver
     */
    public void setVSync(boolean enabled) {
        if (!isInitialized) {
            Log.w(TAG, "Gestionnaire non initialisé");
            return;
        }
        
        Log.d(TAG, "Définition V-Sync: " + enabled);
        setVSyncNative(enabled);
    }
    
    /**
     * Définit la résolution de rendu
     * 
     * @param width Largeur en pixels
     * @param height Hauteur en pixels
     */
    public void setResolution(int width, int height) {
        if (!isInitialized) {
            Log.w(TAG, "Gestionnaire non initialisé");
            return;
        }
        
        Log.d(TAG, "Définition de la résolution: " + width + "x" + height);
        setResolutionNative(width, height);
    }
    
    /**
     * Obtient le ratio d'aspect actuel
     * 
     * @return Ratio d'aspect actuel
     */
    public float getAspectRatio() {
        if (!isInitialized) {
            Log.w(TAG, "Gestionnaire non initialisé");
            return ASPECT_RATIO_4_3;
        }
        
        return getAspectRatioNative();
    }
    
    /**
     * Obtient le mode de filtre actuel
     * 
     * @return Mode de filtre actuel
     */
    public int getFilterMode() {
        if (!isInitialized) {
            Log.w(TAG, "Gestionnaire non initialisé");
            return VIDEO_FILTER_LINEAR;
        }
        
        return getFilterModeNative();
    }
    
    /**
     * Vérifie si V-Sync est activé
     * 
     * @return true si V-Sync est activé
     */
    public boolean isVSyncEnabled() {
        if (!isInitialized) {
            Log.w(TAG, "Gestionnaire non initialisé");
            return true;
        }
        
        return isVSyncEnabledNative();
    }
    
    /**
     * Vérifie si le gestionnaire est initialisé
     * 
     * @return true si initialisé
     */
    public boolean isInitialized() {
        return isInitialized && isInitializedNative();
    }
    
    /**
     * Obtient la surface actuelle
     * 
     * @return Surface actuelle ou null
     */
    public Surface getCurrentSurface() {
        return currentSurface;
    }
    
    // Méthodes utilitaires RetroArch
    
    /**
     * Configure les paramètres vidéo par défaut RetroArch
     */
    public void setDefaultRetroArchSettings() {
        if (!isInitialized) {
            Log.w(TAG, "Gestionnaire non initialisé");
            return;
        }
        
        Log.i(TAG, "Application des paramètres RetroArch par défaut");
        
        // Paramètres vidéo par défaut RetroArch
        setAspectRatio(ASPECT_RATIO_4_3);
        setScale(1.0f, 1.0f);
        setFilterMode(VIDEO_FILTER_LINEAR);
        setVSync(true);
        setResolution(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }
    
    /**
     * Configure les paramètres pour une qualité HD
     */
    public void setHDSettings() {
        if (!isInitialized) {
            Log.w(TAG, "Gestionnaire non initialisé");
            return;
        }
        
        Log.i(TAG, "Configuration pour qualité HD");
        
        setAspectRatio(ASPECT_RATIO_16_9);
        setScale(1.0f, 1.0f);
        setFilterMode(VIDEO_FILTER_BILINEAR);
        setVSync(true);
        setResolution(HD_WIDTH, HD_HEIGHT);
    }
    
    /**
     * Configure les paramètres pour une qualité Full HD
     */
    public void setFullHDSettings() {
        if (!isInitialized) {
            Log.w(TAG, "Gestionnaire non initialisé");
            return;
        }
        
        Log.i(TAG, "Configuration pour qualité Full HD");
        
        setAspectRatio(ASPECT_RATIO_16_9);
        setScale(1.0f, 1.0f);
        setFilterMode(VIDEO_FILTER_TRILINEAR);
        setVSync(true);
        setResolution(FULLHD_WIDTH, FULLHD_HEIGHT);
    }
    
    /**
     * Configure les paramètres pour une performance maximale
     */
    public void setPerformanceSettings() {
        if (!isInitialized) {
            Log.w(TAG, "Gestionnaire non initialisé");
            return;
        }
        
        Log.i(TAG, "Configuration pour performance maximale");
        
        setAspectRatio(ASPECT_RATIO_4_3);
        setScale(1.0f, 1.0f);
        setFilterMode(VIDEO_FILTER_NEAREST);
        setVSync(false);
        setResolution(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }
    
    // Méthodes natives JNI
    
    private native boolean initVideo(Surface surface);
    private native void shutdownVideo();
    private native void setAspectRatioNative(float ratio);
    private native void setScaleNative(float x, float y);
    private native void setFilterModeNative(int mode);
    private native void setVSyncNative(boolean enabled);
    private native void setResolutionNative(int width, int height);
    private native float getAspectRatioNative();
    private native int getFilterModeNative();
    private native boolean isVSyncEnabledNative();
    private native boolean isInitializedNative();
}
