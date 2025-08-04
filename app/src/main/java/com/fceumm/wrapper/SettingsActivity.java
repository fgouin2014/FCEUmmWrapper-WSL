package com.fceumm.wrapper;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class SettingsActivity extends Activity {
    private static final String TAG = "SettingsActivity";
    private static final String PREF_NAME = "FCEUmmSettings";
    private static final String KEY_SELECTED_OVERLAY = "selected_overlay";
    
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Mode FULLSCREEN complet
        getWindow().setFlags(
            android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN,
            android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN
        );
        
        setContentView(R.layout.activity_settings);
        
        // Masquer la barre de navigation (Android 4.4+)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                    getWindow().setDecorFitsSystemWindows(false);
                    if (getWindow().getInsetsController() != null) {
                        getWindow().getInsetsController().hide(android.view.WindowInsets.Type.statusBars() | android.view.WindowInsets.Type.navigationBars());
                        getWindow().getInsetsController().setSystemBarsBehavior(android.view.WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
                    }
                } else {
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
        
        // Initialiser les préférences
        preferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        
        Log.i(TAG, "Activité des paramètres initialisée");
        
        // Configurer les boutons
        setupButtons();
    }
    
    private void setupButtons() {
        // Bouton Overlay NES (Flat)
        Button btnOverlayNes = findViewById(R.id.btn_overlay_nes);
        btnOverlayNes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Overlay NES (Flat) sélectionné");
                saveOverlaySelection("flat/nes");
                Toast.makeText(SettingsActivity.this, "✅ Overlay NES (Flat) sélectionné", Toast.LENGTH_SHORT).show();
            }
        });
        
        // Bouton Overlay Retropad
        Button btnOverlayRetropad = findViewById(R.id.btn_overlay_retropad);
        btnOverlayRetropad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Overlay Retropad sélectionné");
                saveOverlaySelection("retropad");
                Toast.makeText(SettingsActivity.this, "✅ Overlay Retropad sélectionné", Toast.LENGTH_SHORT).show();
            }
        });
        
        // Bouton Overlay SNES
        Button btnOverlaySnes = findViewById(R.id.btn_overlay_snes);
        btnOverlaySnes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Overlay SNES sélectionné");
                saveOverlaySelection("flat/snes");
                Toast.makeText(SettingsActivity.this, "✅ Overlay SNES sélectionné", Toast.LENGTH_SHORT).show();
            }
        });
        
        // Bouton Overlay Nintendo 64
        Button btnOverlayN64 = findViewById(R.id.btn_overlay_n64);
        btnOverlayN64.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Overlay Nintendo 64 sélectionné");
                saveOverlaySelection("flat/nintendo64");
                Toast.makeText(SettingsActivity.this, "✅ Overlay Nintendo 64 sélectionné", Toast.LENGTH_SHORT).show();
            }
        });
        
        // Bouton Retour
        Button btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Bouton Retour pressé");
                finish();
            }
        });
    }
    
    private void saveOverlaySelection(String overlayName) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY_SELECTED_OVERLAY, overlayName);
        editor.apply();
        Log.i(TAG, "Overlay sauvegardé: " + overlayName);
    }
    
    /**
     * Méthode statique pour récupérer l'overlay sélectionné
     */
    public static String getSelectedOverlay(Activity activity) {
        SharedPreferences prefs = activity.getSharedPreferences(PREF_NAME, Activity.MODE_PRIVATE);
        String overlay = prefs.getString(KEY_SELECTED_OVERLAY, "flat/nes"); // Par défaut: NES
        Log.i("SettingsActivity", "Overlay récupéré: " + overlay);
        return overlay;
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "Activité des paramètres reprise");
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "Activité des paramètres mise en pause");
    }
} 