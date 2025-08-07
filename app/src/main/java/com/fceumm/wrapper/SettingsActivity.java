package com.fceumm.wrapper;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.widget.SeekBar;
import android.app.AlertDialog;
import android.content.DialogInterface;

public class SettingsActivity extends Activity {
    private static final String TAG = "SettingsActivity";
    private static final String PREF_NAME = "FCEUmmSettings";
    private static final String KEY_SELECTED_OVERLAY = "selected_overlay";
    private static final String KEY_DIAGONAL_SENSITIVITY = "diagonal_sensitivity";
    private static final String KEY_OVERLAY_OPACITY = "overlay_opacity";
    private static final String KEY_DETAILED_LOGS = "detailed_logs";
    
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
        // === SECTION OVERLAYS ===
        
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
        
        // Bouton Overlay Retropad Original
        Button btnOverlayRetropadOriginal = findViewById(R.id.btn_overlay_retropad_original);
        btnOverlayRetropadOriginal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Overlay Retropad Original sélectionné");
                saveOverlaySelection("retropad");
                Toast.makeText(SettingsActivity.this, "✅ Overlay Retropad Original sélectionné", Toast.LENGTH_SHORT).show();
            }
        });
        
        // Bouton Overlay Neo-Retropad
        Button btnOverlayNeoRetropad = findViewById(R.id.btn_overlay_neo_retropad);
        btnOverlayNeoRetropad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Overlay Neo-Retropad sélectionné");
                saveOverlaySelection("neo-retropad");
                Toast.makeText(SettingsActivity.this, "✅ Overlay Neo-Retropad sélectionné", Toast.LENGTH_SHORT).show();
            }
        });
        
        // === SECTION DEBUG ===
        
        // Bouton Debug Visuel
        Button btnOverlayDebug = findViewById(R.id.btn_overlay_debug);
        btnOverlayDebug.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Overlay Debug Visuel sélectionné");
                saveOverlaySelection("debug/visual");
                Toast.makeText(SettingsActivity.this, "🔍 Mode Debug Visuel activé", Toast.LENGTH_SHORT).show();
            }
        });
        
        // Bouton Test Simple
        Button btnOverlayTest = findViewById(R.id.btn_overlay_test);
        btnOverlayTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Overlay Test Simple sélectionné");
                saveOverlaySelection("test/simple");
                Toast.makeText(SettingsActivity.this, "🧪 Mode Test Simple activé", Toast.LENGTH_SHORT).show();
            }
        });
        
        // === SECTION PARAMÈTRES AVANCÉS ===
        
        // Bouton Sensibilité Diagonales
        Button btnDiagonalSensitivity = findViewById(R.id.btn_diagonal_sensitivity);
        btnDiagonalSensitivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDiagonalSensitivityDialog();
            }
        });
        
        // Bouton Opacité Overlays
        Button btnOverlayOpacity = findViewById(R.id.btn_overlay_opacity);
        btnOverlayOpacity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showOverlayOpacityDialog();
            }
        });
        
        // Bouton Logs Détaillés
        Button btnDetailedLogs = findViewById(R.id.btn_detailed_logs);
        btnDetailedLogs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleDetailedLogs();
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
    
    private void showDiagonalSensitivityDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("🎯 Sensibilité des Diagonales");
        
        // Créer la vue avec SeekBar
        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(50, 50, 50, 50);
        
        final SeekBar seekBar = new SeekBar(this);
        seekBar.setMax(100);
        int currentSensitivity = preferences.getInt(KEY_DIAGONAL_SENSITIVITY, 50);
        seekBar.setProgress(currentSensitivity);
        
        final android.widget.TextView textView = new android.widget.TextView(this);
        textView.setText("Sensibilité: " + currentSensitivity + "%");
        textView.setTextColor(android.graphics.Color.WHITE);
        textView.setTextSize(16);
        textView.setPadding(0, 20, 0, 20);
        
        seekBar.setOnSeekBarChangeListener(new android.widget.SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(android.widget.SeekBar seekBar, int progress, boolean fromUser) {
                textView.setText("Sensibilité: " + progress + "%");
            }
            
            @Override
            public void onStartTrackingTouch(android.widget.SeekBar seekBar) {}
            
            @Override
            public void onStopTrackingTouch(android.widget.SeekBar seekBar) {}
        });
        
        layout.addView(textView);
        layout.addView(seekBar);
        
        builder.setView(layout);
        builder.setPositiveButton("✅ Appliquer", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int sensitivity = seekBar.getProgress();
                saveDiagonalSensitivity(sensitivity);
                Toast.makeText(SettingsActivity.this, "🎯 Sensibilité définie: " + sensitivity + "%", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("❌ Annuler", null);
        
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    
    private void showOverlayOpacityDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("🎨 Opacité des Overlays");
        
        // Créer la vue avec SeekBar
        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(50, 50, 50, 50);
        
        final SeekBar seekBar = new SeekBar(this);
        seekBar.setMax(100);
        int currentOpacity = preferences.getInt(KEY_OVERLAY_OPACITY, 80);
        seekBar.setProgress(currentOpacity);
        
        final android.widget.TextView textView = new android.widget.TextView(this);
        textView.setText("Opacité: " + currentOpacity + "%");
        textView.setTextColor(android.graphics.Color.WHITE);
        textView.setTextSize(16);
        textView.setPadding(0, 20, 0, 20);
        
        seekBar.setOnSeekBarChangeListener(new android.widget.SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(android.widget.SeekBar seekBar, int progress, boolean fromUser) {
                textView.setText("Opacité: " + progress + "%");
            }
            
            @Override
            public void onStartTrackingTouch(android.widget.SeekBar seekBar) {}
            
            @Override
            public void onStopTrackingTouch(android.widget.SeekBar seekBar) {}
        });
        
        layout.addView(textView);
        layout.addView(seekBar);
        
        builder.setView(layout);
        builder.setPositiveButton("✅ Appliquer", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int opacity = seekBar.getProgress();
                saveOverlayOpacity(opacity);
                Toast.makeText(SettingsActivity.this, "🎨 Opacité définie: " + opacity + "%", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("❌ Annuler", null);
        
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    
    private void toggleDetailedLogs() {
        boolean currentState = preferences.getBoolean(KEY_DETAILED_LOGS, false);
        boolean newState = !currentState;
        
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(KEY_DETAILED_LOGS, newState);
        editor.apply();
        
        String message = newState ? "📋 Logs détaillés activés" : "📋 Logs détaillés désactivés";
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        
        Log.i(TAG, "Logs détaillés: " + (newState ? "activés" : "désactivés"));
    }
    
    private void saveOverlaySelection(String overlayName) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY_SELECTED_OVERLAY, overlayName);
        editor.apply();
        Log.i(TAG, "Overlay sauvegardé: " + overlayName);
    }
    
    private void saveDiagonalSensitivity(int sensitivity) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(KEY_DIAGONAL_SENSITIVITY, sensitivity);
        editor.apply();
        Log.i(TAG, "Sensibilité diagonales sauvegardée: " + sensitivity);
    }
    
    private void saveOverlayOpacity(int opacity) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(KEY_OVERLAY_OPACITY, opacity);
        editor.apply();
        Log.i(TAG, "Opacité overlay sauvegardée: " + opacity);
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
    
    /**
     * Méthode statique pour récupérer la sensibilité des diagonales
     */
    public static int getDiagonalSensitivity(Activity activity) {
        SharedPreferences prefs = activity.getSharedPreferences(PREF_NAME, Activity.MODE_PRIVATE);
        return prefs.getInt(KEY_DIAGONAL_SENSITIVITY, 50); // Par défaut: 50%
    }
    
    /**
     * Méthode statique pour récupérer l'opacité des overlays
     */
    public static int getOverlayOpacity(Activity activity) {
        SharedPreferences prefs = activity.getSharedPreferences(PREF_NAME, Activity.MODE_PRIVATE);
        return prefs.getInt(KEY_OVERLAY_OPACITY, 80); // Par défaut: 80%
    }
    
    /**
     * Méthode statique pour vérifier si les logs détaillés sont activés
     */
    public static boolean isDetailedLogsEnabled(Activity activity) {
        SharedPreferences prefs = activity.getSharedPreferences(PREF_NAME, Activity.MODE_PRIVATE);
        return prefs.getBoolean(KEY_DETAILED_LOGS, false);
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