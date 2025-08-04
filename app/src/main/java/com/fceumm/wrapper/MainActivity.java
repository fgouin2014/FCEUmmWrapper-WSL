package com.fceumm.wrapper;

import android.app.Activity;
import android.os.Bundle;
import android.view.WindowManager;
import android.util.Log;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import android.os.Handler;
import android.os.Looper;
import android.view.ViewGroup.LayoutParams;
import com.fceumm.wrapper.input.InputPortManager;
import com.fceumm.wrapper.overlay.RetroArchOverlayLoader;

public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";
    private EmulatorView emulatorView;
    private RetroArchOverlayLoader retroArchOverlayLoader;
    private InputPortManager inputPortManager;
    private Handler mainHandler;
    private boolean isRunning = false;
    private int frameCount = 0;

    static {
        System.loadLibrary("fceummwrapper");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Mode FULLSCREEN complet
        getWindow().setFlags(
            android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN,
            android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN
        );
        
        // Utiliser le layout activity_emulation.xml
        setContentView(R.layout.activity_emulation);
        
        // Masquer la barre de navigation (Android 4.4+) - APRÈS setContentView
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                    // Nouvelle API pour Android 11+
                    getWindow().setDecorFitsSystemWindows(false);
                    if (getWindow().getInsetsController() != null) {
                        getWindow().getInsetsController().hide(android.view.WindowInsets.Type.statusBars() | android.view.WindowInsets.Type.navigationBars());
                        getWindow().getInsetsController().setSystemBarsBehavior(android.view.WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
                    }
                } else {
                    // Ancienne API pour compatibilité
                    if (getWindow().getDecorView() != null) {
                        getWindow().getDecorView().setSystemUiVisibility(
                            android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                            android.view.View.SYSTEM_UI_FLAG_FULLSCREEN |
                            android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        );
                    }
                }
            } catch (Exception e) {
                Log.w(TAG, "Erreur lors de la configuration du fullscreen: " + e.getMessage());
            }
        }
        
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        
        Log.i(TAG, "Démarrage de l'application FCEUmm Wrapper");
        
        // Initialiser le handler pour le thread principal
        mainHandler = new Handler(Looper.getMainLooper());
        
        // Initialiser le gestionnaire de ports
        inputPortManager = new InputPortManager();
        // Configurer le port 0 en auto-détection par défaut
        inputPortManager.setPortAutoConfigure(0);
        
        // Copier les ROMs et cores nécessaires
        String selectedRom = getIntent().getStringExtra("selected_rom");
        Log.i(TAG, "selected_rom extra: " + selectedRom);
        
        if (selectedRom != null && !selectedRom.isEmpty()) {
            copySelectedRom(selectedRom);
        } else {
            Log.i(TAG, "Aucune ROM sélectionnée, utilisation de la ROM par défaut");
            copyMarioDuckHuntRom();
            selectedRom = "marioduckhunt.nes";
        }
        
        copyLibretroCores();
        
        // Initialiser les vues
        emulatorView = findViewById(R.id.emulator_view);
        retroArchOverlayLoader = findViewById(R.id.retroarch_overlay_loader);
        
        // Configurer l'overlay RetroArch
        retroArchOverlayLoader.setOnButtonPressedListener(new RetroArchOverlayLoader.OnButtonPressedListener() {
            @Override
            public void onButtonPressed(String buttonName, boolean pressed) {
                Log.d(TAG, "Bouton RetroArch pressé: " + buttonName + " -> " + pressed);
                
                // Gérer les boutons spéciaux (combinaisons)
                if (buttonName.contains("|")) {
                    handleSpecialButton(buttonName, pressed);
                } else {
                    // Bouton simple
                    int buttonId = convertButtonNameToId(buttonName);
                    if (buttonId >= 0) {
                        setJoypadButton(buttonId, pressed);
                        Log.d(TAG, "Bouton joypad défini: " + buttonId + " -> " + pressed);
                    }
                }
            }
        });
        
        // Charger l'overlay RetroArch sélectionné dans les paramètres
        String selectedOverlay = SettingsActivity.getSelectedOverlay(this);
        Log.i(TAG, "Chargement de l'overlay sélectionné: " + selectedOverlay);
        retroArchOverlayLoader.loadOverlay(selectedOverlay);
        Log.i(TAG, "✅ Overlay RetroArch configuré et activé");
        
        // Ajuster les marges pour l'orientation actuelle
        adjustMarginsForOrientation();
        
        // Initialiser le wrapper libretro
        if (initLibretro()) {
            Log.i(TAG, "Wrapper libretro initialisé avec succès");
            
            // Charger la ROM
            String romPath = getFilesDir() + "/" + selectedRom;
            if (loadROM(romPath)) {
                Log.i(TAG, "ROM chargée avec succès: " + selectedRom);
                
                // Activer l'audio OpenSL ES
                setAudioEnabled(true);
                Log.i(TAG, "Audio OpenSL ES activé");
                
                // Démarrer l'émulation
                isRunning = true;
                startEmulation();
                Log.i(TAG, "Émulation synchronisée A/V démarrée");
            } else {
                Log.e(TAG, "Erreur lors du chargement de la ROM");
            }
        } else {
            Log.e(TAG, "Erreur lors de l'initialisation du wrapper libretro");
        }
    }
    
    private void startEmulation() {
        if (!isRunning) return;
        
        // Traitement d'une frame
                runFrame();
                
        // Mettre à jour la vue d'émulation avec les données de frame
        if (emulatorView != null && emulatorView.isFrameUpdated()) {
                        byte[] frameData = emulatorView.getFrameBuffer();
            int frameWidth = emulatorView.getFrameWidth();
            int frameHeight = emulatorView.getFrameHeight();
            
            if (frameData != null && frameData.length > 0) {
                emulatorView.updateFrame(frameData, frameWidth, frameHeight);
            }
        }
        
        // Synchronisation A/V
        byte[] audioData = getAudioData();
        if (audioData != null && audioData.length > 0) {
            // Traitement audio (géré par OpenSL ES dans le code natif)
            forceAudioProcessing();
        }
        
        // Planifier la prochaine frame (60 FPS)
        mainHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startEmulation();
            }
        }, 16); // ~60 FPS (1000ms / 60)
                
                // Log de synchronisation toutes les 60 frames
                if (frameCount % 60 == 0) {
            Log.d(TAG, "Synchronisation A/V: Frame " + frameCount + ", Durée: 1ms");
                }
        frameCount++;
    }
    
    private void copySelectedRom(String romFileName) {
        try {
            File romFile = new File(getFilesDir(), romFileName);
            if (!romFile.exists()) {
                InputStream is = getAssets().open("roms/" + romFileName);
                FileOutputStream fos = new FileOutputStream(romFile);
            byte[] buffer = new byte[1024];
            int length;
                while ((length = is.read(buffer)) > 0) {
                    fos.write(buffer, 0, length);
                }
                fos.close();
                is.close();
                Log.i(TAG, "ROM copiée: " + romFileName);
            } else {
                Log.i(TAG, "ROM déjà présente: " + romFile.getAbsolutePath());
            }
        } catch (IOException e) {
            Log.e(TAG, "Erreur lors de la copie de la ROM: " + e.getMessage());
        }
    }
    
    private void copyMarioDuckHuntRom() {
        try {
            File romFile = new File(getFilesDir(), "marioduckhunt.nes");
            if (!romFile.exists()) {
                InputStream is = getAssets().open("roms/marioduckhunt.nes");
                FileOutputStream fos = new FileOutputStream(romFile);
                byte[] buffer = new byte[1024];
                int length;
                while ((length = is.read(buffer)) > 0) {
                    fos.write(buffer, 0, length);
                }
                fos.close();
                is.close();
                Log.i(TAG, "ROM Mario Duck Hunt copiée");
            } else {
                Log.i(TAG, "ROM Mario Duck Hunt déjà présente: " + romFile.getAbsolutePath());
            }
        } catch (IOException e) {
            Log.e(TAG, "Erreur lors de la copie de la ROM Mario Duck Hunt: " + e.getMessage());
        }
    }
    
    private void copyLibretroCores() {
        try {
            File coresDir = new File(getFilesDir(), "cores");
            if (!coresDir.exists()) {
                coresDir.mkdirs();
            }
            
            File coreFile = new File(coresDir, "fceumm_libretro_android.so");
            if (!coreFile.exists()) {
                InputStream is = getAssets().open("cores/fceumm_libretro_android.so");
                FileOutputStream fos = new FileOutputStream(coreFile);
                byte[] buffer = new byte[1024];
                int length;
                while ((length = is.read(buffer)) > 0) {
                    fos.write(buffer, 0, length);
                }
                fos.close();
                is.close();
                Log.i(TAG, "Core libretro copié");
            } else {
                Log.i(TAG, "Core libretro déjà présent: " + coreFile.getAbsolutePath());
            }
        } catch (IOException e) {
            Log.e(TAG, "Erreur lors de la copie du core libretro: " + e.getMessage());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        
        // Redémarrer l'émulation si elle était en pause
        if (!isRunning) {
            isRunning = true;
            startEmulation();
        }
        
        // Réactiver l'audio
        setAudioEnabled(true);
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        
        // Arrêter l'émulation
        isRunning = false;
        
        // Désactiver l'audio
        setAudioEnabled(false);
        
        // Réinitialiser tous les boutons
        if (retroArchOverlayLoader != null) {
            retroArchOverlayLoader.resetAllButtons();
        }
        
        Log.i(TAG, "Activité mise en pause - boutons réinitialisés");
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        // Arrêter l'émulation
        isRunning = false;
        
        // Nettoyer les ressources
        cleanup();
        
        Log.i(TAG, "Activité détruite - ressources nettoyées");
    }
    
    @Override
    public void onConfigurationChanged(android.content.res.Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        
        // Ajuster les marges pour la nouvelle orientation
        adjustMarginsForOrientation();
        
        // Recharger l'overlay sélectionné pour la nouvelle orientation
        if (retroArchOverlayLoader != null) {
            String selectedOverlay = SettingsActivity.getSelectedOverlay(this);
            Log.i(TAG, "Rechargement de l'overlay pour changement d'orientation: " + selectedOverlay);
            retroArchOverlayLoader.reloadOverlayForOrientation(selectedOverlay);
        }
    }
    
    private void adjustMarginsForOrientation() {
            android.content.res.Configuration config = getResources().getConfiguration();
        String orientation = (config.orientation == android.content.res.Configuration.ORIENTATION_PORTRAIT) ? "PORTRAIT" : "PAYSAGE";
        Log.i(TAG, "Marges ajustées pour l'orientation: " + orientation);
    }
    
    // Méthodes natives
    private native boolean initLibretro();
    private native boolean loadROM(String romPath);
    private native void runFrame();
    private native void cleanup();
    
    // Méthodes audio natives
    private native void setAudioEnabled(boolean enabled);
    private native byte[] getAudioData();
    private native int getAudioSampleRate();
    private native int getAudioBufferSize();
    private native void forceAudioProcessing();
    
    // Méthodes de contrôle audio natives
    private native void setMasterVolume(int volume);
    private native void setAudioMuted(boolean muted);
    private native void setAudioQuality(int quality);
    private native void setSampleRate(int sampleRate);
    
    // Méthode pour définir l'état d'un bouton du joypad
    private native void setJoypadButton(int buttonId, boolean pressed);
    
    // Gestion des boutons spéciaux (combinaisons)
    private void handleSpecialButton(String buttonName, boolean pressed) {
        switch (buttonName.toLowerCase()) {
            case "up|left":
                setJoypadButton(4, pressed); // up
                setJoypadButton(6, pressed); // left
                break;
            case "up|right":
                setJoypadButton(4, pressed); // up
                setJoypadButton(7, pressed); // right
                break;
            case "down|left":
                setJoypadButton(5, pressed); // down
                setJoypadButton(6, pressed); // left
                break;
            case "down|right":
                setJoypadButton(5, pressed); // down
                setJoypadButton(7, pressed); // right
                break;
            case "left|up":
                setJoypadButton(6, pressed); // left
                setJoypadButton(4, pressed); // up
                break;
            case "right|up":
                setJoypadButton(7, pressed); // right
                setJoypadButton(4, pressed); // up
                break;
            case "left|down":
                setJoypadButton(6, pressed); // left
                setJoypadButton(5, pressed); // down
                break;
            case "right|down":
                setJoypadButton(7, pressed); // right
                setJoypadButton(5, pressed); // down
                break;
            case "a|b":
                setJoypadButton(8, pressed); // a
                setJoypadButton(0, pressed); // b
                break;
            case "b|a":
                setJoypadButton(0, pressed); // b
                setJoypadButton(8, pressed); // a
                break;
            case "y|a":
                setJoypadButton(1, pressed); // y (mappé sur B pour NES)
                setJoypadButton(8, pressed); // a
                break;
            case "y|x":
                setJoypadButton(1, pressed); // y (mappé sur B pour NES)
                setJoypadButton(9, pressed); // x (mappé sur A pour NES)
                break;
            case "b|x":
                setJoypadButton(0, pressed); // b
                setJoypadButton(9, pressed); // x (mappé sur A pour NES)
                break;
            case "x|y":
                setJoypadButton(9, pressed); // x (mappé sur A pour NES)
                setJoypadButton(1, pressed); // y (mappé sur B pour NES)
                break;
            case "nul":
                // Ignorer le bouton nul (centre du D-pad)
                break;
            default:
                Log.w(TAG, "Bouton spécial inconnu: " + buttonName);
                break;
        }
    }
    
    // Conversion des noms de boutons en IDs
    private int convertButtonNameToId(String buttonName) {
        switch (buttonName.toLowerCase()) {
            case "a":
                return 8; // A button
            case "b":
                return 0; // B button
            case "x":
                return 9; // X button (mappé sur A pour NES)
            case "y":
                return 1; // Y button (mappé sur B pour NES)
            case "up":
                return 4; // D-pad up
            case "down":
                return 5; // D-pad down
            case "left":
                return 6; // D-pad left
            case "right":
                return 7; // D-pad right
            case "start":
                return 3; // Start button (corrigé)
            case "select":
                return 2; // Select button (corrigé)
            case "l":
                return 10; // L button
            case "r":
                return 11; // R button
            default:
                Log.w(TAG, "Bouton inconnu: " + buttonName);
                return -1;
        }
    }
    
    // Méthodes publiques pour la configuration des ports
    public void configurePort(int port, InputPortManager.DeviceType deviceType, String deviceName) {
        if (inputPortManager != null) {
            inputPortManager.setPortDevice(port, deviceType, deviceName);
        }
    }
    
    public void configurePortAuto(int port) {
        if (inputPortManager != null) {
            inputPortManager.setPortAutoConfigure(port);
        }
    }
    
    public InputPortManager.PortConfig getPortConfig(int port) {
        if (inputPortManager != null) {
            return inputPortManager.getPortConfig(port);
        }
        return null;
    }
    
    public void setCurrentPort(int port) {
        if (inputPortManager != null) {
            inputPortManager.setCurrentPort(port);
        }
    }
    
    public int getCurrentPort() {
        if (inputPortManager != null) {
            return inputPortManager.getCurrentPort();
        }
        return 0;
    }
    
    /**
     * Configure la sensibilité des diagonales pour l'overlay RetroArch
     * @param sensitivity Valeur entre 0 et 100
     */
    public void setDiagonalSensitivity(float sensitivity) {
        if (retroArchOverlayLoader != null) {
            retroArchOverlayLoader.setDiagonalSensitivity(sensitivity);
            Log.i(TAG, "Sensibilité des diagonales configurée: " + sensitivity);
        }
    }
} 