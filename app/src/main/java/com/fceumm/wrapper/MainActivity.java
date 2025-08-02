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
import com.fceumm.wrapper.input.SimpleInputManager;
import com.fceumm.wrapper.input.SimpleOverlay;
// Audio géré directement par OpenSL ES dans le code natif

public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";
    private EmulatorView emulatorView;
    private SimpleOverlay inputOverlay;
    private SimpleInputManager inputManager;
    // Audio géré par OpenSL ES dans le code natif
    private Handler mainHandler;
    private boolean isRunning = false;
    
    // Bouton pour les paramètres audio
    private android.widget.ImageButton audioSettingsButton;

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
        
        // Copier les ROMs et cores nécessaires
        String selectedRom = getIntent().getStringExtra("selected_rom");
        Log.i(TAG, "selected_rom extra: " + selectedRom);
        if (selectedRom != null) {
            Log.i(TAG, "Copie de la ROM sélectionnée: " + selectedRom);
            copySelectedRom(selectedRom);
        } else {
            Log.i(TAG, "Aucune ROM sélectionnée, utilisation de la ROM par défaut");
            copyMarioDuckHuntRom();
        }
        copyLibretroCores();
        
        // Audio initialisé par OpenSL ES dans le code natif
        
        // Récupérer les vues du layout
        emulatorView = findViewById(R.id.emulator_view);
        inputOverlay = findViewById(R.id.input_overlay);
        if (inputOverlay != null) {
            inputManager = inputOverlay.getInputManager();
        }
        
        // Le bouton audio est désactivé car l'audio est géré par OpenSL ES
        
        // Ajuster les marges selon l'orientation
        adjustMarginsForOrientation();
        audioSettingsButton = findViewById(R.id.audio_settings_button);
        if (audioSettingsButton != null) {
            audioSettingsButton.setVisibility(android.view.View.GONE);
        }
        
        // Initialiser le wrapper libretro
        if (initLibretro()) {
            Log.i(TAG, "Wrapper libretro initialisé avec succès");
            
            // Charger la ROM
            String romFileName = selectedRom != null ? selectedRom : "marioduckhunt.nes";
            String romPath = new File(getFilesDir(), romFileName).getAbsolutePath();
            Log.i(TAG, "Chargement de la ROM: " + romFileName + " -> " + romPath);
            if (loadROM(romPath)) {
                Log.i(TAG, "ROM chargée avec succès");
                
                // Activer l'audio
                setAudioEnabled(true);
                Log.i(TAG, "Audio OpenSL ES activé");
                
                startEmulation();
            } else {
                Log.e(TAG, "Échec du chargement de la ROM");
            }
        } else {
            Log.e(TAG, "Échec de l'initialisation du wrapper libretro");
        }
    }
    
    private void startEmulation() {
        isRunning = true;
        
        // Démarrer la boucle d'émulation synchronisée dans un thread séparé
        new Thread(() -> {
            // Définir la priorité maximale pour ce thread
            Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
            
            long targetFrameTime = 16666667; // ~60 FPS (16.67ms)
            long frameCount = 0;
            
            while (isRunning) {
                long frameStartTime = System.nanoTime();
                
                // Exécuter une frame (audio + vidéo ensemble)
                runFrame();
                
                // Audio traité directement par OpenSL ES dans le code natif
                
                // Mettre à jour la vidéo immédiatement (synchronisé avec l'audio)
                if (emulatorView.isFrameUpdated()) {
                    mainHandler.post(() -> {
                        byte[] frameData = emulatorView.getFrameBuffer();
                        int width = emulatorView.getFrameWidth();
                        int height = emulatorView.getFrameHeight();
                        
                        if (frameData != null) {
                            emulatorView.updateFrame(frameData, width, height);
                        }
                    });
                }
                
                // Timing précis pour maintenir 60 FPS exact
                long frameEndTime = System.nanoTime();
                long frameDuration = frameEndTime - frameStartTime;
                long sleepTime = Math.max(0, targetFrameTime - frameDuration);
                
                if (sleepTime > 0) {
                    try {
                        Thread.sleep(sleepTime / 1000000, (int)(sleepTime % 1000000) / 1000);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
                
                frameCount++;
                
                // Log de synchronisation toutes les 60 frames
                if (frameCount % 60 == 0) {
                    Log.d(TAG, "Synchronisation A/V: Frame " + frameCount + 
                          ", Durée: " + (frameDuration / 1000000) + "ms");
                }
            }
        }).start();
        
        Log.i(TAG, "Émulation synchronisée A/V démarrée");
    }
    
    private void copySelectedRom(String romFileName) {
        try {
            File romFile = new File(getFilesDir(), romFileName);
            
            // Vérifier si la ROM existe déjà
            if (romFile.exists()) {
                Log.i(TAG, "ROM " + romFileName + " déjà présente: " + romFile.getAbsolutePath());
                return;
            }
            
            // Copier depuis les assets
            String assetPath = "roms/nes/" + romFileName;
            InputStream inputStream = getAssets().open(assetPath);
            FileOutputStream outputStream = new FileOutputStream(romFile);
            
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            
            inputStream.close();
            outputStream.close();
            
            Log.i(TAG, "ROM " + romFileName + " copiée vers: " + romFile.getAbsolutePath());
            
        } catch (IOException e) {
            Log.e(TAG, "Erreur lors de la copie de la ROM " + romFileName, e);
        }
    }
    
    private void copyMarioDuckHuntRom() {
        try {
            File romFile = new File(getFilesDir(), "marioduckhunt.nes");
            
            // Vérifier si la ROM existe déjà
            if (romFile.exists()) {
                Log.i(TAG, "ROM Mario Duck Hunt déjà présente: " + romFile.getAbsolutePath());
                return;
            }
            
            // Copier depuis les assets
            InputStream inputStream = getAssets().open("roms/nes/marioduckhunt.nes");
            FileOutputStream outputStream = new FileOutputStream(romFile);
            
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            
            inputStream.close();
            outputStream.close();
            
            Log.i(TAG, "ROM Mario Duck Hunt copiée vers: " + romFile.getAbsolutePath());
            
        } catch (IOException e) {
            Log.e(TAG, "Erreur lors de la copie de la ROM Mario Duck Hunt", e);
        }
    }
    
    private void copyLibretroCores() {
        try {
            // Créer le dossier cores s'il n'existe pas
            File coresDir = new File(getFilesDir(), "cores");
            if (!coresDir.exists()) {
                coresDir.mkdirs();
            }
            
            // Copier le core pour l'architecture actuelle
            String arch = android.os.Build.SUPPORTED_ABIS[0]; // Première architecture supportée
            File coreFile = new File(coresDir, "fceumm_libretro_android.so");
            
            // Vérifier si le core existe déjà
            if (coreFile.exists()) {
                Log.i(TAG, "Core libretro déjà présent: " + coreFile.getAbsolutePath());
                return;
            }
            
            // Vérifier la préférence de core sélectionnée
            boolean useCustomCores = CoreSelectionActivity.isUsingCustomCores(this);
            String coreAssetPath;
            InputStream inputStream = null;
            
            if (useCustomCores) {
                // Essayer d'abord le répertoire coreCustom (cores personnalisés)
                coreAssetPath = "coreCustom/" + arch + "/fceumm_libretro_android.so";
                try {
                    inputStream = getAssets().open(coreAssetPath);
                    Log.i(TAG, "Utilisation du core personnalisé: " + coreAssetPath);
                } catch (IOException e) {
                    Log.w(TAG, "Core personnalisé non trouvé, fallback vers core pré-compilé");
                    // Fallback vers le core pré-compilé
                    coreAssetPath = "coresCompiled/" + arch + "/fceumm_libretro_android.so";
                    try {
                        inputStream = getAssets().open(coreAssetPath);
                        Log.i(TAG, "Utilisation du core pré-compilé (fallback): " + coreAssetPath);
                    } catch (IOException e2) {
                        Log.e(TAG, "Aucun core trouvé pour l'architecture: " + arch, e2);
                        return;
                    }
                }
            } else {
                // Utiliser directement le core pré-compilé
                coreAssetPath = "coresCompiled/" + arch + "/fceumm_libretro_android.so";
                try {
                    inputStream = getAssets().open(coreAssetPath);
                    Log.i(TAG, "Utilisation du core pré-compilé: " + coreAssetPath);
                } catch (IOException e) {
                    Log.e(TAG, "Aucun core pré-compilé trouvé pour l'architecture: " + arch, e);
                    return;
                }
            }
            
            // Copier depuis les assets
            FileOutputStream outputStream = new FileOutputStream(coreFile);
            
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            
            inputStream.close();
            outputStream.close();
            
            // Rendre le fichier exécutable
            coreFile.setExecutable(true);
            
            Log.i(TAG, "Core libretro copié vers: " + coreFile.getAbsolutePath());
            
        } catch (IOException e) {
            Log.e(TAG, "Erreur lors de la copie du core libretro", e);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Maintenir le mode fullscreen lors du retour à l'activité
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                // Nouvelle API pour Android 11+
                getWindow().setDecorFitsSystemWindows(false);
                getWindow().getInsetsController().hide(android.view.WindowInsets.Type.statusBars() | android.view.WindowInsets.Type.navigationBars());
                getWindow().getInsetsController().setSystemBarsBehavior(android.view.WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            } else {
                // Ancienne API pour compatibilité
                getWindow().getDecorView().setSystemUiVisibility(
                    android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                    android.view.View.SYSTEM_UI_FLAG_FULLSCREEN |
                    android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                );
            }
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        isRunning = false;
        
        // Audio libéré par OpenSL ES dans le code natif
        
        // Nettoyer les ressources libretro
        cleanup();
        
        Log.i(TAG, "Application fermée");
    }
    
    @Override
    public void onConfigurationChanged(android.content.res.Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Ajuster les marges quand l'orientation change
        adjustMarginsForOrientation();
    }
    
    private void adjustMarginsForOrientation() {
        if (emulatorView != null && inputOverlay != null) {
            android.content.res.Configuration config = getResources().getConfiguration();
            boolean isPortrait = config.orientation == android.content.res.Configuration.ORIENTATION_PORTRAIT;
            
            android.view.ViewGroup.MarginLayoutParams emulatorParams = 
                (android.view.ViewGroup.MarginLayoutParams) emulatorView.getLayoutParams();
            android.view.ViewGroup.MarginLayoutParams overlayParams = 
                (android.view.ViewGroup.MarginLayoutParams) inputOverlay.getLayoutParams();
            
            if (isPortrait) {
                // En mode portrait : ajouter une marge en haut
                emulatorParams.topMargin = -100;
                overlayParams.topMargin = 100;
            } else {
                // En mode paysage : pas de marge
                emulatorParams.topMargin = 0;
                overlayParams.topMargin = 0;
            }
            
            emulatorView.setLayoutParams(emulatorParams);
            inputOverlay.setLayoutParams(overlayParams);
            
            Log.i(TAG, "Marges ajustées pour l'orientation: " + (isPortrait ? "PORTRAIT" : "PAYSAGE"));
        }
    }
    
    // Méthodes natives
    private native boolean initLibretro();
    private native boolean loadROM(String romPath);
    private native void runFrame();
    private native void cleanup();
    
    // Méthodes natives pour l'audio
    private native void setAudioEnabled(boolean enabled);
    private native byte[] getAudioData();
    private native int getAudioSampleRate();
    private native int getAudioBufferSize();
    private native void forceAudioProcessing();
    
    // Méthodes natives pour les contrôles audio
    private native void setMasterVolume(int volume);
    private native void setAudioMuted(boolean muted);
    private native void setAudioQuality(int quality);
    private native void setSampleRate(int sampleRate);
} 