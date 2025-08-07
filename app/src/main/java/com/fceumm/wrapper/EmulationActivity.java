package com.fceumm.wrapper;

import android.app.Activity;
import android.os.Bundle;
import android.view.WindowManager;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.content.res.Configuration;
import com.fceumm.wrapper.overlay.RetroArchOverlaySystem;
import com.fceumm.wrapper.overlay.OverlayRenderView;
import android.os.Handler;
import android.os.Looper;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;

/**
 * Activité spécialisée pour l'émulation
 * Utilise le système RetroArch exact avec configuration par jeu/core
 */
public class EmulationActivity extends Activity {
    private static final String TAG = "EmulationActivity";
    
    // Charger la bibliothèque native
    static {
        System.loadLibrary("fceummwrapper");
    }
    
    // Composants UI
    private EmulatorView emulatorView;
    private OverlayRenderView overlayRenderView;
    private FrameLayout gameViewport;
    private LinearLayout controlsArea;
    
    // Système d'overlays RetroArch
    private RetroArchOverlaySystem overlaySystem;
    
    // État de l'émulation
    private boolean isRunning = false;
    private boolean isPaused = false;
    private int frameCount = 0;
    private Handler mainHandler;
    
    // Menu pause
    private android.widget.LinearLayout pauseMenu;
    private boolean pauseMenuVisible = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "🚀 EmulationActivity.onCreate() - Démarrage de l'émulation RetroArch");
        
        // Configuration plein écran
        setupFullscreen();
        
        // Utiliser le layout RetroArch approprié selon l'orientation
        setContentView(R.layout.activity_retroarch);
        
        // Initialiser les composants UI
        initUIComponents();
        
        // Initialiser le système d'overlays RetroArch
        initRetroArchOverlaySystem();
        
        // Initialiser l'émulation
        initEmulation();
        
        Log.i(TAG, "✅ EmulationActivity initialisée avec succès - Layouts spécifiques gèrent l'orientation");
    }
    
    /**
     * Configuration plein écran avec immersion complète
     */
    private void setupFullscreen() {
        // Configuration plein écran classique
        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN |
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_FULLSCREEN |
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        );
        
        // Mode immersion complète (cache barre de navigation)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            );
        }
    }
    
    /**
     * Initialiser les composants UI
     */
    private void initUIComponents() {
        emulatorView = findViewById(R.id.emulator_view);
        overlayRenderView = findViewById(R.id.overlay_render_view);
        gameViewport = findViewById(R.id.game_viewport);
        // Pas de controls_area (100% RetroArch)
        
        // SEUL overlay render view (comme RetroArch)
        overlayRenderView = findViewById(R.id.overlay_render_view);
        if (overlayRenderView == null) {
            Log.e(TAG, "❌ overlay_render_view non trouvé");
        } else {
            Log.d(TAG, "✅ overlay_render_view trouvé");
        }
        
        // Zone de contrôles (comme RetroArch)
        controlsArea = findViewById(R.id.controls_area);
        if (controlsArea == null) {
            Log.e(TAG, "❌ controls_area non trouvé");
        } else {
            Log.d(TAG, "✅ controls_area trouvé");
        }
        
        // pause_menu n'existe que dans activity_main.xml, pas dans activity_retroarch.xml
        pauseMenu = findViewById(R.id.pause_menu);
        if (pauseMenu == null) {
            Log.d(TAG, "📱 Pause menu non disponible dans ce layout");
        }
        
        mainHandler = new Handler(Looper.getMainLooper());
        
        Log.d(TAG, "✅ Composants UI initialisés");
    }
    
    /**
     * Initialiser le système d'overlays RetroArch
     */
    private void initRetroArchOverlaySystem() {
        Log.d(TAG, "🎮 Initialisation du système d'overlays RetroArch");
        
        // Obtenir l'instance du système d'overlays RetroArch
        overlaySystem = RetroArchOverlaySystem.getInstance(this);
        Log.d(TAG, "✅ RetroArchOverlaySystem obtenu");
        
        // Configurer l'overlay render view principal (pour le jeu)
        if (overlayRenderView != null) {
            overlayRenderView.setOverlaySystem(overlaySystem);
            Log.d(TAG, "✅ OverlayRenderView principal configuré");
            // Forcer le redessinage
            overlayRenderView.invalidate();
        } else {
            Log.e(TAG, "❌ overlayRenderView est null");
        }
        
        // SEUL overlay render view configuré (comme RetroArch)
        if (overlayRenderView != null) {
            overlayRenderView.setOverlaySystem(overlaySystem);
            Log.d(TAG, "✅ OverlayRenderView configuré (100% RetroArch)");
            // Forcer le redessinage
            overlayRenderView.invalidate();
        } else {
            Log.e(TAG, "❌ OverlayRenderView non disponible");
        }
        
        // Configurer le listener d'input
        overlaySystem.setInputListener(new RetroArchOverlaySystem.OnOverlayInputListener() {
            @Override
            public void onOverlayInput(int deviceId, boolean pressed) {
                Log.d(TAG, "🎮 Input RetroArch: " + deviceId + " -> " + pressed);
                handleRetroArchInput(deviceId, pressed);
            }
        });
        Log.d(TAG, "✅ Listener d'input configuré");
        
        // Charger l'overlay par défaut
        loadDefaultOverlay();
        
        // Vérifier l'état de l'overlay
        if (overlaySystem.isOverlayEnabled()) {
            Log.i(TAG, "✅ Overlay activé avec succès");
        } else {
            Log.e(TAG, "❌ Overlay non activé");
        }
        
        Log.d(TAG, "✅ Système d'overlays RetroArch initialisé");
    }
    
    /**
     * Charger l'overlay par défaut
     */
    private void loadDefaultOverlay() {
        try {
            // **AMÉLIORATION** : Chargement automatique selon le système
            String selectedRom = getIntent().getStringExtra("selected_rom");
            String systemName = "nes"; // Par défaut
            
            // Détecter le système selon l'extension de la ROM
            if (selectedRom != null) {
                if (selectedRom.toLowerCase().endsWith(".nes")) {
                    systemName = "nes";
                } else if (selectedRom.toLowerCase().endsWith(".smc") || selectedRom.toLowerCase().endsWith(".sfc")) {
                    systemName = "snes";
                } else if (selectedRom.toLowerCase().endsWith(".gba")) {
                    systemName = "gba";
                } else if (selectedRom.toLowerCase().endsWith(".gb") || selectedRom.toLowerCase().endsWith(".gbc")) {
                    systemName = "gb";
                } else if (selectedRom.toLowerCase().endsWith(".iso") || selectedRom.toLowerCase().endsWith(".bin")) {
                    systemName = "psx";
                }
            }
            
            Log.i(TAG, "🎮 Système détecté: " + systemName + " pour ROM: " + selectedRom);
            
            // Charger l'overlay approprié
            overlaySystem.loadOverlayForSystem(systemName);
            overlaySystem.setOverlayEnabled(true);
            
            Log.i(TAG, "✅ Overlay chargé automatiquement pour système: " + systemName);
            
            // Vérifier l'état après chargement
            if (overlaySystem.getActiveOverlay() != null) {
                Log.i(TAG, "✅ Overlay actif: " + overlaySystem.getActiveOverlay().name + 
                      " avec " + overlaySystem.getActiveOverlay().descs.size() + " boutons");
            } else {
                Log.w(TAG, "⚠️ Aucun overlay actif après chargement");
            }
            
            // Forcer le redessinage
            if (overlayRenderView != null) {
                overlayRenderView.invalidate();
            }
            
        } catch (Exception e) {
            Log.e(TAG, "❌ Erreur lors du chargement de l'overlay par défaut", e);
            
            // Fallback vers retropad générique
            try {
                overlaySystem.updateOverlayPath("flat");
                overlaySystem.loadOverlay("retropad.cfg");
                overlaySystem.setOverlayEnabled(true);
                Log.i(TAG, "✅ Overlay fallback chargé: retropad.cfg");
                
                // Vérifier l'état après chargement fallback
                if (overlaySystem.getActiveOverlay() != null) {
                    Log.i(TAG, "✅ Overlay actif (fallback): " + overlaySystem.getActiveOverlay().name + 
                          " avec " + overlaySystem.getActiveOverlay().descs.size() + " boutons");
                }
                
                // Forcer le redessinage
                if (overlayRenderView != null) {
                    overlayRenderView.invalidate();
                }
                
            } catch (Exception e2) {
                Log.e(TAG, "❌ Erreur lors du chargement de l'overlay fallback", e2);
            }
        }
    }
    
    /**
     * Gérer les inputs RetroArch natifs
     */
    private void handleRetroArchInput(int deviceId, boolean pressed) {
        // Système d'input 100% RetroArch natif
        Log.d(TAG, "🎮 Input RetroArch natif: " + deviceId + " -> " + pressed);
        
        // Les device IDs sont déjà dans le format libretro natif (0-15)
        if (deviceId >= 0 && deviceId < 16) {
            // Utiliser le système d'input natif RetroArch
            // Pas besoin de conversion, les IDs sont déjà corrects
            sendRetroArchInput(deviceId, pressed);
            
            // Log pour debug
            String[] buttonNames = {"B", "Y", "SELECT", "START", "UP", "DOWN", "LEFT", "RIGHT", "A", "X", "L", "R", "L2", "R2", "L3", "R3"};
            if (deviceId < buttonNames.length) {
                Log.d(TAG, "🎮 Bouton " + buttonNames[deviceId] + " " + (pressed ? "PRESSÉ" : "RELÂCHÉ"));
            }
        } else {
            Log.w(TAG, "⚠️ Device ID invalide: " + deviceId);
        }
    }
    
    /**
     * Initialiser l'émulation
     */
    private void initEmulation() {
        Log.i(TAG, "🎮 Initialisation de l'émulation RetroArch");
        
        // Récupérer la ROM sélectionnée depuis l'Intent
        String selectedRom = getIntent().getStringExtra("selected_rom");
        if (selectedRom == null) {
            selectedRom = "marioduckhunt.nes"; // ROM par défaut
        }
        Log.i(TAG, "ROM sélectionnée pour l'émulation: " + selectedRom);
        
        // Copier les assets nécessaires
        copySelectedRom(selectedRom);
        copyLibretroCores();
        
        // Initialiser le core libretro
        if (initLibretro()) {
            Log.i(TAG, "✅ Core libretro initialisé");
            
            // Charger le ROM sélectionné
            String romPath = getFilesDir() + "/" + selectedRom;
            if (loadROM(romPath)) {
                Log.i(TAG, "✅ ROM chargé: " + romPath);
                
                // Activer l'audio
                setAudioEnabled(true);
                
                // Démarrer l'émulation
                startEmulation();
                
                // **NOUVEAU** : Timer pour forcer le redessinage des overlays
                startOverlayRefreshTimer();
                
            } else {
                Log.e(TAG, "❌ Échec du chargement du ROM: " + romPath);
            }
        } else {
            Log.e(TAG, "❌ Échec de l'initialisation du core libretro");
        }
    }
    
    /**
     * Démarrer l'émulation
     */
    private void startEmulation() {
        Log.i(TAG, "🚀 Démarrage de l'émulation");
        isRunning = true;
        
        // **NOUVEAU** : Logs de diagnostic
        Log.i(TAG, "🔍 Diagnostic émulation - isRunning: " + isRunning + ", isPaused: " + isPaused);
        Log.i(TAG, "🔍 Diagnostic émulation - emulatorView: " + (emulatorView != null ? "NON-NULL" : "NULL"));
        Log.i(TAG, "🔍 Diagnostic émulation - overlaySystem: " + (overlaySystem != null ? "NON-NULL" : "NULL"));
        
        // Boucle d'émulation dans un thread séparé
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (isRunning && !isPaused) {
                    runFrame();
                    frameCount++;
                    
                    // Mettre à jour l'affichage vidéo
                    updateVideoDisplay();
                    
                    // Contrôler la fréquence (60 FPS)
                    try {
                        Thread.sleep(16); // ~60 FPS
                    } catch (InterruptedException e) {
                        Log.e(TAG, "❌ Thread d'émulation interrompu", e);
                        break;
                    }
                }
            }
        }).start();
        
        Log.i(TAG, "✅ Émulation démarrée");
    }
    
    /**
     * Timer pour forcer le redessinage des overlays
     */
    private void startOverlayRefreshTimer() {
        Handler overlayHandler = new Handler(Looper.getMainLooper());
        Runnable overlayRefreshRunnable = new Runnable() {
            @Override
            public void run() {
                // Forcer le redessinage de l'unique overlay
                if (overlayRenderView != null) {
                    overlayRenderView.invalidate();
                }
                
                // Continuer le timer
                overlayHandler.postDelayed(this, 100); // 100ms
            }
        };
        
        // Démarrer le timer
        overlayHandler.post(overlayRefreshRunnable);
    }
    
    /**
     * Mettre à jour l'affichage vidéo
     */
    private void updateVideoDisplay() {
        if (emulatorView != null) {
            boolean frameUpdated = emulatorView.isFrameUpdated();
            
            if (frameUpdated) {
                byte[] frameData = emulatorView.getFrameBuffer();
                int width = emulatorView.getFrameWidth();
                int height = emulatorView.getFrameHeight();
                
                if (frameData != null && frameData.length > 0) {
                    emulatorView.updateFrame(frameData, width, height);
                }
            }
        }
    }
    
    /**
     * Copier la ROM sélectionnée depuis les assets
     */
    private void copySelectedRom(String romFileName) {
        try {
            File romFile = new File(getFilesDir(), romFileName);
            if (!romFile.exists()) {
                String assetPath = "roms/nes/" + romFileName;
                Log.i(TAG, "Copie de la ROM depuis: " + assetPath);
                
                InputStream inputStream = getAssets().open(assetPath);
                FileOutputStream outputStream = new FileOutputStream(romFile);
                
                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }
                
                inputStream.close();
                outputStream.close();
                
                Log.i(TAG, "✅ ROM copiée: " + romFile.getAbsolutePath());
            } else {
                Log.d(TAG, "ROM déjà présente: " + romFile.getAbsolutePath());
            }
        } catch (IOException e) {
            Log.e(TAG, "❌ Erreur lors de la copie de la ROM: " + romFileName, e);
        }
    }
    
    /**
     * Copier les cores libretro depuis les assets
     */
    private void copyLibretroCores() {
        try {
            File coresDir = new File(getFilesDir(), "cores");
            if (!coresDir.exists()) {
                coresDir.mkdirs();
            }
            
            // Copier le core FCEUmm
            File coreFile = new File(coresDir, "fceumm_libretro_android.so");
            if (!coreFile.exists()) {
                InputStream inputStream = getAssets().open("cores/fceumm_libretro_android.so");
                FileOutputStream outputStream = new FileOutputStream(coreFile);
                
                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }
                
                inputStream.close();
                outputStream.close();
                
                Log.i(TAG, "✅ Core copié: " + coreFile.getAbsolutePath());
            } else {
                Log.d(TAG, "Core déjà présent: " + coreFile.getAbsolutePath());
            }
        } catch (IOException e) {
            Log.e(TAG, "❌ Erreur lors de la copie du core", e);
        }
    }
    
    /**
     * Gestion automatique de l'orientation par les layouts spécifiques (100% RetroArch)
     * Android gère automatiquement les layouts selon l'orientation
     */
    
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "▶️ EmulationActivity.onResume()");
        isPaused = false;
        
        // Remettre en mode immersion complète
        setupFullscreen();
        
        // Layouts spécifiques gèrent automatiquement l'orientation (100% RetroArch)
        Log.d(TAG, "🔄 onResume() - Layouts spécifiques gèrent l'orientation");
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "⏸️ EmulationActivity.onPause()");
        isPaused = true;
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "🗑️ EmulationActivity.onDestroy()");
        isRunning = false;
        
        cleanup();
    }
    
    /**
     * Gestion des changements de visibilité du système UI
     * Pour maintenir l'immersion complète
     */
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            // Remettre en mode immersion quand l'activité reprend le focus
            setupFullscreen();
        }
    }
    
    // Méthodes natives
    private native boolean initLibretro();
    private native boolean loadROM(String romPath);
    private native void runFrame();
    private native void cleanup();
    private native void setAudioEnabled(boolean enabled);
    private native byte[] getAudioData();
    private native int getAudioSampleRate();
    private native int getAudioBufferSize();
    private native void forceAudioProcessing();
    private native void setMasterVolume(int volume);
    private native void setAudioMuted(boolean muted);
    private native void setAudioQuality(int quality);
    private native void setSampleRate(int sampleRate);
    private native void sendRetroArchInput(int buttonId, boolean pressed);
    private native void resetAllRetroArchInputs();
    private native boolean isRetroArchInputPressed(int deviceId);
} 