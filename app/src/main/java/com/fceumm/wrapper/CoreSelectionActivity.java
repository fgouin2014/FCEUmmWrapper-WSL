package com.fceumm.wrapper;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class CoreSelectionActivity extends Activity {
    private static final String TAG = "CoreSelectionActivity";
    private static final String PREF_NAME = "CoreSettings";
    private static final String PREF_CORE_TYPE = "core_type";
    private static final String CORE_TYPE_COMPILED = "compiled";
    private static final String CORE_TYPE_CUSTOM = "custom";

    private TextView tvCurrentCore;
    private Button btnUseCompiled;
    private Button btnUseCustom;
    private Button btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Mode FULLSCREEN complet
        getWindow().setFlags(
            android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN,
            android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN
        );
        
        setContentView(R.layout.activity_core_selection);
        
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
        
        Log.i(TAG, "Activité de sélection de core initialisée");
        
        // Initialiser les vues
        initViews();
        
        // Charger et afficher le core actuel
        updateCurrentCoreDisplay();
        
        // Configurer les boutons
        setupButtons();
    }
    
    private void initViews() {
        tvCurrentCore = findViewById(R.id.tv_current_core);
        btnUseCompiled = findViewById(R.id.btn_use_compiled);
        btnUseCustom = findViewById(R.id.btn_use_custom);
        btnBack = findViewById(R.id.btn_back);
    }
    
    private void updateCurrentCoreDisplay() {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        String currentCoreType = prefs.getString(PREF_CORE_TYPE, CORE_TYPE_COMPILED);
        
        String coreInfo;
        if (CORE_TYPE_COMPILED.equals(currentCoreType)) {
            coreInfo = "Core actuel : Pré-compilé (coresCompiled)";
        } else {
            coreInfo = "Core actuel : Personnalisé (coreCustom)";
        }
        
        tvCurrentCore.setText(coreInfo);
        Log.i(TAG, "Core actuel affiché: " + coreInfo);
    }
    
    private void setupButtons() {
        // Bouton pour utiliser les cores pré-compilés
        btnUseCompiled.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Bouton Cores Pré-compilés pressé");
                setCoreType(CORE_TYPE_COMPILED);
                Toast.makeText(CoreSelectionActivity.this, "Core pré-compilé sélectionné", Toast.LENGTH_SHORT).show();
            }
        });
        
        // Bouton pour utiliser les cores personnalisés
        btnUseCustom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Bouton Cores Personnalisés pressé");
                setCoreType(CORE_TYPE_CUSTOM);
                Toast.makeText(CoreSelectionActivity.this, "Core personnalisé sélectionné", Toast.LENGTH_SHORT).show();
            }
        });
        
        // Bouton Retour
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Bouton Retour pressé");
                finish();
            }
        });
    }
    
    private void setCoreType(String coreType) {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PREF_CORE_TYPE, coreType);
        editor.apply();
        
        Log.i(TAG, "Core type sauvegardé: " + coreType);
        updateCurrentCoreDisplay();
    }
    
    // Méthode statique pour récupérer le type de core actuel
    public static String getCurrentCoreType(android.content.Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, android.content.Context.MODE_PRIVATE);
        return prefs.getString(PREF_CORE_TYPE, CORE_TYPE_COMPILED);
    }
    
    // Méthode statique pour vérifier si on utilise les cores personnalisés
    public static boolean isUsingCustomCores(android.content.Context context) {
        return CORE_TYPE_CUSTOM.equals(getCurrentCoreType(context));
    }
} 