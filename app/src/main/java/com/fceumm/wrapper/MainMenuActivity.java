package com.fceumm.wrapper;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.content.SharedPreferences;

public class MainMenuActivity extends Activity {
    private static final String TAG = "MainMenuActivity";
    private static final String PREF_NAME = "FCEUmmSettings";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Mode FULLSCREEN complet
        getWindow().setFlags(
            android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN,
            android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN
        );
        
        setContentView(R.layout.activity_main_menu);
        
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
        
        Log.i(TAG, "Menu principal initialisé - MODE FULLSCREEN");
        
        // Configurer les boutons
        setupButtons();
        
        // **100% RETROARCH** : Ne plus charger automatiquement la configuration
        // pour éviter les conflits avec l'émulation
        // displayCurrentConfiguration(); // DÉSACTIVÉ
    }
    
    private void setupButtons() {
        // **100% RETROARCH** : Configuration des boutons du menu principal
        
        // Bouton Play - Lance directement l'émulation avec ROM par défaut
        Button btnPlay = findViewById(R.id.btn_play);
        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Bouton Play pressé - Lancement direct de l'émulation avec ROM par défaut");
                Intent intent = new Intent(MainMenuActivity.this, EmulationActivity.class);
                // Pas de ROM spécifique = ROM par défaut (marioduckhunt.nes)
                startActivity(intent);
            }
        });
        
        // Bouton Sélection ROM - Ouvre le sélecteur de ROMs
        Button btnSelectRom = findViewById(R.id.btn_select_rom);
        btnSelectRom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Bouton Sélection ROM pressé");
                Intent intent = new Intent(MainMenuActivity.this, RomSelectionActivity.class);
                startActivity(intent);
            }
        });
        

        
        // Bouton Choix Core
        Button btnCoreSelection = findViewById(R.id.btn_core_selection);
        btnCoreSelection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Bouton Choix Core pressé");
                Intent intent = new Intent(MainMenuActivity.this, CoreSelectionActivity.class);
                startActivity(intent);
            }
        });
        
        // Bouton Settings
        Button btnSettings = findViewById(R.id.btn_settings);
        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Bouton Settings pressé");
                Intent intent = new Intent(MainMenuActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });
        
        // Bouton About
        Button btnAbout = findViewById(R.id.btn_about);
        btnAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Bouton About pressé");
                Intent intent = new Intent(MainMenuActivity.this, AboutActivity.class);
                startActivity(intent);
            }
        });
        

        

        
        Log.i(TAG, "Boutons du menu principal configurés");
    }
    
    /**
     * **100% RETROARCH** : Méthode simplifiée pour afficher la configuration
     * Appelée uniquement sur demande (bouton paramètres)
     */
    private void displayCurrentConfiguration() {
        try {
            SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
            String currentOverlay = prefs.getString("selected_overlay", "flat/nes");
            
            // **100% RETROARCH** : Affichage minimal pour éviter les conflits
            Log.i(TAG, "Configuration overlay: " + currentOverlay);
            
        } catch (Exception e) {
            Log.w(TAG, "Erreur lors de l'affichage de la configuration: " + e.getMessage());
        }
    }
    
    private String getOverlayDisplayName(String overlayPath) {
        if (overlayPath == null) return "Défaut";
        
        switch (overlayPath) {
            case "flat/nes":
                return "NES (Flat)";
            case "retropad":
                return "Retropad";
            case "flat/snes":
                return "SNES";
            case "flat/nintendo64":
                return "Nintendo 64";
            case "neo-retropad":
                return "Neo-Retropad";
            case "debug/visual":
                return "Debug Visuel";
            case "test/simple":
                return "Test Simple";
            default:
                return overlayPath;
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "Menu principal affiché");
        
        // **100% RETROARCH** : Ne plus recharger automatiquement la configuration
        // pour éviter les conflits avec l'émulation
        // displayCurrentConfiguration(); // DÉSACTIVÉ
    }
    
    @Override
    public void onBackPressed() {
        // Demander confirmation avant de quitter
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("❓ Confirmation");
        builder.setMessage("Voulez-vous vraiment quitter l'application ?");
        builder.setPositiveButton("✅ Oui", new android.content.DialogInterface.OnClickListener() {
            @Override
            public void onClick(android.content.DialogInterface dialog, int which) {
                finish();
            }
        });
        builder.setNegativeButton("❌ Non", null);
        builder.show();
    }
} 