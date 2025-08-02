package com.fceumm.wrapper;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class AboutActivity extends Activity {
    private static final String TAG = "AboutActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Mode FULLSCREEN complet
        getWindow().setFlags(
            android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN,
            android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN
        );
        
        setContentView(R.layout.activity_about);
        
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
        
        Log.i(TAG, "Activité About initialisée - MODE FULLSCREEN");
        
        // Améliorer le layout avec plus d'informations
        setupAboutContent();
    }
    
    private void setupAboutContent() {
        TextView titleText = findViewById(R.id.about_title);
        if (titleText != null) {
            titleText.setText("FCEUmm Wrapper");
        }
        
        TextView versionText = findViewById(R.id.about_version);
        if (versionText != null) {
            versionText.setText("Version 1.0");
        }
        
        TextView descriptionText = findViewById(R.id.about_description);
        if (descriptionText != null) {
            descriptionText.setText("Émulateur NES basé sur FCEUmm\n" +
                                 "Développé avec libretro\n" +
                                 "Interface Android native");
        }
        
        // Bouton retour
        Button btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i(TAG, "Bouton retour pressé");
                    finish();
                }
            });
        }
        
        Log.i(TAG, "Contenu About configuré");
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "Écran About affiché");
    }
} 