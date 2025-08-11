package com.fceumm.wrapper;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RomSelectionActivity extends Activity {
    private static final String TAG = "RomSelectionActivity";
    private ListView romListView;
    private List<String> romList;
    private RomListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Mode FULLSCREEN complet
        getWindow().setFlags(
            android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN,
            android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN
        );
        
        setContentView(R.layout.activity_rom_selection);
        
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
        
        Log.i(TAG, "Activité de sélection des ROMs initialisée");
        
        // Initialiser la liste des ROMs
        initRomList();
        setupListView();
        setupBackButton();
    }
    
    private void initRomList() {
        romList = new ArrayList<>();
        
        try {
            // Lister les fichiers dans le dossier assets/roms/nes
            String[] files = getAssets().list("roms/nes");
            if (files != null) {
                Log.d(TAG, "Fichiers trouvés dans roms/nes: " + files.length);
                for (String file : files) {
                    Log.d(TAG, "Fichier: " + file);
                    if (file.toLowerCase().endsWith(".nes")) {
                        // Enlever l'extension .nes pour l'affichage
                        String displayName = file.substring(0, file.length() - 4);
                        romList.add(displayName);
                        Log.d(TAG, "ROM trouvée: " + displayName);
                    }
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Erreur lors de la lecture des ROMs", e);
            Toast.makeText(this, "Erreur lors du chargement des ROMs", Toast.LENGTH_SHORT).show();
        }
        
        if (romList.isEmpty()) {
            romList.add("Aucune ROM trouvée");
            Toast.makeText(this, "Aucune ROM NES trouvée dans le dossier assets", Toast.LENGTH_LONG).show();
        }
    }
    
    private void setupListView() {
        romListView = findViewById(R.id.rom_list_view);
        
        adapter = new RomListAdapter(this, romList);
        
        romListView.setAdapter(adapter);
        
        romListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedRom = romList.get(position);
                
                if (!selectedRom.equals("Aucune ROM trouvée")) {
                    Log.i(TAG, "ROM sélectionnée: " + selectedRom);
                    
                    // Lancer directement EmulationActivity avec la ROM sélectionnée
                    Intent intent = new Intent(RomSelectionActivity.this, EmulationActivity.class);
                    intent.putExtra("selected_rom", selectedRom + ".nes");
                    startActivity(intent);
                } else {
                    Toast.makeText(RomSelectionActivity.this, 
                        "Aucune ROM disponible", Toast.LENGTH_SHORT).show();
                }
            }
        });
        
        Log.i(TAG, "Liste des ROMs configurée avec " + romList.size() + " ROMs");
    }
    
    private void setupBackButton() {
        android.widget.Button btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Bouton Retour pressé");
                finish();
            }
        });
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
} 