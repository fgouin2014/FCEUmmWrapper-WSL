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

public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";
    private EmulatorView emulatorView;
    private Handler mainHandler;
    private boolean isRunning = false;

    static {
        System.loadLibrary("fceummwrapper");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        
        Log.i(TAG, "Démarrage de l'application FCEUmm Wrapper");
        
        // Initialiser le handler pour le thread principal
        mainHandler = new Handler(Looper.getMainLooper());
        
                       // Copier la ROM Mario Duck Hunt depuis les assets
               copyMarioDuckHuntRom();
        
        // Créer la vue d'émulation
        emulatorView = new EmulatorView(this);
        setContentView(emulatorView);
        
        // Initialiser le wrapper libretro
        if (initLibretro()) {
            Log.i(TAG, "Wrapper libretro initialisé avec succès");
            
                               // Charger la ROM
                   String romPath = new File(getFilesDir(), "marioduckhunt.nes").getAbsolutePath();
            if (loadROM(romPath)) {
                Log.i(TAG, "ROM chargée avec succès");
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
        
        // Démarrer la boucle d'émulation dans un thread séparé
        new Thread(() -> {
            while (isRunning) {
                // Exécuter une frame
                runFrame();
                
                // Vérifier si une nouvelle frame est disponible
                if (emulatorView.isFrameUpdated()) {
                    // Mettre à jour l'affichage sur le thread principal
                    mainHandler.post(() -> {
                        byte[] frameData = emulatorView.getFrameBuffer();
                        int width = emulatorView.getFrameWidth();
                        int height = emulatorView.getFrameHeight();
                        
                        if (frameData != null) {
                            emulatorView.updateFrame(frameData, width, height);
                        }
                    });
                }
                
                // Attendre un peu pour ne pas surcharger le CPU
                try {
                    Thread.sleep(16); // ~60 FPS
                } catch (InterruptedException e) {
                    break;
                }
            }
        }).start();
        
        Log.i(TAG, "Émulation démarrée");
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isRunning = false;
        cleanup();
    }
    
    // Méthodes natives
    private native boolean initLibretro();
    private native boolean loadROM(String romPath);
    private native void runFrame();
    private native void cleanup();
} 