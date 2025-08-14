package com.fceumm.wrapper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.fceumm.wrapper.input.GamepadManager;
import com.fceumm.wrapper.ui.RetroArchModernUI;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * **100% RETROARCH NATIF** : Activité d'émulation principale
 * Intègre l'interface moderne RetroArch et tous les systèmes natifs
 */
public class EmulationActivity extends Activity {
    private static final String TAG = "EmulationActivity";
    
    static {
        System.loadLibrary("fceummwrapper");
    }
    
    // Composants UI
    private EmulatorView emulatorView;
    private RetroArchModernUI modernUI;
    
    // Variables pour le redimensionnement RetroArch
    private boolean isLandscape = false;
    
    // Gestionnaires RetroArch unifiés
    private GamepadManager gamepadManager;
    
    // État de l'émulation
    private boolean isRunning = false;
    private boolean isPaused = false;
    private int frameCount = 0;
    private Handler mainHandler;
    
    // **100% RETROARCH** : ROM sélectionnée
    private String selectedRomFileName = "marioduckhunt.nes"; // ROM par défaut
    
    // Variables pour la détection des touches RetroArch
    private boolean startPressed = false;
    private boolean selectPressed = false;
    private long lastMenuPress = 0;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "🚀 **DEBUG** EmulationActivity.onCreate() - DÉBUT");
        
        try {
            Log.i(TAG, "🚀 **DEBUG** Étape 1: setContentView");
        setContentView(R.layout.activity_retroarch);
            Log.i(TAG, "✅ **DEBUG** Étape 1: setContentView - RÉUSSI");

            Log.i(TAG, "🚀 **DEBUG** Étape 2: cleanupOverlaySettings");
            // **100% RETROARCH** : Nettoyer les paramètres d'overlay du menu principal
            // pour éviter les conflits avec l'émulation
            cleanupOverlaySettings();
            Log.i(TAG, "✅ **DEBUG** Étape 2: cleanupOverlaySettings - RÉUSSI");

            Log.i(TAG, "🚀 **DEBUG** Étape 3: initUIComponents");
        initUIComponents();
            Log.i(TAG, "✅ **DEBUG** Étape 3: initUIComponents - RÉUSSI");

            Log.i(TAG, "🚀 **DEBUG** Étape 4: initModernUIFromLayout");
            initModernUIFromLayout();
            Log.i(TAG, "✅ **DEBUG** Étape 4: initModernUIFromLayout - RÉUSSI");

            Log.i(TAG, "🚀 **DEBUG** Étape 5: initRetroArchManagers");
        initRetroArchManagers();
            Log.i(TAG, "✅ **DEBUG** Étape 5: initRetroArchManagers - RÉUSSI");

            Log.i(TAG, "🚀 **DEBUG** Étape 6: copyLibretroCores");
            copyLibretroCores();
            Log.i(TAG, "✅ **DEBUG** Étape 6: copyLibretroCores - RÉUSSI");

            Log.i(TAG, "🚀 **DEBUG** Étape 7: copySelectedRom");
            // **100% RETROARCH** : Lire la ROM sélectionnée depuis l'Intent
            String selectedRom = getIntent().getStringExtra("selected_rom");
            if (selectedRom != null && !selectedRom.isEmpty()) {
                selectedRomFileName = selectedRom;
                Log.i(TAG, "🎮 **100% RETROARCH** - ROM sélectionnée: " + selectedRom);
                copySelectedRom(selectedRom);
        } else {
                Log.i(TAG, "🎮 **100% RETROARCH** - Aucune ROM sélectionnée, utilisation de la ROM par défaut");
                copySelectedRom("marioduckhunt.nes"); // ROM par défaut
            }
            Log.i(TAG, "✅ **DEBUG** Étape 7: copySelectedRom - RÉUSSI");

            Log.i(TAG, "🚀 **DEBUG** Étape 8: showRetroArchMainMenu");
            // **100% RETROARCH** : Afficher le menu RetroArch au démarrage
            showRetroArchMainMenu();
            Log.i(TAG, "✅ **DEBUG** Étape 8: showRetroArchMainMenu - RÉUSSI");

            Log.i(TAG, "🎉 **DEBUG** EmulationActivity.onCreate() - TERMINÉ AVEC SUCCÈS");

        } catch (Exception e) {
            Log.e(TAG, "❌ **DEBUG** ERREUR CRITIQUE dans onCreate: " + e.getMessage(), e);
            // Ne pas terminer l'activité ici, laisser une chance de récupération
        }
    }

    /**
     * **100% RETROARCH** : Nettoyer les paramètres d'overlay du menu principal
     * pour éviter les conflits avec l'émulation
     */
    private void cleanupOverlaySettings() {
        try {
            // Utiliser un namespace séparé pour l'émulation
            SharedPreferences emulationPrefs = getSharedPreferences("RetroArchEmulation", MODE_PRIVATE);
            SharedPreferences.Editor editor = emulationPrefs.edit();

            // Réinitialiser les paramètres d'overlay pour l'émulation
            editor.putString("input_overlay_enable", "true");
            editor.putString("input_overlay_path", "overlays/gamepads/flat/nes.cfg");
            editor.putString("input_overlay_scale", "1.5");
            editor.putString("input_overlay_opacity", "0.8");
            editor.putString("input_overlay_show_inputs", "false");

            editor.apply();

            Log.i(TAG, "🎮 **100% RETROARCH** : Paramètres d'overlay nettoyés pour l'émulation");

        } catch (Exception e) {
            Log.w(TAG, "Erreur lors du nettoyage des paramètres d'overlay: " + e.getMessage());
        }
    }
    
    /**
     * Initialiser les gestionnaires RetroArch
     */
    private void initRetroArchManagers() {
        // **100% RETROARCH** : Initialiser le gestionnaire de gamepad
        gamepadManager = new GamepadManager(this);
        gamepadManager.setGamepadCallback(new GamepadManager.GamepadCallback() {
            @Override
            public void onGamepadConnected(InputDevice device, GamepadManager.GamepadType type) {
                Log.i(TAG, "�� **100% RETROARCH** : Gamepad connecté: " + device.getName() + " (Type: " + type + ")");
                Toast.makeText(EmulationActivity.this, "Gamepad connecté: " + device.getName(), Toast.LENGTH_SHORT).show();
                
                // **100% RETROARCH** : Mettre à jour l'affichage des gamepads
                updateGamepadDisplay();
            }
            
            @Override
            public void onGamepadDisconnected(InputDevice device) {
                Log.i(TAG, "�� **100% RETROARCH** : Gamepad déconnecté: " + device.getName());
                Toast.makeText(EmulationActivity.this, "Gamepad déconnecté: " + device.getName(), Toast.LENGTH_SHORT).show();
                
                // **100% RETROARCH** : Mettre à jour l'affichage des gamepads
                updateGamepadDisplay();
            }
            
            @Override
            public void onButtonPressed(int deviceId, int buttonId, boolean pressed) {
                // Envoyer directement au core RetroArch
                sendRetroArchInput(buttonId, pressed);
                Log.d(TAG, "🎮 **100% RETROARCH** - Bouton gamepad: " + buttonId + " " + (pressed ? "PRESSÉ" : "RELÂCHÉ"));
            }
            
            @Override
            public void onAxisChanged(int deviceId, int axisId, float value) {
                // Traiter les axes analogiques (pour les jeux qui les supportent)
                Log.d(TAG, "🎮 **100% RETROARCH** - Axe gamepad: " + axisId + " = " + value);
            }
        });
        Log.i(TAG, "✅ Gestionnaire de gamepad RetroArch initialisé");
    }
    
    /**
     * Initialiser les composants UI
     */
    private void initUIComponents() {
        Log.i(TAG, "🎨 **DIAGNOSTIC** Initialisation des composants UI");
        
        emulatorView = findViewById(R.id.emulator_view);
        Log.i(TAG, "🎨 **DIAGNOSTIC** EmulatorView trouvée: " + (emulatorView != null));
        if (emulatorView != null) {
            Log.i(TAG, "🎨 **DIAGNOSTIC** EmulatorView - Visibilité: " + emulatorView.getVisibility() + " - Largeur: " + emulatorView.getWidth() + " - Hauteur: " + emulatorView.getHeight());
        }
        
        mainHandler = new Handler(Looper.getMainLooper());
        
        Log.i(TAG, "🎨 **DIAGNOSTIC** Composants UI initialisés - EmulatorView: " + (emulatorView != null ? "OK" : "NULL"));
        if (emulatorView != null) {
            Log.i(TAG, "🎨 **DIAGNOSTIC** EmulatorView finale - Visibilité: " + emulatorView.getVisibility() + " - Largeur: " + emulatorView.getWidth() + " - Hauteur: " + emulatorView.getHeight());
            
            // **CRITIQUE** : Forcer le layout si dimensions nulles
            if (emulatorView.getWidth() == 0 || emulatorView.getHeight() == 0) {
                Log.i(TAG, "🎨 **CRITIQUE** Dimensions nulles détectées - Forçage du layout");
                emulatorView.post(new Runnable() {
                    @Override
                    public void run() {
                        emulatorView.requestLayout();
                        emulatorView.invalidate();
                        Log.i(TAG, "🎨 **CRITIQUE** Layout forcé - Nouvelles dimensions: " + emulatorView.getWidth() + "x" + emulatorView.getHeight());
                    }
                });
            }
        }
    }
    
    /**
     * **100% RETROARCH NATIF** : Initialiser la nouvelle interface moderne depuis le layout
     */
    private void initModernUIFromLayout() {
        Log.i(TAG, "🎮 **100% RETROARCH** - Initialisation de l'interface moderne depuis le layout");
        
        // **DEBUG** : Vérifier si le layout est chargé
        View rootView = findViewById(android.R.id.content);
        Log.d(TAG, "🔍 Root view: " + (rootView != null ? "TROUVÉ" : "NULL"));
        
        // **CRITIQUE** : Utiliser un délai pour s'assurer que les vues sont créées
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
            public void run() {
                // **100% RETROARCH** : Chercher l'interface moderne dans le layout
                modernUI = findViewById(R.id.retroarch_modern_ui);
                Log.d(TAG, "🔍 Recherche de retroarch_modern_ui (après délai): " + (modernUI != null ? "TROUVÉ" : "NULL"));
                
                if (modernUI != null) {
                    // **100% RETROARCH** : Configuration du callback UI
                    modernUI.setUICallback(new RetroArchModernUI.UICallback() {
                        @Override
                        public void onMenuRequested() {
                            Log.i(TAG, "🎮 Menu RetroArch demandé");
                        }

                        @Override
                        public void onQuickMenuRequested() {
                            Log.i(TAG, "🎮 Menu rapide RetroArch demandé");
                        }

                @Override
                        public void onStateSaved() {
                            Log.i(TAG, "🎮 État sauvegardé");
                        }

                @Override
                        public void onStateLoaded() {
                            Log.i(TAG, "🎮 État chargé");
                        }

                @Override
                        public void onScreenshotTaken() {
                            Log.i(TAG, "🎮 Screenshot pris");
                        }

                @Override
                        public void onRewindActivated() {
                            Log.i(TAG, "🎮 Rewind activé");
                        }

                @Override
                        public void onFastForwardActivated() {
                            Log.i(TAG, "🎮 Fast Forward activé");
                        }

                        @Override
                        public void onRomSelectionRequested() {
                            Log.i(TAG, "🎮 Sélection ROM demandée");
                            // Lancer l'activité de sélection de ROM
                            Intent intent = new Intent(EmulationActivity.this, RomSelectionActivity.class);
                            startActivity(intent);
                        }

                        @Override
                        public void onSettingsRequested() {
                            Log.i(TAG, "🎮 Paramètres demandés");
                            // Lancer l'activité des paramètres
                            Intent intent = new Intent(EmulationActivity.this, SettingsActivity.class);
                            startActivity(intent);
                        }

                        @Override
                        public void onBackToMainMenu() {
                            Log.i(TAG, "🎮 Retour au menu principal demandé");
                            // Retourner au menu principal
                            finish();
                        }
                        
                        @Override
                        public void onInputSent(int deviceId, boolean pressed) {
                            Log.i(TAG, "🎮 **100% RETROARCH** - Input envoyé: " + deviceId + " (pressed: " + pressed + ")");
                            sendRetroArchInput(deviceId, pressed);
                        }
                    });
                    
                    Log.i(TAG, "✅ Interface moderne initialisée depuis le layout");
                } else {
                    Log.e(TAG, "❌ Interface moderne non trouvée dans le layout - Tentative de création programmatique");
                    createModernUIProgrammatically();
                }
            }
        }, 100); // Délai de 100ms pour s'assurer que les vues sont créées
    }

    /**
     * **100% RETROARCH** : Créer l'interface moderne programmatiquement si nécessaire
     */
    private void createModernUIProgrammatically() {
        Log.i(TAG, "🔧 **FALLBACK** - Création programmatique de l'interface moderne");
        
        // **100% RETROARCH** : Créer l'interface moderne programmatiquement
        modernUI = new RetroArchModernUI(this);
        
        // **100% RETROARCH** : Configuration du callback UI
        modernUI.setUICallback(new RetroArchModernUI.UICallback() {
            @Override
            public void onMenuRequested() {
                Log.i(TAG, "🎮 **100% RETROARCH** - Menu demandé via interface moderne (programmatique)");
            }

            @Override
            public void onQuickMenuRequested() {
                Log.i(TAG, "🎮 **100% RETROARCH** - Menu rapide demandé via interface moderne (programmatique)");
            }

            @Override
            public void onStateSaved() {
                Log.i(TAG, "🎮 **100% RETROARCH** - État sauvegardé via interface moderne (programmatique)");
            }

            @Override
            public void onStateLoaded() {
                Log.i(TAG, "🎮 **100% RETROARCH** - État chargé via interface moderne (programmatique)");
            }

                @Override
            public void onScreenshotTaken() {
                Log.i(TAG, "🎮 **100% RETROARCH** - Screenshot pris via interface moderne (programmatique)");
            }

            @Override
            public void onRewindActivated() {
                Log.i(TAG, "🎮 **100% RETROARCH** - Rembobinage activé via interface moderne (programmatique)");
            }

            @Override
            public void onFastForwardActivated() {
                Log.i(TAG, "🎮 **100% RETROARCH** - Avance rapide activée via interface moderne (programmatique)");
            }

            @Override
            public void onRomSelectionRequested() {
                Log.i(TAG, "🎮 **100% RETROARCH** - Sélection ROM demandée via interface moderne (programmatique)");
                // Lancer l'activité de sélection de ROM
                Intent intent = new Intent(EmulationActivity.this, RomSelectionActivity.class);
                startActivity(intent);
            }

            @Override
            public void onSettingsRequested() {
                Log.i(TAG, "🎮 **100% RETROARCH** - Paramètres demandés via interface moderne (programmatique)");
                // Lancer l'activité des paramètres
                Intent intent = new Intent(EmulationActivity.this, SettingsActivity.class);
                startActivity(intent);
            }

            @Override
            public void onBackToMainMenu() {
                Log.i(TAG, "🎮 **100% RETROARCH** - Retour au menu principal demandé via interface moderne (programmatique)");
                // Retourner au menu principal
                finish();
            }
            
            @Override
            public void onInputSent(int deviceId, boolean pressed) {
                Log.i(TAG, "🎮 **100% RETROARCH** - Input envoyé (programmatique): " + deviceId + " (pressed: " + pressed + ")");
                sendRetroArchInput(deviceId, pressed);
            }
        });

        // **100% RETROARCH** : Ajouter l'interface moderne au layout
        View mainLayout = findViewById(R.id.main_layout);
        if (mainLayout == null) {
            Log.d(TAG, "🔍 Tentative avec android.R.id.content");
            mainLayout = findViewById(android.R.id.content);
        }
        if (mainLayout == null) {
            Log.d(TAG, "🔍 Tentative avec getWindow().getDecorView()");
            mainLayout = getWindow().getDecorView().findViewById(android.R.id.content);
        }
        
        if (mainLayout instanceof android.widget.FrameLayout) {
            android.widget.FrameLayout frameLayout = (android.widget.FrameLayout) mainLayout;
            frameLayout.addView(modernUI);
            Log.i(TAG, "✅ Interface moderne créée et ajoutée programmatiquement");
        } else {
            Log.e(TAG, "❌ Impossible de trouver le layout principal pour ajouter l'interface moderne");
        }
        
        Log.i(TAG, "✅ Interface moderne programmatique initialisée avec succès");
    }

    /**
     * **100% RETROARCH NATIF** : Détecter les touches RetroArch pour ouvrir le menu
     */
    private void handleRetroArchMenuInput(int deviceId, boolean pressed) {
        // Détection Start + Select (comme RetroArch officiel)
        if (deviceId == 6) { // RETRO_DEVICE_ID_JOYPAD_START
            startPressed = pressed;
        } else if (deviceId == 7) { // RETRO_DEVICE_ID_JOYPAD_SELECT
            selectPressed = pressed;
        }
        
        // Si Start ET Select sont pressés simultanément
        if (startPressed && selectPressed) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastMenuPress > 500) { // Éviter les doubles pressions
                lastMenuPress = currentTime;
                
                // **100% RETROARCH** : Utiliser l'interface moderne
                if (modernUI != null) {
                    if (modernUI.isMenuVisible()) {
                        modernUI.hideMenu();
        } else {
                        modernUI.showMainMenu();
                    }
                    Log.i(TAG, "🎮 **100% RETROARCH** - Menu moderne activé via Start + Select");
                }
            }
        }
    }
    
    /**
     * Démarrer l'émulation
     */
    private void startEmulation() {
        isRunning = true;
        isPaused = false;
        
        Log.i(TAG, "🚀 Démarrage de l'émulation");
        Log.i(TAG, "🔍 Diagnostic émulation - isRunning: " + isRunning + ", isPaused: " + isPaused);
        Log.i(TAG, "🔍 Diagnostic émulation - emulatorView: " + (emulatorView != null ? "NON-NULL" : "NULL"));
        
        // **100% RETROARCH** : Initialiser le core libretro
        Log.i(TAG, "🎮 **100% RETROARCH** - Initialisation du core libretro");
        if (initLibretro()) {
            Log.i(TAG, "✅ **100% RETROARCH** - Core libretro initialisé avec succès");
            
            // **100% RETROARCH** : Vérifier que les fonctions critiques sont disponibles
            Log.i(TAG, "🔍 **100% RETROARCH** - Vérification des fonctions critiques...");
            
            // **100% RETROARCH** : Charger la ROM copiée
            String romPath = new File(getFilesDir(), selectedRomFileName).getAbsolutePath();
            Log.i(TAG, "🎮 **100% RETROARCH** - Chargement de la ROM: " + romPath);
            
            // Vérifier que le fichier existe
            File romFile = new File(romPath);
            if (!romFile.exists()) {
                Log.e(TAG, "❌ **100% RETROARCH** - Fichier ROM introuvable: " + romPath);
                Toast.makeText(this, "ROM introuvable: " + selectedRomFileName, Toast.LENGTH_LONG).show();
                isRunning = false;
            return;
        }
        
            Log.i(TAG, "📊 **100% RETROARCH** - Taille du fichier ROM: " + romFile.length() + " bytes");
            
            if (loadROM(romPath)) {
                Log.i(TAG, "✅ **100% RETROARCH** - ROM chargée avec succès par le core libretro");
                
                // **100% RETROARCH** : Notification de succès
                if (modernUI != null) {
                    String romName = selectedRomFileName.replace(".nes", "");
                    modernUI.showNotification("🎮 ROM chargée: " + romName, 3000, 1);
                }
                
                // Activer l'audio
                setAudioEnabled(true);
                Log.i(TAG, "✅ Audio activé");
                
                // **100% RETROARCH** : Démarrer la boucle d'émulation
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (isRunning && !isPaused) {
                    frameCount++;
                    
                            // **100% RETROARCH** : Exécuter une frame
                            runFrame();
                    updateVideoDisplay();
                    
                    try {
                        Thread.sleep(16); // ~60 FPS
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            }
        }).start();
        
        Log.i(TAG, "✅ Émulation démarrée");
            } else {
                Log.e(TAG, "❌ **100% RETROARCH** - Échec du chargement de la ROM par le core libretro");
                // **100% RETROARCH** : Notification d'erreur
                if (modernUI != null) {
                    String romName = selectedRomFileName.replace(".nes", "");
                    modernUI.showNotification("❌ Échec du chargement: " + romName, 3000, 3);
                }
                Toast.makeText(this, "Échec du chargement de la ROM: " + selectedRomFileName, Toast.LENGTH_LONG).show();
                isRunning = false;
            }
        } else {
            Log.e(TAG, "❌ Échec de l'initialisation du core libretro");
            isRunning = false;
        }
    }

    /**
     * **100% RETROARCH** : Rafraîchir l'interface moderne
     */
    private void startOptimizedOverlayRefresh() {
        Runnable overlayRefreshRunnable = new Runnable() {
            @Override
            public void run() {
                if (modernUI != null) {
                    modernUI.invalidate();
                }
                
                if (isRunning) {
                    mainHandler.postDelayed(this, 16); // ~60 FPS
                }
            }
        };
        mainHandler.post(overlayRefreshRunnable);
    }

    /**
     * **100% RETROARCH** : Mettre à jour l'affichage vidéo
     */
    private void updateVideoDisplay() {
        // **100% RETROARCH** : Mettre à jour le gestionnaire de gamepad
        if (gamepadManager != null) {
            gamepadManager.update();
        }

        // **100% RETROARCH** : Mettre à jour l'affichage des gamepads (toutes les 60 frames)
        if (frameCount % 60 == 0) {
            updateGamepadDisplay();
        }

        if (emulatorView != null) {
            boolean frameUpdated = emulatorView.isFrameUpdated();
            
            if (frameUpdated) {
                byte[] frameData = emulatorView.getFrameBuffer();
                int width = emulatorView.getFrameWidth();
                int height = emulatorView.getFrameHeight();
                
                // **VALIDATION** : Vérifier les données de frame
                if (frameData != null && frameData.length > 0 && width > 0 && height > 0) {
                    emulatorView.updateFrame(frameData, width, height);
                }
            }
        }
    }
    
    /**
     * **100% RETROARCH** : Mettre à jour l'affichage des gamepads
     */
    private void updateGamepadDisplay() {
        if (modernUI != null && gamepadManager != null) {
            List<InputDevice> connectedGamepads = gamepadManager.getConnectedGamepads();
            List<String> gamepadNames = new ArrayList<>();
            
            for (InputDevice device : connectedGamepads) {
                GamepadManager.GamepadType type = gamepadManager.getGamepadType(device.getId());
                String gamepadInfo = device.getName() + " (" + type.toString() + ")";
                gamepadNames.add(gamepadInfo);
            }
            
            // Mettre à jour l'interface
            modernUI.updateGamepadStatus(gamepadNames);
            
            Log.i(TAG, "🎮 **100% RETROARCH** : Affichage mis à jour - " + gamepadNames.size() + " gamepad(s) connecté(s)");
        }
    }
    
    /**
     * **100% RETROARCH** : Copier la ROM sélectionnée depuis les assets
     */
    private void copySelectedRom(String romFileName) {
        try {
            Log.i(TAG, "🎮 **100% RETROARCH** - Début de la copie de la ROM: " + romFileName);
            
            // Vérifier si la ROM existe dans les assets
            String[] availableRoms = getAssets().list("roms/nes");
            boolean romExists = false;
            for (String rom : availableRoms) {
                if (rom.equals(romFileName)) {
                    romExists = true;
                    break;
                }
            }
            
            if (!romExists) {
                Log.e(TAG, "❌ **100% RETROARCH** - ROM non trouvée dans les assets: " + romFileName);
                Log.i(TAG, "📋 ROMs disponibles: " + String.join(", ", availableRoms));
                return;
            }
            
            Log.i(TAG, "✅ **100% RETROARCH** - ROM trouvée dans les assets: " + romFileName);
            
            // Chemin de destination
            File destFile = new File(getFilesDir(), romFileName);
            Log.i(TAG, "📁 **100% RETROARCH** - Chemin de destination: " + destFile.getAbsolutePath());
            
            // Vérifier si la ROM est déjà présente
            if (destFile.exists()) {
                Log.i(TAG, "📋 **100% RETROARCH** - ROM déjà présente: " + destFile.getAbsolutePath());
                Log.i(TAG, "📊 **100% RETROARCH** - Taille du fichier: " + destFile.length() + " bytes");
                return;
            }
            
            // **100% RETROARCH** : Copier la ROM depuis les assets
            String assetPath = "roms/nes/" + romFileName;
            Log.i(TAG, "📂 **100% RETROARCH** - Chemin asset: " + assetPath);
                InputStream inputStream = getAssets().open(assetPath);
            FileOutputStream outputStream = new FileOutputStream(destFile);
                
                byte[] buffer = new byte[1024];
            int bytesRead;
            long totalBytes = 0;
            
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
                totalBytes += bytesRead;
                }
                
                inputStream.close();
                outputStream.close();
                
            Log.i(TAG, "✅ **100% RETROARCH** - ROM copiée avec succès: " + romFileName);
            Log.i(TAG, "📊 **100% RETROARCH** - Taille copiée: " + totalBytes + " bytes");
            Log.i(TAG, "📁 **100% RETROARCH** - Fichier final: " + destFile.getAbsolutePath());
            
        } catch (Exception e) {
            Log.e(TAG, "❌ **100% RETROARCH** - Erreur lors de la copie de la ROM: " + romFileName, e);
            Toast.makeText(this, "Erreur lors du chargement de la ROM: " + romFileName, Toast.LENGTH_LONG).show();
        }
    }
    
    /**
     * Copier les cores libretro
     */
    private void copyLibretroCores() {
        try {
            // **100% RETROARCH** : Détecter l'architecture
            String[] abis = android.os.Build.SUPPORTED_ABIS;
            String targetArch = "arm64-v8a"; // Par défaut
            
            if (abis != null && abis.length > 0) {
                String primaryAbi = abis[0];
                if (primaryAbi.equals("arm64-v8a")) {
                    targetArch = "arm64-v8a";
                } else if (primaryAbi.equals("armeabi-v7a")) {
                    targetArch = "armeabi-v7a";
                } else if (primaryAbi.equals("x86")) {
                    targetArch = "x86";
                } else if (primaryAbi.equals("x86_64")) {
                    targetArch = "x86_64";
                }
            }
            
            Log.i(TAG, "🎮 **100% RETROARCH** - Architecture détectée: " + targetArch);
            
            File coresDir = new File(getFilesDir(), "cores");
            if (!coresDir.exists()) {
                coresDir.mkdirs();
            }
            
            File coreFile = new File(coresDir, "fceumm_libretro_android.so");
            String assetPath = "coresCompiled/" + targetArch + "/fceumm_libretro_android.so";
            
            if (!coreFile.exists()) {
                Log.i(TAG, "Copie du core depuis: " + assetPath);
                InputStream inputStream = getAssets().open(assetPath);
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
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de la copie du core: " + e.getMessage());
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "▶️ EmulationActivity.onResume()");
        
        // **100% RETROARCH** : Reprendre l'émulation
        isPaused = false;
        
        // **100% RETROARCH** : Redémarrer le rafraîchissement de l'interface
        startOptimizedOverlayRefresh();
        
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
        Log.i(TAG, "🗑️ **DEBUG** EmulationActivity.onDestroy() - DÉBUT");
        super.onDestroy();
        Log.d(TAG, "🗑️ EmulationActivity.onDestroy()");
        isRunning = false;
        
        // **100% RETROARCH** : Nettoyer le gestionnaire de gamepad
        if (gamepadManager != null) {
            Log.i(TAG, "🗑️ **DEBUG** Nettoyage du gamepadManager");
            gamepadManager.cleanup();
        }

        Log.i(TAG, "🗑️ **DEBUG** Appel de cleanup()");
        cleanup();
        Log.i(TAG, "🗑️ **DEBUG** EmulationActivity.onDestroy() - TERMINÉ");
    }
    
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            // **100% RETROARCH** : Forcer le fullscreen
            getWindow().getDecorView().setSystemUiVisibility(
                android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                android.view.View.SYSTEM_UI_FLAG_FULLSCREEN |
                android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            );
        }
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        
        // **100% RETROARCH** : Gérer le changement d'orientation
        boolean newIsLandscape = newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE;
        
        if (newIsLandscape != isLandscape) {
            isLandscape = newIsLandscape;
            Log.i(TAG, "🔄 **100% RETROARCH** - Orientation changée: " + (isLandscape ? "LANDSCAPE" : "PORTRAIT"));
            
            // **100% RETROARCH** : Redimensionner l'interface
            if (modernUI != null) {
                modernUI.invalidate();
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        
        // **100% RETROARCH** : Sauvegarder l'état
        outState.putBoolean("isRunning", isRunning);
        outState.putBoolean("isPaused", isPaused);
        outState.putBoolean("isLandscape", isLandscape);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Traiter les événements de gamepad
        if (gamepadManager != null && gamepadManager.processKeyEvent(event)) {
            return true;
        }

        // Gestion des touches système
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                // Retour au menu principal
                finish();
                return true;
            case KeyEvent.KEYCODE_MENU:
                // Ouvrir le menu RetroArch
                if (modernUI != null) {
                    modernUI.showMainMenu();
                }
                return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        // Traiter les événements de gamepad
        if (gamepadManager != null && gamepadManager.processKeyEvent(event)) {
            return true;
        }

        return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        // Traiter les événements de gamepad (axes analogiques)
        if (gamepadManager != null && gamepadManager.processAxisEvent(event)) {
            return true;
        }
        return super.onGenericMotionEvent(event);
    }
    
    /**
     * **100% RETROARCH** : Afficher le menu RetroArch principal au démarrage
     * Conforme à l'architecture RetroArch officielle
     */
    private void showRetroArchMainMenu() {
        Log.i(TAG, "🎮 **100% RETROARCH** - Affichage du menu principal RetroArch");
        
        try {
            // Afficher le menu RetroArch principal
            if (modernUI != null) {
                modernUI.showMainMenu();
                Log.i(TAG, "✅ Menu RetroArch principal affiché");
            } else {
                Log.w(TAG, "⚠️ modernUI non initialisé, lancement direct de l'émulation");
                // Fallback : lancer directement l'émulation avec ROM par défaut
                startEmulation();
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Erreur lors de l'affichage du menu RetroArch: " + e.getMessage(), e);
            // Fallback : lancer directement l'émulation
            startEmulation();
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